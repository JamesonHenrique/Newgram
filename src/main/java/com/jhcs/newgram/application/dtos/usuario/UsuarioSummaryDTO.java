package com.jhcs.newgram.application.dtos.usuario;
import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import lombok.Data;

@Data
public class UsuarioSummaryDTO {
    private Long id;
    private String nome;
    private String username;
    private ArquivoDTO fotoPerfil;
    private boolean seguindoUsuario;

    // Getters e Setters
}