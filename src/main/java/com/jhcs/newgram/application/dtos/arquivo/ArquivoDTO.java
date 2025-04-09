package com.jhcs.newgram.application.dtos.arquivo;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArquivoDTO {
    private Long id;
    private String nomeOriginal;
    private String tipo;
    private Long tamanho;
    private String url;
    private String contentType;


    public ArquivoDTO(String url) {
        this.url = url;
    }
}