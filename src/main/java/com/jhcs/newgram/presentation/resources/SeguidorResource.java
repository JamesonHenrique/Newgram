package com.jhcs.newgram.presentation.resources;



import com.jhcs.newgram.application.dtos.seguidor.SeguidorResponseDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("seguidores")
@Tag(name = "Seguidores", description = "Operações para gerenciamento de seguidores")
public class SeguidorResource {

    @Autowired
    private SeguidorService seguidorService;

    @GetMapping("/seguindo/{usuarioId}")
    @Operation(summary = "Verificar seguimento", description = "Verifica se um usuário está seguindo outro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<Boolean> verificarSeguimento(
            @Parameter(description = "ID do usuário que está sendo seguido", required = true)
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuario) {

        boolean seguindo = seguidorService.verificarSeguimento(usuario.getId(), usuarioId);
        return ResponseEntity.ok(seguindo);
    }

    @GetMapping("/seguidores/{usuarioId}")
    @Operation(summary = "Listar seguidores", description = "Retorna a lista de seguidores de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seguidores listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<List<SeguidorResponseDTO>> listarSeguidores(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long usuarioId,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        List<SeguidorResponseDTO> seguidores = seguidorService.listarSeguidores(usuarioId, pageable);
        return ResponseEntity.ok(seguidores);
    }

    @GetMapping("/seguidos/{usuarioId}")
    @Operation(summary = "Listar seguidos", description = "Retorna a lista de usuários que um usuário segue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seguidos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<List<SeguidorResponseDTO>> listarSeguidos(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long usuarioId,
            @Parameter(description = "Parâmetros de paginação (page=0, size=20)")
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        List<SeguidorResponseDTO> seguidos = seguidorService.listarSeguidos(usuarioId, pageable);
        return ResponseEntity.ok(seguidos);
    }

    @GetMapping("/contagem/{usuarioId}")
    @Operation(summary = "Contagem de seguidores e seguidos", description = "Retorna a quantidade de seguidores e seguidos de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<Map<String, Long>> contarSeguidoresESeguidos(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long usuarioId) {

        Long seguidores = seguidorService.contarSeguidores(usuarioId);
        Long seguidos = seguidorService.contarSeguidos(usuarioId);

        return ResponseEntity.ok(Map.of(
                "seguidores", seguidores,
                "seguidos", seguidos
        ));
    }

    @PostMapping("/{usuarioId}/seguir")
    @Operation(summary = "Seguir usuário", description = "Começa a seguir um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário seguido com sucesso",
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
        return ResponseEntity.ok(relacao);
    }

    @DeleteMapping("/{usuarioId}/deixar-de-seguir")
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

    @PatchMapping("/{usuarioId}/notificacoes")
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
            @Parameter(description = "Quantidade de seguidos a retornar")
            @RequestParam(defaultValue = "5") int limite,
            @AuthenticationPrincipal Usuario usuario) {

        List<UsuarioSummaryDTO> seguidosAleatorios = seguidorService.buscarSeguidosAleatorios(usuario.getId(), limite);
        return ResponseEntity.ok(seguidosAleatorios);
    }
}