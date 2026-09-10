package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateDTO {
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Schema(description = "Nome de usuário único", example = "joaosilva")
    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 30, message = "Username deve ter entre 3 e 30 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username permite apenas letras, números, ponto, underline e hífen")
    private String username;

    @Schema(description = "E-mail do usuário", example = "joao@example.com")
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @Schema(description = "Senha do usuário", example = "senha123")
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    private String senha;

    @Schema(description = "Confirmação da senha", example = "senha123")
    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmacaoSenha;

    @Schema(description = "Biografia do usuário", example = "Desenvolvedor Java")
    @Size(max = 500, message = "Bio deve ter no máximo 500 caracteres")
    private String bio;

    @Schema(description = "Foto de perfil do usuário", type = "string", format = "binary")
    private MultipartFile fotoPerfil;
}
