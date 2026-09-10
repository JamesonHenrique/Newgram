package com.jhcs.newgram.application.dtos.storie;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "DTO para criação de uma storie")
public class StorieCreateDTO {

    @Schema(description = "Indica se a storie deve ser destacada")
    private Boolean destacar;

    @Schema(description = "Imagem do Storie", type = "string", format = "binary")
    private MultipartFile imagem;
}
