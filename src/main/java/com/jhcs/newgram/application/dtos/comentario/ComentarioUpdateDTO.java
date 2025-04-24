package com.jhcs.newgram.application.dtos.comentario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ComentarioUpdateDTO {
    @Schema(description = "Novo texto do comentário", example = "Texto atualizado do comentário.")
    private String texto;
}
