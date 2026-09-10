package com.jhcs.newgram.application.dtos.mensagem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MensagemCreateDTO {
    @Schema(description = "Texto da mensagem", example = "Oi, tudo bem?")
    @NotBlank(message = "Texto é obrigatório")
    @Size(max = 1000, message = "Mensagem deve ter no máximo 1000 caracteres")
    private String texto;
}
