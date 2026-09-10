package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RedefinirSenhaDTO {
    @Schema(description = "Token recebido por e-mail")
    @NotBlank(message = "Token é obrigatório")
    private String token;

    @Schema(description = "Nova senha", example = "novaSenha123")
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    private String novaSenha;

    @Schema(description = "Confirmação da nova senha", example = "novaSenha123")
    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmacaoSenha;
}
