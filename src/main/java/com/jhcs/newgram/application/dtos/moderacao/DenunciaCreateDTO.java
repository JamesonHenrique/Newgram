package com.jhcs.newgram.application.dtos.moderacao;

import com.jhcs.newgram.core.domain.enums.AlvoDenuncia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DenunciaCreateDTO {
    @Schema(description = "Tipo do alvo", example = "POST")
    @NotNull(message = "Tipo do alvo é obrigatório")
    private AlvoDenuncia tipoAlvo;

    @Schema(description = "ID do alvo", example = "123")
    @NotNull(message = "ID do alvo é obrigatório")
    private Long alvoId;

    @Schema(description = "Motivo", example = "Spam")
    @NotBlank(message = "Motivo é obrigatório")
    @Size(max = 100, message = "Motivo deve ter no máximo 100 caracteres")
    private String motivo;

    @Schema(description = "Descrição opcional", example = "Detalhes do ocorrido")
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;
}
