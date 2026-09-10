package com.jhcs.newgram.application.dtos.comentario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ComentarioCreateDTO {
    @Schema(description = "Texto do comentário", example = "Este é um comentário.")
    private String texto;

    @Schema(description = "ID do post ao qual o comentário pertence", example = "123")
    private Long postId;

    @Schema(description = "ID do comentário pai, caso seja uma resposta", example = "456")
    private Long comentarioPaiId;
}
