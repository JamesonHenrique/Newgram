package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UsuarioComumDTO {
    @Schema(description = "ID do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String nome;

    @Schema(description = "Nome de usuário único", example = "joaosilva")
    private String username;

    @Schema(description = "Número de seguidores em comum", example = "5")
    private int commonFollowers;
}
