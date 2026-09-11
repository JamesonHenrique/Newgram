package com.jhcs.newgram.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TotpServiceTest {

    private final TotpService totp = new TotpService();

    @Test
    void base32IdaEVolta() {
        byte[] dados = new byte[20];
        new java.security.SecureRandom().nextBytes(dados);
        assertArrayEquals(dados, TotpService.base32Decode(TotpService.base32Encode(dados)));
    }

    @Test
    void rejeitaCodigoInvalidoESegredoRuim() {
        String segredo = totp.gerarSegredo();
        assertFalse(totp.verificar(segredo, "000000"));
        assertFalse(totp.verificar(segredo, "abc"));
        assertFalse(totp.verificar(null, "123456"));
        assertFalse(totp.verificar("!!!", "123456"));
    }

    @Test
    void segredoTem20Bytes() {
        assertTrue(TotpService.base32Decode(totp.gerarSegredo()).length == 20);
    }
}
