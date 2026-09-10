package com.jhcs.newgram.application.dtos.curtida;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CurtidaCreateDTO {
    @Schema(description = "ID do post que foi curtido", example = "123")
    private Long postId;

    @Schema(description = "ID do comentário que foi curtido", example = "456")
    private Long comentarioId;
}
