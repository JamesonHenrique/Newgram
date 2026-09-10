package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.usuario.TokenDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioCreateDTO;
import com.jhcs.newgram.core.domain.entities.StatusUsuario;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.TipoArquivo;
import com.jhcs.newgram.core.domain.repositories.StatusUsuarioRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.security.JwtService;
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

        var token = jwtService.generateToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);

        return criarTokenDTO(token, refreshToken);
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
