package com.jhcs.newgram.application.dtos.arquivo;

import lombok.Data;

@Data
public class ArquivoUploadResponseDTO {
    private Long id;
    private String url;
    private String tipo;
    private String contentType;
}