package com.jhcs.newgram.infrastructure.security;

import com.jhcs.newgram.core.domain.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Emissao e validacao de JWT (HMAC-SHA256).
 *
 * <p>Modelo: access curto + refresh longo com rotacao. O segredo vem de
 * {@code JWT_SECRET} (Base64 ou texto com ao menos 256 bits). Sem segredo
 * configurado, uma chave efemera e gerada apenas para desenvolvimento
 * (tokens nao sobrevivem ao restart — nunca usar em prod).
 */
@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    static final String CLAIM_TOKEN_TYPE = "tokenType";
    static final String TYPE_ACCESS = "access";
    static final String TYPE_REFRESH = "refresh";

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.access.expiration:900000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration:604800000}")
    private long refreshTokenExpiration;

    @Value("${jwt.clock-skew-seconds:60}")
    private long clockSkewSeconds;

    @Value("${jwt.issuer:newgram}")
    private String issuer;

    private SecretKey signingKey;

    private final UserDetailsService userDetailsService;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public JwtService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            byte[] random = new byte[32];
            new SecureRandom().nextBytes(random);
            this.signingKey = Keys.hmacShaKeyFor(random);
            logger.warn("JWT_SECRET nao configurado: usando chave efemera (apenas desenvolvimento)");
            return;
        }
        byte[] keyBytes = decodeSecret(secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET precisa de ao menos 256 bits (32 bytes)");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private static byte[] decodeSecret(String value) {
        try {
            return Base64.getDecoder().decode(value.trim());
        } catch (IllegalArgumentException notBase64) {
            return value.trim().getBytes(StandardCharsets.UTF_8);
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TYPE_ACCESS);
        return generateToken(claims, userDetails, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TYPE_REFRESH);
        return generateToken(claims, userDetails, refreshTokenExpiration);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        if (userDetails instanceof Usuario usuario) {
            claims.putIfAbsent("nome", usuario.getNome());
            claims.putIfAbsent("id", usuario.getId());
        }
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    /** Access token valido: assinatura ok, nao expirado, nao revogado, issuer e tipo conferem. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            if (blacklistedTokens.contains(token)) {
                return false;
            }
            Claims claims = extractAllClaims(token);
            if (!TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE))) {
                return false;
            }
            if (!issuer.equals(claims.getIssuer())) {
                return false;
            }
            final String username = claims.getSubject();
            return username != null
                    && username.equals(userDetails.getUsername())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Token JWT invalido");
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            if (blacklistedTokens.contains(token)) {
                return false;
            }
            Claims claims = extractAllClaims(token);
            if (!TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE))) {
                return false;
            }
            if (!issuer.equals(claims.getIssuer())) {
                return false;
            }
            return claims.getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            logger.warn("Refresh token expirado");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Refresh token invalido");
            return false;
        }
    }

    /**
     * Rotaciona o par de tokens: invalida o refresh antigo e emite access +
     * refresh novos. Reuso do refresh antigo apos a rotacao e rejeitado
     * (sem reemissao em cadeia — sinal de possivel roubo).
     */
    public Map<String, String> refreshToken(String refreshToken) {
        if (!isRefreshTokenValid(refreshToken)) {
            throw new JwtAuthenticationException("Refresh token inválido ou expirado");
        }
        String email = extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        blacklistedTokens.add(refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", generateToken(userDetails));
        tokens.put("refreshToken", generateRefreshToken(userDetails));
        return tokens;
    }

    public void invalidateToken(String token) {
        blacklistedTokens.add(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .clockSkewSeconds(clockSkewSeconds)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expirado");
            throw e;
        } catch (JwtException e) {
            throw new JwtAuthenticationException("Token inválido", e);
        } catch (IllegalArgumentException e) {
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
