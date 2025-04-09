package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.usuario.UsuarioComumDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioUpdateDTO;
import com.jhcs.newgram.application.services.ArquivoService;
import com.jhcs.newgram.application.services.UsuarioService;
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
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "API para gerenciamento de usuários")
public class UsuarioResource {
    private final ArquivoService arquivoService;
    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os detalhes de um usuário específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorId(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        UsuarioResponseDTO usuario = usuarioService.buscarUsuarioPorId(id, usuarioLogado.getId());
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
    @PutMapping("/{id}")
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
            @Parameter(description = "Dados para atualização do usuário", required = true)
            @RequestBody @Valid UsuarioUpdateDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        if (!usuarioLogado.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }

        UsuarioResponseDTO usuarioAtualizado = usuarioService.atualizarUsuario(id, dto);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @GetMapping("/{id}/seguidores")
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

    @GetMapping("/{id}/seguindo")
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


    @PostMapping(value = "/foto-de-perfil/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Long> salvarFotoDePerfil(
            @PathVariable Long id,
            @Parameter(description = "Imagem da foto de perfil") @RequestPart("file") MultipartFile arquivo
    ) {
        usuarioService.salvarFotoDePerfil(id, arquivo);
        return ResponseEntity.accepted()
                .build();
    }

}