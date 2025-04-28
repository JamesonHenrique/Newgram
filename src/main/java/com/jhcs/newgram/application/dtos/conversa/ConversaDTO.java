package com.jhcs.newgram.application.dtos.conversa;

import com.jhcs.newgram.application.dtos.mensagem.MensagemDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ConversaDTO {
    private Long id;
    private Date dataCriacao;
    private Date ultimaInteracao;
    private boolean isGrupo;
    private String nomeGrupo;
    private Long criadorId;
    private List<Long> participantesIds;
    private List<MensagemDTO> mensagens;
}