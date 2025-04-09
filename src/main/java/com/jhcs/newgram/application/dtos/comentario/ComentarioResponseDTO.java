package com.jhcs.newgram.application.dtos.comentario;

import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class ComentarioResponseDTO {
    private Long id;
    private String texto;
    private Date dataCriacao;
    private UsuarioSummaryDTO autor;
    private Long postId;
    private Long comentarioPaiId;
    private Long numeroCurtidas;
    private Long numeroRespostas;
    private Boolean curtidoPeloUsuario;
    private List<ComentarioResponseDTO> respostas;

}