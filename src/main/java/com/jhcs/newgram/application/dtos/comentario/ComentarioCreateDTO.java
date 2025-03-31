package com.jhcs.newgram.application.dtos.comentario;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ComentarioCreateDTO {
    private String texto;
    private Long postId;
    private Long comentarioPaiId; // Opcional, para respostas a comentários
    private MultipartFile anexo; // Adicionado
}