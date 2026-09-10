package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.moderacao.BloqueioResponseDTO;
import com.jhcs.newgram.application.dtos.moderacao.BloqueioStatusDTO;
import com.jhcs.newgram.application.dtos.moderacao.DenunciaCreateDTO;
import com.jhcs.newgram.application.dtos.moderacao.DenunciaResponseDTO;
import com.jhcs.newgram.application.services.ModeracaoService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/moderacao")
@RequiredArgsConstructor
@Tag(name = "Moderação", description = "API para denúncias e bloqueios")
public class ModeracaoResource {

    private final ModeracaoService moderacaoService;

    @PostMapping("/denuncias")
    @Operation(summary = "Denunciar conteúdo", description = "Abre uma denúncia (idempotente por autor+alvo)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Denúncia registrada",
                    content = @Content(schema = @Schema(implementation = DenunciaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Denúncia duplicada ou alvo inexistente",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Alvo não encontrado",
                    content = @Content)
    })
    public ResponseEntity<DenunciaResponseDTO> denunciar(
            @RequestBody @Valid DenunciaCreateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(moderacaoService.denunciar(dto, usuario.getId()));
    }

    @GetMapping("/denuncias/minhas")
    @Operation(summary = "Minhas denúncias", description = "Denúncias feitas pelo usuário autenticado")
    public ResponseEntity<Page<DenunciaResponseDTO>> minhasDenuncias(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        return ResponseEntity.ok(moderacaoService.listarMinhasDenuncias(usuario.getId(), pageable));
    }

    @PostMapping("/bloqueios/{usuarioId:\\d+}")
    @Operation(summary = "Bloquear usuário", description = "Bloqueia e rompe o vínculo nos dois sentidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário bloqueado"),
            @ApiResponse(responseCode = "400", description = "Bloqueio inválido ou duplicado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> bloquear(
            @Parameter(description = "ID do usuário a bloquear", required = true)
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuario) {

        moderacaoService.bloquear(usuario.getId(), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/bloqueios/{usuarioId:\\d+}")
    @Operation(summary = "Desbloquear usuário", description = "Remove o bloqueio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Desbloqueado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Usuário não está bloqueado",
                    content = @Content)
    })
    public ResponseEntity<Void> desbloquear(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuario) {

        moderacaoService.desbloquear(usuario.getId(), usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bloqueios")
    @Operation(summary = "Listar bloqueios", description = "Usuários bloqueados pelo autenticado")
    public ResponseEntity<Page<BloqueioResponseDTO>> listarBloqueios(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        return ResponseEntity.ok(moderacaoService.listarBloqueios(usuario.getId(), pageable));
    }

    @GetMapping("/bloqueios/verificar/{usuarioId:\\d+}")
    @Operation(summary = "Verificar bloqueio", description = "Diz se o autenticado bloqueou o usuário")
    public ResponseEntity<BloqueioStatusDTO> verificar(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuario) {

        BloqueioStatusDTO dto = new BloqueioStatusDTO();
        dto.setBloqueado(moderacaoService.bloqueadoPor(usuario.getId(), usuarioId));
        return ResponseEntity.ok(dto);
    }
}
