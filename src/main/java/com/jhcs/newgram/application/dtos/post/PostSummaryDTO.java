package com.jhcs.newgram.application.dtos.post;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;

import java.util.Date;



@Data
public class PostSummaryDTO {
    private Long id;
    private ArquivoDTO imagemPrincipal; // Trocar String por ArquivoDTO
    private Date dataCriacao;
    private UsuarioSummaryDTO autor;
    private Long numeroCurtidas;
    private Long numeroComentarios;
}