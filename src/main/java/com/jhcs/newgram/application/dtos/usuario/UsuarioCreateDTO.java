package com.jhcs.newgram.application.dtos.usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioCreateDTO {
    private String nome;
    private String username;
    private String email;
    private String senha;
    private String bio;
    private String confirmacaoSenha;

    // Getters e Setters
}