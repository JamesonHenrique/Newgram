package com.jhcs.newgram.application.dtos.comentario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComentarioCreateDTO {
    @Schema(description = "Texto do comentário", example = "Este é um comentário.")
    @NotBlank(message = "O texto do comentário é obrigatório")
    @Size(max = 1000, message = "O texto deve ter no máximo 1000 caracteres")
    private String texto;

    @Schema(description = "ID do post ao qual o comentário pertence", example = "123")
    @NotNull(message = "O ID do post é obrigatório")
    private Long postId;

    @Schema(description = "ID do comentário pai, caso seja uma resposta", example = "456")
    private Long comentarioPaiId;
}
