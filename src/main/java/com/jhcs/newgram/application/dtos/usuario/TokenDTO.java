package com.jhcs.newgram.application.dtos.usuario;

import lombok.Data;

@Data
public class TokenDTO {
    private String token;
    private String refreshToken;
    private Long userId;
}
