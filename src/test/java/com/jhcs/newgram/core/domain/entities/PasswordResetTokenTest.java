package com.jhcs.newgram.core.domain.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PasswordResetTokenTest {

    @Test
    void tokenValidoAntesDaExpiracao() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiracao(LocalDateTime.now().plusMinutes(30));
        assertFalse(token.expirado(LocalDateTime.now()));
    }

    @Test
    void tokenUsadoContaComoExpirado() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiracao(LocalDateTime.now().plusMinutes(30));
        token.setUsado(true);
        assertTrue(token.expirado(LocalDateTime.now()));
    }

    @Test
    void tokenVencidoContaComoExpirado() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiracao(LocalDateTime.now().minusMinutes(1));
        assertTrue(token.expirado(LocalDateTime.now()));
    }
}
