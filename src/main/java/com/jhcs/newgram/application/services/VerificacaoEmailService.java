package com.jhcs.newgram.application.services;

import com.jhcs.newgram.core.domain.entities.EmailVerificationToken;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.EmailVerificationTokenRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verificação de e-mail com token opaco (24h). Sem SMTP, o link vai ao log
 * via {@link EmailService} — mesma resposta exista ou não a conta.
 */
@Service
@RequiredArgsConstructor
public class VerificacaoEmailService {

    private static final int EXPIRACAO_HORAS = 24;

    private final UsuarioRepository usuarioRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.front-url:http://localhost:4200}")
    private String frontUrl;

    @Transactional
    public void enviarLink(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (usuario.isEmailVerificado()) {
            return;
        }
        tokenRepository.findByUsuarioId(usuarioId).ifPresent(tokenRepository::delete);

        byte[] aleatorio = new byte[32];
        new SecureRandom().nextBytes(aleatorio);
        String token = HexFormat.of().formatHex(aleatorio);

        EmailVerificationToken registro = new EmailVerificationToken();
        registro.setUsuario(usuario);
        registro.setTokenHash(sha256(token));
        registro.setExpiracao(LocalDateTime.now().plusHours(EXPIRACAO_HORAS));
        tokenRepository.save(registro);

        emailService.enviarRecuperacaoSenha(
                usuario.getEmail(), frontUrl + "/verificar-email?token=" + token);
    }

    @Transactional
    public void verificar(String token) {
        EmailVerificationToken registro = tokenRepository.findByTokenHash(sha256(token.trim()))
                .orElseThrow(() -> new BusinessException("Token inválido ou expirado"));
        if (registro.expirado(LocalDateTime.now())) {
            tokenRepository.delete(registro);
            throw new BusinessException("Token inválido ou expirado");
        }
        Usuario usuario = registro.getUsuario();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
        tokenRepository.delete(registro);
    }

    private static String sha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
