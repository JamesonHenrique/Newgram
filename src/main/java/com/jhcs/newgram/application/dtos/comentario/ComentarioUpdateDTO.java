package com.jhcs.newgram.application.dtos.comentario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComentarioUpdateDTO {
    @Schema(description = "Novo texto do comentário", example = "Texto atualizado do comentário.")
    @NotBlank(message = "O texto do comentário é obrigatório")
    @Size(max = 1000, message = "O texto deve ter no máximo 1000 caracteres")
    private String texto;
}
