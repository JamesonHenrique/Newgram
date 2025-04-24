package com.jhcs.newgram.application.dtos.destaque;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DestaqueUpdateDTO {
    @Schema(description = "Nome do destaque", example = "Destaque Atualizado")
    @Size(max = 50, message = "O nome do destaque deve ter no máximo 50 caracteres")
    private String nome;

    @Schema(description = "Descrição do destaque", example = "Descrição detalhada do destaque")
    private String descricao;

    @Schema(description = "Lista de IDs das stories para adicionar ao destaque", example = "[4, 5]")
    private List<Long> storiesParaAdicionar;

    @Schema(description = "Lista de IDs das stories para remover do destaque", example = "[1, 2]")
    private List<Long> storiesParaRemover;
}
