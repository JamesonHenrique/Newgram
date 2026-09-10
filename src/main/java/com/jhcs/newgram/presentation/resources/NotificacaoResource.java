package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.notificacao.NotificacaoResponseDTO;
import com.jhcs.newgram.application.services.NotificacaoService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "API para notificações do usuário")
public class NotificacaoResource {

    private final NotificacaoService notificacaoService;

    @GetMapping
    @Operation(summary = "Listar notificações", description = "Notificações do usuário autenticado")
    public ResponseEntity<Page<NotificacaoResponseDTO>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        return ResponseEntity.ok(notificacaoService.listarNotificacoesPorUsuario(usuario.getId(), pageable));
    }

    @GetMapping("/nao-lidas")
    @Operation(summary = "Listar não lidas", description = "Notificações não visualizadas")
    public ResponseEntity<Page<NotificacaoResponseDTO>> listarNaoLidas(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        return ResponseEntity.ok(notificacaoService.listarNotificacoesNaoVisualizadas(usuario.getId(), pageable));
    }

    @GetMapping("/contagem")
    @Operation(summary = "Contar não lidas", description = "Badge de notificações")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(@AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(Map.of(
                "naoLidas", notificacaoService.contarNotificacoesNaoLidas(usuario.getId())));
    }

    @PatchMapping("/{id:\\d+}/visualizar")
    @Operation(summary = "Marcar como visualizada", description = "Marca uma notificação como lida")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Marcada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada",
                    content = @Content)
    })
    public ResponseEntity<Void> marcarComoVisualizada(
            @Parameter(description = "ID da notificação", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        notificacaoService.marcarComoVisualizada(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/visualizar-todas")
    @Operation(summary = "Marcar todas como visualizadas", description = "Marca todas como lidas")
    public ResponseEntity<Void> marcarTodas(@AuthenticationPrincipal Usuario usuario) {

        notificacaoService.marcarTodasComoVisualizadas(usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Excluir notificação", description = "Remove uma notificação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada",
                    content = @Content)
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da notificação", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        notificacaoService.deletarNotificacao(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
