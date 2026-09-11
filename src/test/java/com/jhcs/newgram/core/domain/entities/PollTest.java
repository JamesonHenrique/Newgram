package com.jhcs.newgram.core.domain.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PollTest {

    @Test
    void semPrazoNuncaEncerra() {
        assertFalse(new Poll().encerrada(LocalDateTime.now()));
    }

    @Test
    void prazoFuturoAbertaEPassadoEncerrada() {
        Poll poll = new Poll();
        poll.setEncerraEm(LocalDateTime.now().plusHours(1));
        assertFalse(poll.encerrada(LocalDateTime.now()));

        poll.setEncerraEm(LocalDateTime.now().minusMinutes(1));
        assertTrue(poll.encerrada(LocalDateTime.now()));
    }
}
