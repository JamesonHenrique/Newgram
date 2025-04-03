package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.usuario.LoginDTO;
import com.jhcs.newgram.application.dtos.usuario.TokenDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioCreateDTO;
import com.jhcs.newgram.application.services.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "API para autenticação e registro de usuários")
public class AutenticacaoResource {
    private final AutenticacaoService autentificaçãoService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria um novo usuário no sistema e retorna um token de autenticação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para registro",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Email ou nome de usuário já cadastrado",
                    content = @Content)
    })
    public ResponseEntity<TokenDTO> registrar(
            @Parameter(description = "Dados do usuário a ser registrado", required = true)
            @RequestBody UsuarioCreateDTO dto) {
        TokenDTO tokenDTO = autentificaçãoService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenDTO);
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Autentica o usuário com email e senha e retorna um token de acesso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso",
                    content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<TokenDTO> login(
            @Parameter(description = "Credenciais de login do usuário", required = true)
            @RequestBody LoginDTO loginDTO) {
        TokenDTO tokenDTO = autentificaçãoService.autenticar(loginDTO.getEmail(), loginDTO.getSenha());
        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Renovar token", description = "Renova o token de acesso utilizando um refresh token válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso",
                    content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado",
                    content = @Content)
    })
    public ResponseEntity<TokenDTO> refreshToken(
            @Parameter(description = "Refresh token para renovação", required = true)
            @RequestBody  String refreshToken) {
        TokenDTO tokenDTO = autentificaçãoService.renovarToken(refreshToken);
        return ResponseEntity.ok(tokenDTO);
    }
}