package com.jhcs.newgram.application.dtos.usuario;

import lombok.Data;

@Data
public class UsuarioComumDTO {
    private Long id;
    private String nome;
    private String username;
    private int commonFollowers;


}