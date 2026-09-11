package com.jhcs.newgram.infrastructure.security;

import com.jhcs.newgram.core.domain.entities.RefreshToken;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emissao e validacao de JWT (HMAC-SHA256).
 *
 * <p>Modelo: access curto (stateless) + refresh longo com rotacao e
 * persistência em {@code refresh_token} (revogável, lista sessões).
 * O segredo vem de {@code JWT_SECRET} (Base64 ou texto com ao menos
 * 256 bits). Sem segredo configurado, uma chave efemera e gerada apenas
 * para desenvolvimento (tokens nao sobrevivem ao restart — nunca em prod).
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
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(UserDetailsService userDetailsService, RefreshTokenRepository refreshTokenRepository) {
        this.userDetailsService = userDetailsService;
        this.refreshTokenRepository = refreshTokenRepository;
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

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TYPE_ACCESS);
        return generateToken(claims, userDetails, accessTokenExpiration);
    }

    /** Emite refresh e persiste a sessão (jti) para revogação posterior. */
    @Transactional
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TYPE_REFRESH);
        String jti = UUID.randomUUID().toString();
        String token = buildToken(claims, userDetails, refreshTokenExpiration, jti);

        if (userDetails instanceof Usuario usuario && usuario.getId() != null) {
            RefreshToken sessao = new RefreshToken();
            sessao.setUsuario(usuario);
            sessao.setJti(jti);
            sessao.setExpiracao(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000));
            sessao.setRevogado(false);
            refreshTokenRepository.save(sessao);
        }
        return token;
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {
        return buildToken(extraClaims, userDetails, expirationTime, UUID.randomUUID().toString());
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime, String jti) {
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
                .id(jti)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    /** Access token valido: assinatura ok, nao expirado, issuer e tipo conferem. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
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
            Claims claims = extractAllClaims(token);
            if (!TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE))) {
                return false;
            }
            if (!issuer.equals(claims.getIssuer())) {
                return false;
            }
            if (!claims.getExpiration().after(new Date())) {
                return false;
            }
            // Sessão precisa existir e estar ativa no banco.
            return refreshTokenRepository.findByJti(claims.getId())
                    .map(sessao -> !sessao.expirado(LocalDateTime.now()))
                    .orElse(false);
        } catch (ExpiredJwtException e) {
            logger.warn("Refresh token expirado");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Refresh token invalido");
            return false;
        }
    }

    /**
     * Rotaciona o par de tokens: revoga o refresh antigo e emite access +
     * refresh novos. Reuso do refresh antigo apos a rotacao e rejeitado
     * (sem reemissao em cadeia — sinal de possivel roubo).
     */
    @Transactional
    public Map<String, String> refreshToken(String refreshToken) {
        if (!isRefreshTokenValid(refreshToken)) {
            throw new JwtAuthenticationException("Refresh token inválido ou expirado");
        }
        String email = extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String jtiAntigo = extractJti(refreshToken);
        refreshTokenRepository.findByJti(jtiAntigo).ifPresent(sessao -> {
            sessao.setRevogado(true);
            refreshTokenRepository.save(sessao);
        });

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", generateToken(userDetails));
        tokens.put("refreshToken", generateRefreshToken(userDetails));
        return tokens;
    }

    /** Logout: revoga a sessão do refresh informado (idempotente). */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        try {
            String jti = extractJti(refreshToken);
            refreshTokenRepository.findByJti(jti).ifPresent(sessao -> {
                sessao.setRevogado(true);
                refreshTokenRepository.save(sessao);
            });
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Logout com refresh invalido (ignorado)");
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarSessoes(Long usuarioId) {
        return refreshTokenRepository.findSessoesAtivas(usuarioId, LocalDateTime.now()).stream()
                .map(sessao -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("jti", sessao.getJti());
                    item.put("expiracao", sessao.getExpiracao().atZone(ZoneId.of("UTC")).toInstant().toString());
                    item.put("dataCriacao", sessao.getDataCriacao().atZone(ZoneId.of("UTC")).toInstant().toString());
                    return item;
                })
                .toList();
    }

    @Transactional
    public void revogarSessao(String jti, Long usuarioId) {
        RefreshToken sessao = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new JwtAuthenticationException("Sessão não encontrada"));
        if (!sessao.getUsuario().getId().equals(usuarioId)) {
            throw new JwtAuthenticationException("Sessão não encontrada");
        }
        sessao.setRevogado(true);
        refreshTokenRepository.save(sessao);
    }

    @Transactional
    public void revogarTodasSessoes(Long usuarioId) {
        refreshTokenRepository.revogarTodasDoUsuario(usuarioId);
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
