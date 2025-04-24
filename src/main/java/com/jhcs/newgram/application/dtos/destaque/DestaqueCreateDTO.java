package com.jhcs.newgram.application.dtos.destaque;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DestaqueCreateDTO {
    @Schema(description = "Nome do destaque", example = "Destaque Principal")
    @NotBlank(message = "O nome do destaque é obrigatório")
    @Size(max = 50, message = "O nome do destaque deve ter no máximo 50 caracteres")
    private String nome;

    @Schema(description = "Lista de IDs das stories associadas ao destaque", example = "[1, 2, 3]")
    private List<Long> storiesIds;
}
