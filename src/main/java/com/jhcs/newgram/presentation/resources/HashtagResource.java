package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.hashtag.HashtagResponseDTO;
import com.jhcs.newgram.application.dtos.hashtag.HashtagSummaryDTO;
import com.jhcs.newgram.application.services.HashtagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hashtags")
@Tag(name = "Hashtags", description = "Operações relacionadas a hashtags")
public class HashtagResource {

    @Autowired
    private HashtagService hashtagService;

    @GetMapping("/{nome}")
    @Operation(summary = "Buscar hashtag por nome", description = "Busca uma hashtag específica pelo seu nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hashtag encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = HashtagResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Hashtag não encontrada")
    })
    public ResponseEntity<HashtagResponseDTO> buscarPorNome(
            @Parameter(description = "Nome da hashtag", required = true) @PathVariable("nome") String nome) {

        HashtagResponseDTO hashtag = hashtagService.buscarPorNome(nome);
        return ResponseEntity.ok(hashtag);
    }

    @GetMapping("/populares")
    @Operation(summary = "Listar hashtags populares", description = "Lista as hashtags mais utilizadas na plataforma")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hashtags populares listadas com sucesso")
    })
    public ResponseEntity<Page<HashtagSummaryDTO>> listarHashtagsPopulares(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<HashtagSummaryDTO> hashtags = hashtagService.listarHashtagsPopulares(pageable);
        return ResponseEntity.ok(hashtags);
    }

    @GetMapping("/sugestoes")
    @Operation(summary = "Sugerir hashtags", description = "Sugere hashtags com base em um termo de busca")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sugestões de hashtags listadas com sucesso")
    })
    public ResponseEntity<List<HashtagSummaryDTO>> sugerirHashtags(
            @Parameter(description = "Termo de busca", required = true) @RequestParam("termo") String termo,
            @Parameter(description = "Limite de resultados") @RequestParam(value = "limite", defaultValue = "5") int limite) {

        List<HashtagSummaryDTO> sugestoes = hashtagService.sugerirHashtags(termo, limite);
        return ResponseEntity.ok(sugestoes);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar hashtags", description = "Busca hashtags que contenham o termo especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hashtags listadas com sucesso")
    })
    public ResponseEntity<Page<HashtagSummaryDTO>> buscarHashtags(
            @Parameter(description = "Termo de busca", required = true) @RequestParam("termo") String termo,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<HashtagSummaryDTO> hashtags = hashtagService.buscarHashtags(termo, pageable);
        return ResponseEntity.ok(hashtags);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Listar hashtags por post", description = "Lista todas as hashtags associadas a um post específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hashtags do post listadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Post não encontrado")
    })
    public ResponseEntity<List<HashtagSummaryDTO>> listarHashtagsPorPostId(
            @Parameter(description = "ID do post", required = true) @PathVariable("postId") Long postId) {

        List<HashtagSummaryDTO> hashtags = hashtagService.listarHashtagsPorPostId(postId);
        return ResponseEntity.ok(hashtags);
    }
}