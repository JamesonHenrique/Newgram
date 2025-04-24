package com.jhcs.newgram.application.dtos.conversa;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ConversaCreateDTO {
    @Schema(description = "Nome da conversa", example = "Grupo de Estudos")
    private String nome;

    @Schema(description = "Indica se a conversa é um grupo", example = "true")
    private boolean isGrupo;

    @Schema(description = "Lista de IDs dos participantes", example = "[1, 2, 3]")
    private List<Long> participantesIds;

    @Schema(description = "Descrição da conversa", example = "Grupo para discutir assuntos de estudo.")
    private String descricao;
}
