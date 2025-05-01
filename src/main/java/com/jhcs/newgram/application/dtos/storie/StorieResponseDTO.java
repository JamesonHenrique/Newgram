package com.jhcs.newgram.application.dtos.storie;

import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "DTO para resposta de uma storie")
public class StorieResponseDTO {

    @Schema(description = "ID da storie")
    private Long id;

    @Schema(description = "Data de criação da storie")
    private Date dataCriacao;

    @Schema(description = "Data de expiração da storie")
    private Date dataExpiracao;

    @Schema(description = "Informações do autor da storie")
    private UsuarioSummaryDTO autor;

    @Schema(description = "Número de visualizações da storie")
    private Long numeroVisualizacoes;

    @Schema(description = "Indica se a storie foi visualizada pelo usuário")
    private boolean visualizadoPeloUsuario;

    @Schema(description = "Indica se a storie está destacada")
    private boolean destacado;

    private String storieImagemUrl;
    private List<String> imagensUrls;
    private Long autorId;
    private String autorUsername;
}
