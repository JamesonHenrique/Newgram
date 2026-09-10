package com.jhcs.newgram.application.dtos.post;

import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;

import java.time.LocalDateTime;



@Data
public class PostSummaryDTO {
    private Long id;

    private LocalDateTime dataCriacao;
    private UsuarioSummaryDTO autor;
    private String localizacao;
    private String legenda;
    private Long numeroCurtidas;
    private Long numeroComentarios;
    private boolean salvoPeloUsuario;
    private boolean curtidoPeloUsuario;
    private String imagem;
}