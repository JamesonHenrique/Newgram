package com.jhcs.newgram.application.dtos.arquivo;



import lombok.Data;

@Data
public class ArquivoDTO {
    private Long id;
    private String nomeOriginal;
    private String tipo;
    private Long tamanho;
    private String url;
    private String contentType;
}

