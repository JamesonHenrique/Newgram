package com.jhcs.newgram.application.dtos.post;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.enums.TipoVisibilidade;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class PostResponseDTO {
    private Long id;
    private String legenda;
    private Date dataCriacao;
    private String localizacao;
    private TipoVisibilidade visibilidade;
    private boolean arquivado;
    private UsuarioSummaryDTO autor;
    private List<String> hashtags;
    private List<UsuarioSummaryDTO> usuariosMarcados;
    private Long numeroCurtidas;
    private Long numeroComentarios;
    private boolean curtidoPeloUsuario;
    private boolean salvoPeloUsuario;
}