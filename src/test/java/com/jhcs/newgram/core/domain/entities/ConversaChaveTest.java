package com.jhcs.newgram.core.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConversaChaveTest {

    @Test
    void chaveOrdenadaIndependeDaOrdem() {
        assertEquals("3_17", Conversa.chavePara(3L, 17L));
        assertEquals("3_17", Conversa.chavePara(17L, 3L));
    }

    @Test
    void paresDiferentesGeramChavesDiferentes() {
        assertEquals("3_18", Conversa.chavePara(3L, 18L));
    }
}
