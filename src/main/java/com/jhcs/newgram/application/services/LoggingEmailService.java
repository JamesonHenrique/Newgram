package com.jhcs.newgram.application.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Desenvolvimento: link de recuperação vai para o log, não para SMTP. */
@Slf4j
@Service
public class LoggingEmailService implements EmailService {

    @Override
    public void enviarRecuperacaoSenha(String destinatario, String linkRecuperacao) {
        log.warn("RECUPERACAO SENHA para {}: {}", destinatario, linkRecuperacao);
    }
}
