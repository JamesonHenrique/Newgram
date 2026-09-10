package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UsuarioUpdateDTO {
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String nome;

    @Schema(description = "Nome de usuário único", example = "joaosilva")
    private String username;
    @Schema(description = "Biografia do usuário", example = "Desenvolvedor Java")
    private String bio;

    @Schema(description = "Foto de perfil do usuário", type = "string", format = "binary")
    private MultipartFile fotoPerfil;
}
