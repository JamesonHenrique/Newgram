package com.jhcs.newgram.application.dtos.seguidor;

import lombok.Data;

import java.util.Date;

@Data
public class SeguidorResponseDTO {
    private Long id;
    private Date dataCriacao;
    private boolean notificacoesAtivadas;
    private Long seguidorId;
    private String seguidorUsername;
    private String seguidorNome;
    private String seguidorFotoPerfil;
    private boolean seguidorVerificado;
    private Long seguidoId;
    private String seguidoUsername;
    private String seguidoNome;
    private String seguidoFotoPerfil;
    private boolean seguidoVerificado;
}