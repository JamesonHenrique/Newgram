package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.arquivo.ArquivoUploadResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.TokenDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioCreateDTO;

import com.jhcs.newgram.application.dtos.usuario.UsuarioResponseDTO;
import com.jhcs.newgram.core.domain.entities.Arquivo;
import com.jhcs.newgram.core.domain.entities.StatusUsuario;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.repositories.StatusUsuarioRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.core.domain.utils.ArquivoUtils;
import com.jhcs.newgram.infrastructure.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.internal.util.FileUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final StatusUsuarioRepository statusUsuarioRepository;
    private final SeguidorRepository seguidorRepository;
    private final ArquivoService arquivoService;

    @Transactional
    public TokenDTO registrar(UsuarioCreateDTO dto) {
        if (!dto.getSenha().equals(dto.getConfirmacaoSenha())) {
            throw new RuntimeException("As senhas não conferem");
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Nome de usuário já utilizado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setBio(dto.getBio());
        usuario.setDataCriacao(new Date());

        usuarioRepository.save(usuario);
        StatusUsuario statusUsuario = new StatusUsuario();
        statusUsuario.setUsuario(usuario);
        statusUsuario.setOnline(false);
        statusUsuario.setUltimoAcesso(new Date());
        statusUsuarioRepository.save(statusUsuario);


        var token = jwtService.generateToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);
        TokenDTO tokenDTO = criarTokenDTO(token, refreshToken);
        tokenDTO.setUserId(usuario.getId());

        return tokenDTO;
    }

    public TokenDTO autenticar(String email, String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode estar vazia");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        senha
                )
        );

        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        var token = jwtService.generateToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);

        return criarTokenDTO(token, refreshToken);
    }

    public TokenDTO renovarToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);

        if (username != null) {
            var usuario = usuarioRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            if (jwtService.isTokenValid(refreshToken, usuario)) {
                var novoToken = jwtService.generateToken(usuario);

                return criarTokenDTO(novoToken, refreshToken);
            }
        }

        throw new RuntimeException("Token de atualização inválido ou expirado");
    }

    private TokenDTO criarTokenDTO(String token, String refreshToken) {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(token);
        tokenDTO.setRefreshToken(refreshToken);
        return tokenDTO;
    }

    private UsuarioResponseDTO converterParaUsuarioResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setBio(usuario.getBio());
        dto.setDataCadastro(usuario.getDataCriacao());
        dto.setFotoPerfilUrl("/api/usuarios/" + usuario.getId() + "/foto");
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId()));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId()));
        dto.setNumeroPosts((long) usuario.getPosts().size());

        return dto;
    }


}