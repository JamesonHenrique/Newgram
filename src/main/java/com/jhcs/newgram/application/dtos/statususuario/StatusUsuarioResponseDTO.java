package com.jhcs.newgram.application.dtos.statususuario;

import lombok.Data;

import java.util.Date;

@Data
public class StatusUsuarioResponseDTO {
    private Long id;
    private Long usuarioId;
    private boolean online;
    private Date ultimoAcesso;
    private String statusPersonalizado;
    private String username;
}