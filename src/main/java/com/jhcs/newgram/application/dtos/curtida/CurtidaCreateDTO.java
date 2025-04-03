package com.jhcs.newgram.application.dtos.curtida;

import lombok.Data;

@Data
public class CurtidaCreateDTO {
    private Long postId;
    private Long comentarioId;
}