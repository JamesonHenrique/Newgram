package com.jhcs.newgram.presentation.resources;


import com.jhcs.newgram.application.dtos.storie.StorieCreateDTO;
import com.jhcs.newgram.application.dtos.storie.StorieResponseDTO;
import com.jhcs.newgram.application.services.StorieService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("stories")
@Tag(name = "Stories", description = "Operações relacionadas a stories temporários")
public class StorieResource {

    @Autowired
    private StorieService storieService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Criar um novo storie", description = "Cria um novo storie com duração de 24 horas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Storie criado com sucesso",
                    content = @Content(schema = @Schema(implementation = StorieResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<StorieResponseDTO> criarStorie(
            @Valid @ModelAttribute StorieCreateDTO storieCreateDTO,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        StorieResponseDTO responseDTO = storieService.criarStorie(storieCreateDTO, usuarioAutenticado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar storie por ID", description = "Busca um storie específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Storie encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Storie não encontrado")
    })
    public ResponseEntity<StorieResponseDTO> buscarStoriePorId(
            @Parameter(description = "ID do storie", required = true) @PathVariable("id") Long id,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        StorieResponseDTO storie = storieService.buscarPorId(id, usuarioAutenticado.getId());
        return ResponseEntity.ok(storie);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir storie", description = "Remove um storie existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Storie excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Storie não encontrado")
    })
    public ResponseEntity<Void> excluirStorie(
            @Parameter(description = "ID do storie", required = true) @PathVariable("id") Long id,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        storieService.excluirStorie(id, usuarioAutenticado.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feed")
    @Operation(summary = "Listar stories do feed", description = "Lista todos os stories ativos de usuários seguidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stories listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<StorieResponseDTO>> listarStoriesDoFeed(
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        List<StorieResponseDTO> stories = storieService.listarStoriesDeSeguidosAtivos(usuarioAutenticado.getId());
        return ResponseEntity.ok(stories);
    }

    @GetMapping("/usuario/{autorId}")
    @Operation(summary = "Listar stories de um usuário", description = "Lista todos os stories ativos de um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stories listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<List<StorieResponseDTO>> listarStoriesDoUsuario(
            @Parameter(description = "ID do autor", required = true) @PathVariable("autorId") Long autorId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        List<StorieResponseDTO> stories = storieService.listarStoriesDoUsuario(autorId, usuarioAutenticado.getId());
        return ResponseEntity.ok(stories);
    }

    @PostMapping("/{id}/visualizar")
    @Operation(summary = "Marcar storie como visualizado", description = "Registra que o usuário autenticado visualizou o storie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Storie marcado como visualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Storie já visualizado pelo usuário"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Storie não encontrado")
    })
    public ResponseEntity<StorieResponseDTO> marcarComoVisualizado(
            @Parameter(description = "ID do storie", required = true) @PathVariable("id") Long id,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        StorieResponseDTO responseDTO = storieService.marcarComoVisualizado(id, usuarioAutenticado.getId());
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/{id}/destacar/{destaqueId}")
    @Operation(summary = "Destacar storie", description = "Adiciona o storie a um destaque específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Storie destacado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Storie ou destaque não encontrado")
    })
    public ResponseEntity<StorieResponseDTO> destacarStorie(
            @Parameter(description = "ID do storie", required = true) @PathVariable("id") Long id,
            @Parameter(description = "ID do destaque", required = true) @PathVariable("destaqueId") Long destaqueId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        StorieResponseDTO responseDTO = storieService.destacarStorie(id, destaqueId, usuarioAutenticado.getId());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/destacados")
    @Operation(summary = "Listar stories destacados", description = "Lista todos os stories destacados do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stories destacados listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<StorieResponseDTO>> listarStoriesDestacados(
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        List<StorieResponseDTO> stories = storieService.listarStoriesDestacados(usuarioAutenticado.getId());
        return ResponseEntity.ok(stories);
    }


    @PostMapping(value = "/{id}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Adicionar imagem ao storie", description = "Adiciona ou atualiza a imagem de um storie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagem adicionada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Storie não encontrado")
    })
    public ResponseEntity<StorieResponseDTO> adicionarImagemStorie(
            @Parameter(description = "ID do storie", required = true) @PathVariable("id") Long id,
            @Parameter(description = "Arquivo de imagem", required = true)
            @RequestParam("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        storieService.salvarImagem(id, usuarioAutenticado.getId(), arquivo);
        StorieResponseDTO responseDTO = storieService.buscarPorId(id, usuarioAutenticado.getId());
        return ResponseEntity.ok(responseDTO);
    }
}