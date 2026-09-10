package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UsuarioUpdateDTO {
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Schema(description = "Nome de usuário único", example = "joaosilva")
    @Size(min = 3, max = 30, message = "Username deve ter entre 3 e 30 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username permite apenas letras, números, ponto, underline e hífen")
    private String username;

    @Schema(description = "Biografia do usuário", example = "Desenvolvedor Java")
    @Size(max = 500, message = "Bio deve ter no máximo 500 caracteres")
    private String bio;

    @Schema(description = "Foto de perfil do usuário", type = "string", format = "binary")
    private MultipartFile fotoPerfil;
}
