package com.jhcs.newgram.application.dtos.storie;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "DTO para criação de uma storie")
public class StorieCreateDTO {

    @Schema(description = "Arquivo de mídia da storie", required = true)
    private MultipartFile midia;

    @Schema(description = "Legenda da storie")
    private String legenda;

    @Schema(description = "Localização associada à storie")
    private String localizacao;

    @Schema(description = "Lista de IDs dos usuários marcados na storie")
    private List<Long> usuariosMarcados;

    @Schema(description = "Link associado à storie")
    private String link;

    @Schema(description = "Indica se a storie deve ser destacada")
    private boolean destacar;
}
