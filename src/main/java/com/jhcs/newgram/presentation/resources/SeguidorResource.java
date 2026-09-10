package com.jhcs.newgram.presentation.resources;



import com.jhcs.newgram.application.dtos.seguidor.ContagemSeguidoresDTO;
import com.jhcs.newgram.application.dtos.seguidor.SeguidorResponseDTO;
import com.jhcs.newgram.application.dtos.seguidor.VerificarSeguimentoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.application.services.SeguidorService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Max;
import jakarta.validation.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seguidores")
@Validated
@Tag(name = "Seguidores", description = "Operações para gerenciamento de seguidores")
public class SeguidorResource {

    @Autowired
    private SeguidorService seguidorService;

    @GetMapping("/seguindo/{usuarioId:\\d+}")
    @Operation(summary = "Verificar seguimento", description = "Verifica se um usuário está seguindo outro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<VerificarSeguimentoDTO> verificarSeguimento(
            @Parameter(description = "ID do usuário que está sendo seguido", required = true)
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuario) {

        VerificarSeguimentoDTO dto = new VerificarSeguimentoDTO();
        dto.setSeguindo(seguidorService.verificarSeguimento(usuario.getId(), usuarioId));
        dto.setSolicitacaoPendente(seguidorService.verificarSolicitacaoPendente(usuario.getId(), usuarioId));
        return ResponseEntity.ok(dto);
    }
    }

    @GetMapping("/seguidores/{usuarioId:\\d+}")
    @Operation(summary = "Listar seguidores", description = "Retorna a lista de seguidores de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seguidores listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<Page<SeguidorResponseDTO>> listarSeguidores(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long usuarioId,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        Page<SeguidorResponseDTO> seguidores = seguidorService.listarSeguidores(usuarioId, pageable);
        return ResponseEntity.ok(seguidores);
    }

    @GetMapping("/seguidos/{usuarioId:\\d+}")
    @Operation(summary = "Listar seguidos", description = "Retorna a lista de usuários que um usuário segue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seguidos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<Page<SeguidorResponseDTO>> listarSeguidos(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long usuarioId,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        Page<SeguidorResponseDTO> seguidos = seguidorService.listarSeguidos(usuarioId, pageable);
        return ResponseEntity.ok(seguidos);
    }

    @GetMapping("/contagem/{usuarioId:\\d+}")
    @Operation(summary = "Contagem de seguidores e seguidos", description = "Retorna a quantidade de seguidores e seguidos de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<ContagemSeguidoresDTO> contarSeguidoresESeguidos(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long usuarioId) {

        ContagemSeguidoresDTO dto = new ContagemSeguidoresDTO();
        dto.setSeguidores(seguidorService.contarSeguidores(usuarioId));
        dto.setSeguidos(seguidorService.contarSeguidos(usuarioId));
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{usuarioId:\\d+}/seguir")
    @Operation(summary = "Seguir usuário", description = "Começa a seguir um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário seguido com sucesso",
                    content = @Content(schema = @Schema(implementation = SeguidorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Não é possível seguir a si mesmo ou já está seguindo",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<SeguidorResponseDTO> seguirUsuario(
            @Parameter(description = "ID do usuário a ser seguido", required = true)
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuario) {

        SeguidorResponseDTO relacao = seguidorService.seguir(usuario.getId(), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(relacao);
    }

    @DeleteMapping("/{usuarioId:\\d+}/deixar-de-seguir")
    @Operation(summary = "Deixar de seguir", description = "Deixa de seguir um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deixou de seguir com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não está seguindo este usuário",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> deixarDeSeguir(
            @Parameter(description = "ID do usuário que deixará de seguir", required = true)
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuario) {

        seguidorService.deixarDeSeguir(usuario.getId(), usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{usuarioId:\\d+}/notificacoes")
    @Operation(summary = "Alterar notificações", description = "Ativa ou desativa notificações de um seguido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notificações alteradas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não está seguindo este usuário",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> alterarNotificacoes(
            @Parameter(description = "ID do usuário seguido", required = true)
            @PathVariable Long usuarioId,
            @Parameter(description = "Estado das notificações", required = true)
            @RequestParam boolean ativar,
            @AuthenticationPrincipal Usuario usuario) {

        seguidorService.alterarNotificacoes(usuario.getId(), usuarioId, ativar);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/aleatorios")
    @Operation(summary = "Buscar seguidos aleatórios", description = "Retorna uma lista aleatória de usuários seguidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seguidos aleatórios encontrados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<List<UsuarioSummaryDTO>> buscarSeguidosAleatorios(
            @Parameter(description = "Quantidade de seguidos a retornar (1-50)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int limite,
            @AuthenticationPrincipal Usuario usuario) {

        List<UsuarioSummaryDTO> seguidosAleatorios = seguidorService.buscarSeguidosAleatorios(usuario.getId(), limite);
        return ResponseEntity.ok(seguidosAleatorios);
    }

    @GetMapping("/solicitacoes")
    @Operation(summary = "Listar solicitações de seguimento", description = "Solicitações pendentes para contas privadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitações listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<Page<SeguidorResponseDTO>> listarSolicitacoes(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        return ResponseEntity.ok(seguidorService.listarSolicitacoesRecebidas(usuario.getId(), pageable));
    }

    @GetMapping("/solicitacoes/contagem")
    @Operation(summary = "Contar solicitações pendentes", description = "Badge de solicitações de seguimento")
    public ResponseEntity<Map<String, Long>> contarSolicitacoes(
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(Map.of(
                "pendentes", seguidorService.contarSolicitacoesRecebidas(usuario.getId())));
    }

    @PostMapping("/solicitacoes/{id:\\d+}/aceitar")
    @Operation(summary = "Aceitar solicitação", description = "Aceita uma solicitação de seguimento pendente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação aceita",
                    content = @Content(schema = @Schema(implementation = SeguidorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Solicitação já respondida",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada",
                    content = @Content)
    })
    public ResponseEntity<SeguidorResponseDTO> aceitarSolicitacao(
            @Parameter(description = "ID da solicitação", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(seguidorService.aceitarSolicitacao(id, usuario.getId()));
    }

    @DeleteMapping("/solicitacoes/{id:\\d+}")
    @Operation(summary = "Rejeitar solicitação", description = "Rejeita uma solicitação de seguimento pendente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Solicitação rejeitada"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada",
                    content = @Content)
    })
    public ResponseEntity<Void> rejeitarSolicitacao(
            @Parameter(description = "ID da solicitação", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        seguidorService.rejeitarSolicitacao(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
