package com.jhcs.newgram.application.dtos.post;

import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.enums.TipoVisibilidade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "DTO para resposta de um post")
public class PostResponseDTO {

    @Schema(description = "ID do post")
    private Long id;

    @Schema(description = "Legenda do post")
    private String legenda;

    @Schema(description = "Data de criação do post")
    private Date dataCriacao;

    @Schema(description = "Localização associada ao post")
    private String localizacao;

    @Schema(description = "Visibilidade do post")
    private TipoVisibilidade visibilidade;

    @Schema(description = "Indica se o post está arquivado")
    private boolean arquivado;

    @Schema(description = "Informações do autor do post")
    private UsuarioSummaryDTO autor;

    @Schema(description = "Lista de hashtags associadas ao post")
    private List<String> hashtags;

    @Schema(description = "Lista de usuários marcados no post")
    private List<UsuarioSummaryDTO> usuariosMarcados;

    @Schema(description = "Número de curtidas no post")
    private Long numeroCurtidas;

    @Schema(description = "Número de comentários no post")
    private Long numeroComentarios;

    @Schema(description = "Indica se o post foi curtido pelo usuário")
    private boolean curtidoPeloUsuario;

    @Schema(description = "Indica se o post foi salvo pelo usuário")
    private boolean salvoPeloUsuario;
}
