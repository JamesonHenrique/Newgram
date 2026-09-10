package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.usuario.AnalyticsDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioComumDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioUpdateDTO;
import com.jhcs.newgram.application.services.ArquivoService;
import com.jhcs.newgram.application.services.UsuarioService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.TipoConta;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "API para gerenciamento de usuários")
public class UsuarioResource {
    private final UsuarioService usuarioService;

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os detalhes de um usuário específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioSummaryDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<UsuarioSummaryDTO> buscarUsuarioPorId(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        UsuarioSummaryDTO usuario = usuarioService.buscarUsuarioPorId(id, usuarioLogado.getId());
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Buscar usuário por username", description = "Retorna os detalhes de um usuário específico pelo username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorUsername(
            @Parameter(description = "Username do usuário", required = true)
            @PathVariable String username,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        UsuarioResponseDTO usuario = usuarioService.buscarUsuarioPorUsername(username, usuarioLogado.getId());
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar usuários", description = "Retorna usuários que correspondem ao termo de busca")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<UsuarioSummaryDTO>> buscarUsuarios(
            @Parameter(description = "Termo para busca de usuários", required = true)
            @RequestParam String termo,
            @AuthenticationPrincipal Usuario usuarioLogado,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10)")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<UsuarioSummaryDTO> usuarios = usuarioService.buscarUsuarios(termo, pageable, usuarioLogado.getId());
        return ResponseEntity.ok(usuarios);
    }
    @GetMapping("/sugestoes")
    @Operation(summary = "Sugestões de usuários", description = "Retorna sugestões de usuários para seguir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sugestões recuperadas com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<UsuarioSummaryDTO>> listarSugestoesUsuarios(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10)")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<UsuarioSummaryDTO> sugestoes = usuarioService.buscarSugestoesUsuarios(usuarioLogado.getId(), pageable);
        return ResponseEntity.ok(sugestoes);
    }
    @GetMapping("/famosos")
    @Operation(summary = "Usuários mais famosos", description = "Retorna uma lista dos usuários mais famosos com base no número de seguidores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários recuperados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<UsuarioSummaryDTO>> listarUsuariosMaisFamosos(
            @Parameter(description = "Parâmetros de paginação (page=0, size=10)")
            @AuthenticationPrincipal Usuario usuarioLogado,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<UsuarioSummaryDTO> famosos = usuarioService.buscarUsuariosMaisFamosos(usuarioLogado.getId(), pageable);
        return ResponseEntity.ok(famosos);
    }
    @GetMapping("/amigos-comum")
    @Operation(summary = "Buscar usuários por amigos em comum", description = "Retorna uma lista de usuários seguidos por amigos em comum")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários recuperados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<UsuarioComumDTO>> listarUsuariosPorAmigosEmComum(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10)")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<UsuarioComumDTO> usuarios = usuarioService.buscarUsuariosPorAmigosEmComum(usuarioLogado.getId(), pageable);
        return ResponseEntity.ok(usuarios);
    }
    @PutMapping(path = "/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente")

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Permissão negada",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long id,
            @Valid @ModelAttribute UsuarioUpdateDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        if (!usuarioLogado.getId().equals(id)) {
            throw new AccessDeniedException("Você só pode editar o próprio perfil");
        }

        UsuarioResponseDTO usuarioAtualizado = usuarioService.atualizarUsuario(id, dto);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @PatchMapping("/{id:\\d+}/privado")
    @Operation(summary = "Alternar conta privada", description = "Ativa ou desativa a privacidade da conta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Privacidade atualizada",
                    content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Só o próprio perfil",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<UsuarioResponseDTO> atualizarPrivado(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long id,
            @Parameter(description = "Conta privada?", required = true)
            @RequestParam boolean privado,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        return ResponseEntity.ok(usuarioService.atualizarPrivado(id, privado, usuarioLogado.getId()));
    }

    @GetMapping("/{id:\\d+}/seguidores")
    @Operation(summary = "Listar seguidores", description = "Retorna os usuários que seguem um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seguidores listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Page<UsuarioSummaryDTO>> listarSeguidores(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10)")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<UsuarioSummaryDTO> seguidores = usuarioService.buscarSeguidores(id, pageable, usuarioLogado.getId());
        return ResponseEntity.ok(seguidores);
    }

    @GetMapping("/{id:\\d+}/seguindo")
    @Operation(summary = "Listar seguidos", description = "Retorna os usuários que um usuário específico segue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seguidos listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Page<UsuarioSummaryDTO>> listarSeguidos(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10)")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<UsuarioSummaryDTO> seguidos = usuarioService.buscarSeguidos(id, pageable, usuarioLogado.getId());
        return ResponseEntity.ok(seguidos);
    }

    @PostMapping("/eu/solicitar-verificacao")
    @Operation(summary = "Solicitar selo de verificação", description = "Entra na fila de moderação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Solicitação registrada"),
            @ApiResponse(responseCode = "400", description = "Já verificada ou já solicitada",
                    content = @Content)
    })
    public ResponseEntity<Void> solicitarVerificacao(@AuthenticationPrincipal Usuario usuarioLogado) {

        usuarioService.solicitarVerificacao(usuarioLogado.getId());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/eu/exportar")
    @Operation(summary = "Exportar meus dados (LGPD)", description = "JSON com perfil, contagens e posts")
    public ResponseEntity<java.util.Map<String, Object>> exportarDados(
            @AuthenticationPrincipal Usuario usuarioLogado) {

        return ResponseEntity.ok(usuarioService.exportarDados(usuarioLogado.getId()));
    }

    @DeleteMapping("/eu")
    @Operation(summary = "Excluir minha conta (LGPD)", description = "Anonimiza a conta; ação irreversível")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta anonimizada"),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Void> excluirConta(@AuthenticationPrincipal Usuario usuarioLogado) {

        usuarioService.excluirConta(usuarioLogado.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/eu/analytics")
    @Operation(summary = "Analytics da conta", description = "Views, alcance e seguidores")
    public ResponseEntity<AnalyticsDTO> analytics(
            @AuthenticationPrincipal Usuario usuarioLogado) {

        return ResponseEntity.ok(usuarioService.analytics(usuarioLogado.getId()));
    }

    @PatchMapping("/eu/pix")
    @Operation(summary = "Definir chave Pix", description = "Vazia remove; exibida no perfil")
    public ResponseEntity<UsuarioResponseDTO> atualizarChavePix(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        return ResponseEntity.ok(usuarioService.atualizarChavePix(usuarioLogado.getId(), body.get("chavePix")));
    }

    @PatchMapping("/eu/tipo-conta")
    @Operation(summary = "Definir tipo de conta", description = "PESSOAL, CRIADOR ou NEGOCIOS")
    public ResponseEntity<UsuarioResponseDTO> atualizarTipoConta(
            @Parameter(description = "Tipo de conta", required = true)
            @RequestParam TipoConta tipoConta,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        return ResponseEntity.ok(usuarioService.atualizarTipoConta(usuarioLogado.getId(), tipoConta));
    }



}