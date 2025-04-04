package com.jhcs.newgram.infrastructure.security;

import com.jhcs.newgram.core.domain.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.access.expiration:86400000}") // Padrão: 24 horas
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration:604800000}") // Padrão: 7 dias
    private long refreshTokenExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private final UserDetailsService userDetailsService;

    // Armazenamento para tokens invalidados
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public JwtService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() {
        try {
            generateKeyPair();
        } catch (Exception e) {
            logger.error("Falha ao inicializar chaves JWT", e);
            throw new RuntimeException("Falha ao inicializar serviço JWT", e);
        }
    }

    private void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        logger.info("Par de chaves JWT gerado com sucesso");
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        return generateToken(claims, userDetails, refreshTokenExpiration);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {
        if (!(userDetails instanceof Usuario)) {
            throw new IllegalArgumentException("UserDetails deve ser uma instância de Usuario");
        }

        Usuario usuario = (Usuario) userDetails;
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        extraClaims.put("nome", usuario.getNome());
        extraClaims.put("id", usuario.getId());

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            if (blacklistedTokens.contains(token)) {
                return false;
            }

            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            logger.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            if (blacklistedTokens.contains(token) || isTokenExpired(token)) {
                return false;
            }

            Object tokenType = extractAllClaims(token).get("tokenType");
            return tokenType != null && tokenType.equals("refresh");
        } catch (ExpiredJwtException e) {
            logger.warn("Refresh token expirado");
            return false;
        } catch (JwtException e) {
            logger.warn("Refresh token inválido: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, String> refreshToken(String refreshToken) {
        if (!isRefreshTokenValid(refreshToken)) {
            throw new JwtAuthenticationException("Refresh token inválido ou expirado");
        }

        String email = extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Invalidar o refresh token antigo
        blacklistedTokens.add(refreshToken);

        String newAccessToken = generateToken(userDetails);
        String newRefreshToken = generateRefreshToken(userDetails);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);

        return tokens;
    }

    public void invalidateToken(String token) {
        blacklistedTokens.add(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expirado: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT não suportado: {}", e.getMessage());
            throw new JwtAuthenticationException("Formato de token não suportado", e);
        } catch (MalformedJwtException e) {
            logger.error("Token JWT inválido: {}", e.getMessage());
            throw new JwtAuthenticationException("Token mal formatado", e);
        } catch (SignatureException e) {
            logger.error("Assinatura JWT inválida: {}", e.getMessage());
            throw new JwtAuthenticationException("Assinatura do token inválida", e);
        } catch (IllegalArgumentException e) {
            logger.error("String de claims JWT vazia: {}", e.getMessage());
            throw new JwtAuthenticationException("Token vazio ou inválido", e);
        }
    }

    public static class JwtAuthenticationException extends RuntimeException {
        public JwtAuthenticationException(String message) {
            super(message);
        }

        public JwtAuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}