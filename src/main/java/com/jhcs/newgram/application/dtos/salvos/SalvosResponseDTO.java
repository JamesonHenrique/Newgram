package com.jhcs.newgram.application.dtos.salvos;

import lombok.Data;

import java.util.Date;

@Data
public class SalvosResponseDTO {
    private Long id;
    private Long postId;
    private Long usuarioId;
    private Date dataSalvo;
    private String colecao;
    private String imagemPostUrl;
    private String legendaPost;
}