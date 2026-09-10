package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.usuario.TokenDTO;
import com.jhcs.newgram.application.dtos.usuario.TwoFactorLoginDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioCreateDTO;
import com.jhcs.newgram.core.domain.entities.StatusUsuario;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.TipoArquivo;
import com.jhcs.newgram.core.domain.repositories.StatusUsuarioRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import com.jhcs.newgram.infrastructure.security.JwtService;
import com.jhcs.newgram.infrastructure.security.TotpService;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final AuthenticationManager authenticationManager;
    private final StatusUsuarioRepository statusUsuarioRepository;
    private final ArquivoService arquivoService;

    @Transactional
    public TokenDTO registrar(UsuarioCreateDTO dto) {
        if (!dto.getSenha().equals(dto.getConfirmacaoSenha())) {
            throw new BusinessException("As senhas não conferem");
        }

        // Upload ANTES do save: fora da transação de banco (S3 nao participa da TX).
        // Se o save falhar, o arquivo fica orfao no bucket (limpeza via lifecycle rule).
        String fotoPerfilPath = null;
        if (dto.getFotoPerfil() != null && !dto.getFotoPerfil().isEmpty()) {
            fotoPerfilPath = arquivoService.saveFile(dto.getFotoPerfil(), dto.getUsername(), TipoArquivo.FOTO_PERFIL);
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setBio(dto.getBio());
        usuario.setDataCriacao(LocalDateTime.now());
        usuario.setFotoPerfil(fotoPerfilPath);

        try {
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Email ou nome de usuário já cadastrado", e);
        }

        StatusUsuario statusUsuario = new StatusUsuario();
        statusUsuario.setUsuario(usuario);
        statusUsuario.setOnline(false);
        statusUsuario.setUltimoAcesso(LocalDateTime.now());
        statusUsuarioRepository.save(statusUsuario);

        var token = jwtService.generateToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);

        return criarTokenDTO(token, refreshToken);
    }

    public TokenDTO autenticar(String email, String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new BusinessException("Senha não pode estar vazia");
        }

        // BadCredentialsException (generico) vira 401 via GlobalExceptionHandler.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, senha));

        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // 2FA ativo: login parcial, front pede o código em seguida.
        if (usuario.isTwoFactorEnabled()) {
            TokenDTO parcial = new TokenDTO();
            parcial.setTwoFactorRequired(true);
            return parcial;
        }

        var token = jwtService.generateToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);

        return criarTokenDTO(token, refreshToken);
    }

    /** Segunda etapa do login com 2FA (senha já validada na etapa anterior). */
    public TokenDTO verificarTwoFactor(TwoFactorLoginDTO dto) {
        var usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        if (!usuario.isTwoFactorEnabled()) {
            throw new BusinessException("Conta sem autenticação em dois fatores");
        }
        if (!totpService.verificar(usuario.getTotpSecret(), dto.getCodigo())) {
            throw new BusinessException("Código inválido");
        }
        return criarTokenDTO(jwtService.generateToken(usuario), jwtService.generateRefreshToken(usuario));
    }

    /** Etapa 1 da ativação: gera segredo (só persiste após confirmação). */
    @Transactional
    public Map<String, String> iniciarAtivacaoTwoFactor(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (usuario.isTwoFactorEnabled()) {
            throw new BusinessException("2FA já está ativo");
        }
        String segredo = totpService.generarSegredo();
        usuario.setTotpSecret(segredo);
        usuarioRepository.save(usuario);
        return Map.of(
                "segredo", segredo,
                "uri", totpService.uriProvisionamento("Newgram", usuario.getEmail(), segredo));
    }

    /** Etapa 2: confirma o código e ativa. */
    @Transactional
    public void confirmarAtivacaoTwoFactor(Long usuarioId, String codigo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (usuario.getTotpSecret() == null
                || !totpService.verificar(usuario.getTotpSecret(), codigo)) {
            throw new BusinessException("Código inválido");
        }
        usuario.setTwoFactorEnabled(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desativarTwoFactor(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        usuario.setTwoFactorEnabled(false);
        usuario.setTotpSecret(null);
        usuarioRepository.save(usuario);
    }

    /** Rotaciona o par: refresh antigo invalidado, par novo emitido. */
    public TokenDTO renovarToken(String refreshToken) {
        Map<String, String> tokens = jwtService.refreshToken(refreshToken);
        return criarTokenDTO(tokens.get("accessToken"), tokens.get("refreshToken"));
    }

    private TokenDTO criarTokenDTO(String token, String refreshToken) {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        tokenDTO.setRefreshToken(refreshToken);
        return tokenDTO;
    }
}
