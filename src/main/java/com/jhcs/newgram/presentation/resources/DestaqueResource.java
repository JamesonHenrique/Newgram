package com.jhcs.newgram.presentation.resources;


import com.jhcs.newgram.application.dtos.destaque.ContagemStoriesDTO;
import com.jhcs.newgram.application.dtos.destaque.DestaqueCreateDTO;
import com.jhcs.newgram.application.dtos.destaque.DestaqueResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/destaques")
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
    public ResponseEntity<Page<DestaqueResponseDTO>> listarDestaquesPorUsername(
            @Parameter(description = "Username do usuário", required = true)
            @PathVariable("username") String username,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        Page<DestaqueResponseDTO> destaques = destaqueService.listarDestaquesPorUsuario(username, pageable);
        return ResponseEntity.ok(destaques);
    }

    @GetMapping("/{id:\\d+}")
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

    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.APPLICATION_JSON_VALUE)
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

    @DeleteMapping("/{id:\\d+}")
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

    @GetMapping("/{id:\\d+}/stories")
    @Operation(summary = "Listar stories de um destaque", description = "Lista todas as stories presentes em um destaque específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stories listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Destaque não encontrado")
    })
    public ResponseEntity<Page<StorieResponseDTO>> listarStoriesPorDestaque(
            @Parameter(description = "ID do destaque", required = true) @PathVariable("id") Long id,
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        Page<StorieResponseDTO> stories =
                destaqueService.listarStoriesPorDestaque(id, usuarioAutenticado.getId(), pageable);
        return ResponseEntity.ok(stories);
    }

    @PutMapping(value = "/{id:\\d+}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

        DestaqueResponseDTO responseDTO =
                destaqueService.adicionarCapaDestaque(id, arquivo, usuarioAutenticado.getId());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id:\\d+}/contagem-stories")
    @Operation(summary = "Contar stories em um destaque", description = "Retorna o número de stories em um destaque específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Destaque não encontrado")
    })
    public ResponseEntity<ContagemStoriesDTO> contarStoriesPorDestaque(
            @Parameter(description = "ID do destaque", required = true) @PathVariable("id") Long id) {

        ContagemStoriesDTO dto = new ContagemStoriesDTO();
        dto.setQuantidade(destaqueService.contarStoriesPorDestaque(id));
        return ResponseEntity.ok(dto);
    }
}
