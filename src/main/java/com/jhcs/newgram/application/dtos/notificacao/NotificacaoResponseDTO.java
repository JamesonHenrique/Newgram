package com.jhcs.newgram.application.dtos.notificacao;

import com.jhcs.newgram.core.domain.enums.TipoNotificacao;
import lombok.Data;

import java.util.Date;

@Data
public class NotificacaoResponseDTO {
    private Long id;
    private TipoNotificacao tipo;
    private String conteudo;
    private Date dataCriacao;
    private boolean lida;
    private Long destinatarioId;
    private Long remetenteId;
    private String nomeRemetente;
    private String usernameRemetente;
    private String fotoPerfilRemetente;
}