package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.mensagem.ConversaResponseDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemCreateDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemResponseDTO;
import com.jhcs.newgram.application.services.ConversaService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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

import java.util.Map;

@RestController
@RequestMapping("/conversas")
@RequiredArgsConstructor
@Tag(name = "Conversas", description = "API para mensagens diretas 1:1")
public class MensagemResource {

    private final ConversaService conversaService;

    @PostMapping("/iniciar/{usuarioId:\\d+}")
    @Operation(summary = "Iniciar ou obter conversa", description = "Abre a conversa 1:1 (idempotente por par)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conversa aberta"),
            @ApiResponse(responseCode = "400", description = "Conversa indisponível",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<ConversaResponseDTO> iniciar(
            @Parameter(description = "ID do outro usuário", required = true)
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversaService.iniciarOuObter(usuarioId, usuario.getId()));
    }

    @GetMapping
    @Operation(summary = "Listar conversas", description = "Conversas do usuário ordenadas por atividade")
    public ResponseEntity<Page<ConversaResponseDTO>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        return ResponseEntity.ok(conversaService.listarConversas(usuario.getId(), pageable));
    }

    @GetMapping("/{id:\\d+}/mensagens")
    @Operation(summary = "Listar mensagens", description = "Marca como lidas ao listar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensagens listadas"),
            @ApiResponse(responseCode = "400", description = "Fora da conversa",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Conversa não encontrada",
                    content = @Content)
    })
    public ResponseEntity<Page<MensagemResponseDTO>> mensagens(
            @Parameter(description = "ID da conversa", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=50)")
            @PageableDefault(page = 0, size = 50) Pageable pageable) {

        return ResponseEntity.ok(conversaService.listarMensagens(id, usuario.getId(), pageable));
    }

    @PostMapping("/{id:\\d+}/mensagens")
    @Operation(summary = "Enviar mensagem", description = "Envia texto (máx. 1000 caracteres)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mensagem enviada"),
            @ApiResponse(responseCode = "400", description = "Texto inválido ou fora da conversa",
                    content = @Content)
    })
    public ResponseEntity<MensagemResponseDTO> enviar(
            @Parameter(description = "ID da conversa", required = true)
            @PathVariable Long id,
            @RequestBody @Valid MensagemCreateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversaService.enviarMensagem(id, dto, usuario.getId()));
    }

    @GetMapping("/nao-lidas/contagem")
    @Operation(summary = "Contar não lidas", description = "Badge de mensagens")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(@AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(Map.of("naoLidas", conversaService.contarNaoLidas(usuario.getId())));
    }
}
