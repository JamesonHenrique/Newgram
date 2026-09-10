package com.jhcs.newgram.application.dtos.comentario;

import io.swagger.v3.oas.annotations.media.Schema;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComentarioResponseDTO {
    @Schema(description = "ID do comentário", example = "1")
    private Long id;

    @Schema(description = "Texto do comentário", example = "Este é um comentário.")
    private String texto;

    @Schema(description = "Data de criação do comentário", example = "2023-01-01T12:00:00Z")
    private LocalDateTime dataCriacao;

    @Schema(description = "Informações do autor do comentário")
    private UsuarioSummaryDTO autor;

    @Schema(description = "ID do post ao qual o comentário pertence", example = "123")
    private Long postId;

    @Schema(description = "ID do comentário pai, caso seja uma resposta", example = "456")
    private Long comentarioPaiId;

    @Schema(description = "Número de curtidas do comentário", example = "10")
    private Long numeroCurtidas;

    @Schema(description = "Número de respostas ao comentário", example = "5")
    private Long numeroRespostas;

    @Schema(description = "Indica se o comentário foi curtido pelo usuário atual", example = "true")
    private Boolean curtidoPeloUsuario;
}
