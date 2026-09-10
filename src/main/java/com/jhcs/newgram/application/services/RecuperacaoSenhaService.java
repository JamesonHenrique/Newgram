package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.usuario.RedefinirSenhaDTO;
import com.jhcs.newgram.core.domain.entities.PasswordResetToken;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.PasswordResetTokenRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recuperação de senha com token opaco single-use (1h). Resposta sempre
 * genérica para não revelar se o e-mail existe (anti-enumeração).
 */
@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private static final int TOKEN_BYTES = 32;
    private static final int EXPIRACAO_HORAS = 1;
    private static final int MAX_SOLICITACOES_HORA = 3;

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.front-url:http://localhost:4200}")
    private String frontUrl;

    @Transactional
    public void solicitar(String email) {
        var usuarioOpt = usuarioRepository.findByEmail(email.trim().toLowerCase());
        // Genérico de propósito: mesma resposta exista ou não o e-mail.
        if (usuarioOpt.isEmpty()) {
            return;
        }
        Usuario usuario = usuarioOpt.get();

        // Rate-limit simples: 3 solicitações/hora por conta.
        long recentes = tokenRepository.countRecentesPorUsuario(
                usuario.getId(), LocalDateTime.now().minusHours(1));
        if (recentes >= MAX_SOLICITACOES_HORA) {
            return;
        }

        byte[] aleatorio = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(aleatorio);
        String token = HexFormat.of().formatHex(aleatorio);

        PasswordResetToken registro = new PasswordResetToken();
        registro.setUsuario(usuario);
        registro.setTokenHash(sha256(token));
        registro.setExpiracao(LocalDateTime.now().plusHours(EXPIRACAO_HORAS));
        tokenRepository.save(registro);

        emailService.enviarRecuperacaoSenha(
                usuario.getEmail(), frontUrl + "/redefinir-senha?token=" + token);
    }

    @Transactional
    public void redefinir(RedefinirSenhaDTO dto) {
        if (!dto.getNovaSenha().equals(dto.getConfirmacaoSenha())) {
            throw new BusinessException("As senhas não conferem");
        }
        PasswordResetToken registro = tokenRepository.findByTokenHash(sha256(dto.getToken().trim()))
                .orElseThrow(() -> new BusinessException("Token inválido ou expirado"));
        if (registro.expirado(LocalDateTime.now())) {
            throw new BusinessException("Token inválido ou expirado");
        }

        Usuario usuario = registro.getUsuario();
        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);

        registro.setUsado(true);
        tokenRepository.save(registro);
        // Invalida demais tokens pendentes da conta (single-use global).
        tokenRepository.invalidarTodosDoUsuario(usuario.getId());
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
