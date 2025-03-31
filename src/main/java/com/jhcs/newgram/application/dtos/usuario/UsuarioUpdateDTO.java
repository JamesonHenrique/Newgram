package com.jhcs.newgram.application.dtos.usuario;

import lombok.Data;

@Data
public class UsuarioUpdateDTO {
    private String nome;
    private String bio;
    private String website;
    private String telefone;

    // Getters e Setters
}