package com.jhcs.newgram.application.dtos.hashtag;

import lombok.Data;

@Data
public class HashtagResponseDTO {
    private Long id;
    private String nome;
    private Long quantidadePosts;
}
