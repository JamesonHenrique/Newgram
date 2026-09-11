package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.usuario.LoginDTO;
import com.jhcs.newgram.application.dtos.usuario.RecuperarSenhaDTO;
import com.jhcs.newgram.application.dtos.usuario.RedefinirSenhaDTO;
import com.jhcs.newgram.application.dtos.usuario.RefreshTokenDTO;
import com.jhcs.newgram.application.dtos.usuario.TokenDTO;
import com.jhcs.newgram.application.dtos.usuario.TwoFactorDTO;
import com.jhcs.newgram.application.dtos.usuario.TwoFactorLoginDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioCreateDTO;
import com.jhcs.newgram.application.services.AutenticacaoService;
import com.jhcs.newgram.application.services.RecuperacaoSenhaService;
import com.jhcs.newgram.application.services.VerificacaoEmailService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "API para autenticação e registro de usuários")
public class AutenticacaoResource {
    private final AutenticacaoService autenticacaoService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;
    private final VerificacaoEmailService verificacaoEmailService;
    private final JwtService jwtService;
    @PostMapping(path = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar usuário", description = "Registra um novo usuário com foto de perfil")
    public ResponseEntity<TokenDTO> registrar(
            @Parameter(description = "Dados do usuário a ser registrado", required = true)
            @Valid @ModelAttribute UsuarioCreateDTO dto) {

        TokenDTO tokenDTO = autenticacaoService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenDTO);
    }

    @PostMapping(path= "/login", produces = MediaType.APPLICATION_JSON_VALUE)
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
            @RequestBody @Valid LoginDTO loginDTO) {
        TokenDTO tokenDTO = autenticacaoService.autenticar(loginDTO.getEmail(), loginDTO.getSenha());
        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping(path="/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Renovar token", description = "Renova o token de acesso utilizando um refresh token válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso",
                    content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado",
                    content = @Content)
    })
    public ResponseEntity<TokenDTO> refreshToken(
            @Parameter(description = "Refresh token para renovação", required = true)
            @RequestBody @Valid RefreshTokenDTO dto) {
        TokenDTO tokenDTO = autenticacaoService.renovarToken(dto.getRefreshToken());
        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping(path = "/recuperar-senha")
    @Operation(summary = "Solicitar recuperação de senha",
            description = "Resposta sempre genérica para não revelar se o e-mail existe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Solicitação aceita"),
            @ApiResponse(responseCode = "400", description = "E-mail inválido",
                    content = @Content)
    })
    public ResponseEntity<Void> recuperarSenha(
            @Parameter(description = "E-mail da conta", required = true)
            @RequestBody @Valid RecuperarSenhaDTO dto) {
        recuperacaoSenhaService.solicitar(dto.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping(path = "/redefinir-senha")
    @Operation(summary = "Redefinir senha", description = "Troca a senha com token single-use de 1h")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha redefinida"),
            @ApiResponse(responseCode = "400", description = "Token inválido/expirado ou senhas divergentes",
                    content = @Content)
    })
    public ResponseEntity<Void> redefinirSenha(
            @Parameter(description = "Token e nova senha", required = true)
            @RequestBody @Valid RedefinirSenhaDTO dto) {
        recuperacaoSenhaService.redefinir(dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/2fa/verificar")
    @Operation(summary = "Concluir login com 2FA", description = "Segunda etapa: código do app autenticador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso",
                    content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "400", description = "Código inválido",
                    content = @Content)
    })
    public ResponseEntity<TokenDTO> verificarTwoFactor(
            @RequestBody @Valid TwoFactorLoginDTO dto) {
        return ResponseEntity.ok(autenticacaoService.verificarTwoFactor(dto));
    }

    @PostMapping(path = "/2fa/ativar")
    @Operation(summary = "Iniciar ativação do 2FA", description = "Retorna segredo + URI para o app autenticador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Segredo gerado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<java.util.Map<String, String>> ativarTwoFactor(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(autenticacaoService.iniciarAtivacaoTwoFactor(usuario.getId()));
    }

    @PostMapping(path = "/2fa/confirmar")
    @Operation(summary = "Confirmar ativação do 2FA", description = "Valida o código e ativa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "2FA ativo"),
            @ApiResponse(responseCode = "400", description = "Código inválido",
                    content = @Content)
    })
    public ResponseEntity<Void> confirmarTwoFactor(
            @RequestBody @Valid TwoFactorDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        autenticacaoService.confirmarAtivacaoTwoFactor(usuario.getId(), dto.getCodigo());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/2fa")
    @Operation(summary = "Desativar 2FA", description = "Remove a autenticação em dois fatores")
    public ResponseEntity<Void> desativarTwoFactor(@AuthenticationPrincipal Usuario usuario) {
        autenticacaoService.desativarTwoFactor(usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/verificar-email")
    @Operation(summary = "Verificar e-mail", description = "Confirma o token single-use de 24h")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "E-mail verificado"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado",
                    content = @Content)
    })
    public ResponseEntity<Void> verificarEmail(
            @Parameter(description = "Token recebido", required = true)
            @RequestParam String token) {
        verificacaoEmailService.verificar(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/verificar-email/reenviar")
    @Operation(summary = "Reenviar verificação", description = "Novo link de 24h para o autenticado")
    public ResponseEntity<Void> reenviarVerificacao(@AuthenticationPrincipal Usuario usuario) {
        verificacaoEmailService.enviarLink(usuario.getId());
        return ResponseEntity.accepted().build();
    }

    @PostMapping(path = "/logout")
    @Operation(summary = "Logout", description = "Revoga a sessão do refresh informado (idempotente)")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshTokenDTO dto) {
        jwtService.logout(dto == null ? null : dto.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/sessoes")
    @Operation(summary = "Listar sessões ativas", description = "Refresh tokens não revogados da conta")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> sessoes(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(jwtService.listarSessoes(usuario.getId()));
    }

    @DeleteMapping(path = "/sessoes/{jti}")
    @Operation(summary = "Revogar sessão", description = "Revoga um refresh específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sessão revogada"),
            @ApiResponse(responseCode = "401", description = "Sessão não encontrada",
                    content = @Content)
    })
    public ResponseEntity<Void> revogarSessao(
            @Parameter(description = "JTI da sessão", required = true)
            @PathVariable String jti,
            @AuthenticationPrincipal Usuario usuario) {
        jwtService.revogarSessao(jti, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/sessoes")
    @Operation(summary = "Encerrar outras sessões", description = "Revoga todos os refreshes da conta")
    public ResponseEntity<Void> revogarTodas(@AuthenticationPrincipal Usuario usuario) {
        jwtService.revogarTodasSessoes(usuario.getId());
        return ResponseEntity.noContent().build();
    }

}