package com.jhcs.newgram.infrastructure.security;

import com.jhcs.newgram.core.domain.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final UserDetailsService userDetailsService;

    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24; // 1 hora
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 dias

    public JwtService(UserDetailsService userDetailsService) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        this.userDetailsService = userDetailsService;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        return generateToken(claims, userDetails, REFRESH_TOKEN_EXPIRATION);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {
        Usuario usuario = (Usuario) userDetails;
        extraClaims.put("nome", usuario.getNome());
        extraClaims.put("id", usuario.getId());
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            if (isTokenExpired(token)) {
                return false;
            }

            // Verificar se o token é do tipo refresh
            Object tokenType = extractAllClaims(token).get("tokenType");
            return tokenType != null && tokenType.equals("refresh");
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, String> refreshToken(String refreshToken) {
        if (!isRefreshTokenValid(refreshToken)) {
            throw new RuntimeException("Refresh token inválido ou expirado");
        }

        String email = extractUsername(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String newAccessToken = generateToken(userDetails);

        String newRefreshToken = generateRefreshToken(userDetails);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);

        return tokens;
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
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}