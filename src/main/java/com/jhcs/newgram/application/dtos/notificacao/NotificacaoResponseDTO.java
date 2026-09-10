package com.jhcs.newgram.application.dtos.notificacao;

import com.jhcs.newgram.core.domain.enums.TipoNotificacao;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Schema(description = "DTO para resposta de uma notificação")
public class  NotificacaoResponseDTO {
    private Long id;
    private TipoNotificacao tipo;
    private String conteudo;
    private LocalDateTime dataCriacao;
    private boolean lida;
    private Long destinatarioId;
    private Long remetenteId;
    private String nomeRemetente;
    private String usernameRemetente;
    private String fotoPerfilRemetente;
}