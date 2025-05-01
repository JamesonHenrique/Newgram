package com.jhcs.newgram.presentation.resources;


import com.jhcs.newgram.application.dtos.destaque.DestaqueCreateDTO;
import com.jhcs.newgram.application.dtos.destaque.DestaqueResponseDTO;
import com.jhcs.newgram.application.dtos.destaque.DestaqueSummaryDTO;
import com.jhcs.newgram.application.dtos.destaque.DestaqueUpdateDTO;
import com.jhcs.newgram.application.dtos.storie.StorieResponseDTO;
import com.jhcs.newgram.application.services.DestaqueService;
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
@RequestMapping("destaques")
@Tag(name = "Destaques", description = "Operações relacionadas a destaques de stories")
public class DestaqueResource {

    @Autowired
    private DestaqueService destaqueService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Criar um novo destaque", description = "Cria um novo destaque para agrupar stories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Destaque criado com sucesso",
                    content = @Content(schema = @Schema(implementation = DestaqueResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
    })
    public ResponseEntity<DestaqueResponseDTO> criarDestaque(
            @Valid @ModelAttribute DestaqueCreateDTO destaqueCreateDTO,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        DestaqueResponseDTO responseDTO = destaqueService.criarDestaque(destaqueCreateDTO, usuarioAutenticado.getId());
        responseDTO = destaqueService.buscarDestaquePorId(responseDTO.getId(), usuarioAutenticado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/usuario/{username}")
    @Operation(summary = "Listar destaques por username", description = "Lista todos os destaques criados por um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Destaques listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<List<DestaqueResponseDTO>> listarDestaquesPorUsername(
            @Parameter(description = "Username do usuário", required = true)
            @PathVariable("username") String username) {

        List<DestaqueResponseDTO> destaques = destaqueService.listarDestaquesPorUsuario(username);
        return ResponseEntity.ok(destaques);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar destaque por ID", description = "Busca um destaque específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Destaque encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Destaque não encontrado")
    })
    public ResponseEntity<DestaqueResponseDTO> buscarDestaquePorId(
            @Parameter(description = "ID do destaque", required = true) @PathVariable("id") Long id,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        DestaqueResponseDTO destaque = destaqueService.buscarDestaquePorId(id, usuarioAutenticado.getId());
        return ResponseEntity.ok(destaque);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualizar destaque", description = "Atualiza informações de um destaque existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Destaque atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Destaque não encontrado")
    })
    public ResponseEntity<DestaqueResponseDTO> atualizarDestaque(
            @Parameter(description = "ID do destaque", required = true) @PathVariable("id") Long id,
            @Valid @RequestBody DestaqueUpdateDTO destaqueUpdateDTO,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        DestaqueResponseDTO responseDTO = destaqueService.atualizarDestaque(id, destaqueUpdateDTO, usuarioAutenticado.getId());
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir destaque", description = "Remove um destaque existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Destaque excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Destaque não encontrado")
    })
    public ResponseEntity<Void> excluirDestaque(
            @Parameter(description = "ID do destaque", required = true) @PathVariable("id") Long id,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        destaqueService.excluirDestaque(id, usuarioAutenticado.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stories")
    @Operation(summary = "Listar stories de um destaque", description = "Lista todas as stories presentes em um destaque específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stories listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Destaque não encontrado")
    })
    public ResponseEntity<List<StorieResponseDTO>> listarStoriesPorDestaque(
            @Parameter(description = "ID do destaque", required = true) @PathVariable("id") Long id,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        List<StorieResponseDTO> stories = destaqueService.listarStoriesPorDestaque(id, usuarioAutenticado.getId());
        return ResponseEntity.ok(stories);
    }

    @PostMapping(value = "/{id}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Adicionar capa ao destaque", description = "Adiciona ou atualiza a imagem de capa do destaque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Capa adicionada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Destaque não encontrado")
    })
    public ResponseEntity<DestaqueResponseDTO> adicionarCapaDestaque(
            @Parameter(description = "ID do destaque", required = true) @PathVariable("id") Long id,
            @Parameter(description = "Arquivo de imagem da capa", required = true)
            @RequestParam("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        destaqueService.salvarCapaDeDestaque(id, usuarioAutenticado.getId(), arquivo);
        DestaqueResponseDTO responseDTO = destaqueService.buscarDestaquePorId(id, usuarioAutenticado.getId());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}/contagem-stories")
    @Operation(summary = "Contar stories em um destaque", description = "Retorna o número de stories em um destaque específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Destaque não encontrado")
    })
    public ResponseEntity<Long> contarStoriesPorDestaque(
            @Parameter(description = "ID do destaque", required = true) @PathVariable("id") Long id) {

        Long quantidade = destaqueService.contarStoriesPorDestaque(id);
        return ResponseEntity.ok(quantidade);
    }
}