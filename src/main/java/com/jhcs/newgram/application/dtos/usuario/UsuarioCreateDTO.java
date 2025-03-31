package com.jhcs.newgram.application.dtos.usuario;
import lombok.Data;

@Data
public class UsuarioCreateDTO {
    private String nome;
    private String username;
    private String email;
    private String senha;
    private String confirmacaoSenha;

    // Getters e Setters
}