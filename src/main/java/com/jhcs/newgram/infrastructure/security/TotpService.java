package com.jhcs.newgram.infrastructure.security;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * TOTP (RFC 6238, 6 dígitos, passo 30s) sem dependência externa.
 * Segredo em Base32 (RFC 4648, sem padding). Verificação com janela ±1 passo.
 */
@Service
public class TotpService {

    private static final int PASSO_SEGUNDOS = 30;
    private static final int DIGITOS = 6;
    private static final int SEGREDO_BYTES = 20;
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public String gerarSegredo() {
        byte[] aleatorio = new byte[SEGREDO_BYTES];
        new SecureRandom().nextBytes(aleatorio);
        return base32Encode(aleatorio);
    }

    public String uriProvisionamento(String emissor, String conta, String segredo) {
        return "otpauth://totp/" + emissor + ":" + conta
                + "?secret=" + segredo + "&issuer=" + emissor + "&digits=6&period=30";
    }

    public boolean verificar(String segredoBase32, String codigo) {
        if (segredoBase32 == null || codigo == null || !codigo.matches("\\d{6}")) {
            return false;
        }
        byte[] chave;
        try {
            chave = base32Decode(segredoBase32);
        } catch (IllegalArgumentException e) {
            return false;
        }
        long passoAtual = Instant.now().getEpochSecond() / PASSO_SEGUNDOS;
        for (long passo = passoAtual - 1; passo <= passoAtual + 1; passo++) {
            if (gerarCodigo(chave, passo).equals(codigo)) {
                return true;
            }
        }
        return false;
    }

    private static String gerarCodigo(byte[] chave, long passo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(chave, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(passo).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binario = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binario % 1_000_000;
            return String.format("%06d", otp);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar TOTP", e);
        }
    }

    static String base32Encode(byte[] dados) {
        StringBuilder saida = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : dados) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                saida.append(BASE32.charAt((buffer >> bits) & 0x1F));
            }
        }
        if (bits > 0) {
            saida.append(BASE32.charAt((buffer << (5 - bits)) & 0x1F));
        }
        return saida.toString();
    }

    static byte[] base32Decode(String texto) {
        String limpo = texto.trim().replace("=", "").toUpperCase();
        ByteBuffer saida = ByteBuffer.allocate(limpo.length() * 5 / 8 + 1);
        int buffer = 0;
        int bits = 0;
        for (char c : limpo.toCharArray()) {
            int valor = BASE32.indexOf(c);
            if (valor < 0) {
                throw new IllegalArgumentException("Base32 inválido");
            }
            buffer = (buffer << 5) | valor;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                saida.put((byte) ((buffer >> bits) & 0xFF));
            }
        }
        byte[] resultado = new byte[saida.position()];
        saida.rewind();
        saida.get(resultado);
        return resultado;
    }
}
