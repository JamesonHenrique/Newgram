package com.jhcs.newgram.application.dtos.post;

import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;

import com.jhcs.newgram.application.dtos.enquete.PollResponseDTO;
import com.jhcs.newgram.core.domain.enums.TipoMidia;
import java.time.LocalDateTime;



@Data
public class PostSummaryDTO {
    private Long id;

    private LocalDateTime dataCriacao;
    private UsuarioSummaryDTO autor;
    private String localizacao;
    private TipoMidia tipoMidia;
    private PollResponseDTO enquete;
    private String legenda;
    private Long numeroCurtidas;
    private Long numeroComentarios;
    private boolean salvoPeloUsuario;
    private boolean curtidoPeloUsuario;
    private String imagem;
}