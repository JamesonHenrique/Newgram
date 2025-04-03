package com.jhcs.newgram.application.dtos.curtida;

import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;

import java.util.Date;

@Data
public class CurtidaResponseDTO {
    private Long id;
    private UsuarioSummaryDTO usuario;
    private Long postId;
    private Long comentarioId;
    private Date dataCriacao;
}