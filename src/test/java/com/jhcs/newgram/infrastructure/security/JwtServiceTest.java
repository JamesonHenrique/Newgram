package com.jhcs.newgram.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jhcs.newgram.core.domain.entities.RefreshToken;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.RefreshTokenRepository;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private final List<RefreshToken> sessions = new ArrayList<>();
    private UserDetailsService userDetailsService;
    private RefreshTokenRepository refreshTokenRepository;
    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        userDetailsService = mock(UserDetailsService.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        usuario = usuario(7L, "jameson@example.test");

        when(refreshTokenRepository.findByJti(anyString())).thenAnswer(invocation -> {
            String jti = invocation.getArgument(0);
            return sessions.stream().filter(session -> jti.equals(session.getJti())).findFirst();
        });
        doAnswer(invocation -> {
            RefreshToken session = invocation.getArgument(0);
            sessions.removeIf(existing -> session.getJti().equals(existing.getJti()));
            sessions.add(session);
            return session;
        }).when(refreshTokenRepository).save(any(RefreshToken.class));

        jwtService = new JwtService(userDetailsService, refreshTokenRepository);
        ReflectionTestUtils.setField(jwtService, "secret", Base64.getEncoder().encodeToString(new byte[32]));
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 60_000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604_800_000L);
        ReflectionTestUtils.setField(jwtService, "clockSkewSeconds", 0L);
        ReflectionTestUtils.setField(jwtService, "issuer", "newgram-test");
        jwtService.init();
    }

    @Test
    void accessTokenExigeTipoEUsuarioCorrespondentes() {
        String accessToken = jwtService.generateToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        assertTrue(jwtService.isTokenValid(accessToken, usuario));
        assertFalse(jwtService.isTokenValid(accessToken, usuario(8L, "other@example.test")));
        assertFalse(jwtService.isTokenValid(refreshToken, usuario));
        assertTrue(jwtService.isRefreshTokenValid(refreshToken));
    }

    @Test
    void rotacaoRevogaRefreshAnteriorERejeitaReuso() {
        when(userDetailsService.loadUserByUsername(usuario.getUsername())).thenReturn(usuario);
        String oldRefreshToken = jwtService.generateRefreshToken(usuario);
        String oldJti = jwtService.extractJti(oldRefreshToken);

        Map<String, String> rotated = jwtService.refreshToken(oldRefreshToken);
        String newRefreshToken = rotated.get("refreshToken");
        RefreshToken oldSession = sessions.stream()
                .filter(session -> oldJti.equals(session.getJti()))
                .findFirst()
                .orElseThrow();

        assertEquals(2, rotated.size());
        assertTrue(oldSession.isRevogado());
        assertFalse(jwtService.isRefreshTokenValid(oldRefreshToken));
        assertTrue(jwtService.isRefreshTokenValid(newRefreshToken));
        assertTrue(jwtService.isTokenValid(rotated.get("accessToken"), usuario));
        assertNotEquals(oldJti, jwtService.extractJti(newRefreshToken));
        assertThrows(JwtService.JwtAuthenticationException.class, () -> jwtService.refreshToken(oldRefreshToken));
    }

    @Test
    void tokenExpiradoNaoEValido() {
        String expiredToken = jwtService.generateToken(Map.of(JwtService.CLAIM_TOKEN_TYPE, JwtService.TYPE_ACCESS), usuario, -1L);

        assertFalse(jwtService.isTokenValid(expiredToken, usuario));
    }

    private static Usuario usuario(Long id, String email) {
        Usuario user = new Usuario();
        user.setId(id);
        user.setNome("Jameson");
        user.setUsername(email);
        user.setEmail(email);
        return user;
    }
}
