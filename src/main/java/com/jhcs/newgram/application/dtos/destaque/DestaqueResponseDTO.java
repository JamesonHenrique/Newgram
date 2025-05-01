package com.jhcs.newgram.application.dtos.destaque;

import io.swagger.v3.oas.annotations.media.Schema;
import com.jhcs.newgram.application.dtos.storie.StorieResponseDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DestaqueResponseDTO {
    @Schema(description = "ID do destaque", example = "1")
    private Long id;

    @Schema(description = "Nome do destaque", example = "Destaque Principal")
    private String nome;

    @Schema(description = "Data de criação do destaque", example = "2023-01-01T12:00:00Z")
    private Date dataCriacao;

    @Schema(description = "ID do usuário que criou o destaque", example = "10")
    private Long usuarioId;

    @Schema(description = "Nome de usuário do criador do destaque", example = "usuario123")
    private String usernameUsuario;

    @Schema(description = "Quantidade de stories associadas ao destaque", example = "5")
    private Integer quantidadeStories;

    @Schema(description = "Lista de stories associadas ao destaque")
    private List<StorieResponseDTO> stories;
    @Schema(description = "URL da foto de capa do destaque", example = "foto.jpg")
    private String destaqueFotoDeCapaUrl;
}
