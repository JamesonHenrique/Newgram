package com.jhcs.newgram.application.dtos.busca;

import com.jhcs.newgram.application.dtos.hashtag.HashtagSummaryDTO;
import com.jhcs.newgram.application.dtos.post.PostSummaryDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class BuscarResponseDTO {
    @Schema(description = "Usuários encontrados")
    private Page<UsuarioSummaryDTO> usuarios;

    @Schema(description = "Posts encontrados (respeita privacidade)")
    private Page<PostSummaryDTO> posts;

    @Schema(description = "Hashtags encontradas")
    private Page<HashtagSummaryDTO> hashtags;
}
