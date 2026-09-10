package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorLoginDTO {
    @Schema(description = "E-mail da conta", example = "usuario@example.com")
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @Schema(description = "Código de 6 dígitos do app autenticador", example = "123456")
    @NotBlank(message = "Código é obrigatório")
    private String codigo;
}
