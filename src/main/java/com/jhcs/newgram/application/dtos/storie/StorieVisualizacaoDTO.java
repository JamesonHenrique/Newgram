package com.jhcs.newgram.application.dtos.storie;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "DTO para registrar a visualização de uma storie")
public class StorieVisualizacaoDTO {

    @Schema(description = "ID da storie visualizada")
    private Long storieId;

    @Schema(description = "ID do usuário que visualizou a storie")
    private Long usuarioId;
}
