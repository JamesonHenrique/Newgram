package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.comentario.ComentarioCreateDTO;
import com.jhcs.newgram.application.dtos.comentario.ComentarioResponseDTO;
import com.jhcs.newgram.application.dtos.comentario.ComentarioUpdateDTO;
import com.jhcs.newgram.application.services.ComentarioService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comentarios")
@RequiredArgsConstructor
@Tag(name = "Comentários", description = "API para gerenciamento de comentários")
public class ComentarioResource {

    private final ComentarioService comentarioService;

    @PostMapping
    @Operation(summary = "Criar comentário", description = "Cria um novo comentário em um post ou como resposta a outro comentário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comentário criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ComentarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post ou comentário pai não encontrado",
                    content = @Content)
    })
    public ResponseEntity<ComentarioResponseDTO> criarComentario(
            @Parameter(description = "Dados do comentário a ser criado", required = true)
            @RequestBody @Valid ComentarioCreateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        ComentarioResponseDTO comentario = comentarioService.criarComentario(dto, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Atualizar comentário", description = "Atualiza o texto de um comentário existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comentário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ComentarioResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Permissão negada para editar o comentário",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<ComentarioResponseDTO> atualizarComentario(
            @Parameter(description = "ID do comentário a ser atualizado", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados para atualização do comentário", required = true)
            @RequestBody @Valid ComentarioUpdateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        ComentarioResponseDTO comentario = comentarioService.atualizarComentario(id, dto, usuario.getId());
        return ResponseEntity.ok(comentario);
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Excluir comentário", description = "Exclui um comentário existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comentário excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Permissão negada para excluir o comentário",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> excluirComentario(
            @Parameter(description = "ID do comentário a ser excluído", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        comentarioService.excluirComentario(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/post/{postId:\\d+}")
    @Operation(summary = "Listar comentários de um post", description = "Retorna os comentários principais de um post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comentários listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Page<ComentarioResponseDTO>> listarComentariosPorPost(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long postId,
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10)")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<ComentarioResponseDTO> comentarios = comentarioService.listarComentariosPorPost(postId, pageable, usuario.getId());
        return ResponseEntity.ok(comentarios);
    }

    @GetMapping("/{id:\\d+}/respostas")
    @Operation(summary = "Listar respostas de um comentário", description = "Retorna as respostas de um comentário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respostas listadas com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Page<ComentarioResponseDTO>> listarRespostasPorComentario(
            @Parameter(description = "ID do comentário", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10)")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        List<ComentarioResponseDTO> respostas = comentarioService.listarRespostasPorComentario(id, usuario.getId());
        int total = respostas.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<ComentarioResponseDTO> conteudo = start >= total ? List.of() : respostas.subList(start, end);
        return ResponseEntity.ok(new PageImpl<>(conteudo, pageable, total));
    }

    @PostMapping("/{id:\\d+}/curtir")
    @Operation(summary = "Curtir comentário", description = "Adiciona uma curtida a um comentário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comentário curtido com sucesso",
                    content = @Content(schema = @Schema(implementation = ComentarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Comentário já curtido pelo usuário",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<ComentarioResponseDTO> curtirComentario(
            @Parameter(description = "ID do comentário", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        ComentarioResponseDTO comentario = comentarioService.curtirComentario(id, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
    }

    @DeleteMapping("/{id:\\d+}/descurtir")
    @Operation(summary = "Remover curtida", description = "Remove a curtida de um comentário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Curtida removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Comentário não foi curtido pelo usuário",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> descurtirComentario(
            @Parameter(description = "ID do comentário", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        comentarioService.descurtirComentario(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}