package com.jhcs.newgram.application.dtos.pesquisa;

import lombok.Data;

import java.util.Date;

@Data
public class PesquisaResponseDTO {
    private Long id;
    private String termoPesquisado;
    private Date dataPesquisa;
    private Long usuarioId;
}