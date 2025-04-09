package com.jhcs.newgram.application.dtos.destaque;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import lombok.Data;

import java.util.Date;

@Data
public class DestaqueSummaryDTO {
    private Long id;
    private String nome;
    private Date dataCriacao;
    private Integer quantidadeStories;

}