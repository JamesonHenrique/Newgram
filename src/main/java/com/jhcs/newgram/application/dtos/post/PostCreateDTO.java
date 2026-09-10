package com.jhcs.newgram.application.dtos.post;

import com.jhcs.newgram.core.domain.enums.TipoVisibilidade;
import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "DTO para criação de um post")
public class PostCreateDTO {

    @Schema(description = "Legenda do post (máximo de 2200 caracteres)")
    @Size(max = 2200, message = "A legenda deve ter no máximo 2200 caracteres")
    private String legenda;

    @Schema(description = "Localização associada ao post")
    @Size(max = 255, message = "Localização deve ter no máximo 255 caracteres")
    private String localizacao;

    @Schema(description = "Visibilidade do post (padrão: público)")
    private TipoVisibilidade visibilidade = TipoVisibilidade.PUBLICO;

    @Schema(description = "Lista de hashtags associadas ao post")
    @Size(max = 10, message = "Máximo de 10 hashtags por post")
    private List<String> hashtags;
}
