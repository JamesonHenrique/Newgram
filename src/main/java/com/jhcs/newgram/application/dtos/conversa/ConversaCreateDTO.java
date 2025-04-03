package com.jhcs.newgram.application.dtos.conversa;

import lombok.Data;

import java.util.List;

@Data
public class ConversaCreateDTO {
    private String nome;
    private boolean isGrupo;
    private List<Long> participantesIds;
    private String descricao;
}