package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UsuarioUpdateDTO {
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String nome;

    @Schema(description = "Biografia do usuário", example = "Desenvolvedor Java")
    private String bio;
}
