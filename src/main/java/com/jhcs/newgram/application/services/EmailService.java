package com.jhcs.newgram.application.services;

import java.util.Map;

/**
 * Envio de e-mails transacionais. Implementação padrão só registra em log
 * (desenvolvimento); troque por SMTP/SES em produção sem mudar callers.
 */
public interface EmailService {
    void enviarRecuperacaoSenha(String destinatario, String linkRecuperacao);
}
