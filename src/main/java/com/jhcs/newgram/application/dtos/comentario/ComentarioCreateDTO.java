package com.jhcs.newgram.application.dtos.comentario;

import lombok.Data;


@Data
public class ComentarioCreateDTO {
    private String texto;
    private Long postId;
    private Long comentarioPaiId;

}