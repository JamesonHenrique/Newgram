package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioCreateDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioUpdateDTO;
import com.jhcs.newgram.core.domain.entities.Arquivo;
import com.jhcs.newgram.core.domain.entities.StatusUsuario;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.ArquivoRepository;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.repositories.StatusUsuarioRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SeguidorRepository seguidorRepository;

    @Autowired
    private ArquivoRepository arquivoRepository;

    @Autowired
    private StatusUsuarioRepository statusUsuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorId(Long id, Long usuarioLogadoId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        boolean seguindoUsuario = false;
        if (usuarioLogadoId != null) {
            seguindoUsuario = seguidorRepository.existsBySeguidorIdAndSeguidoId(usuarioLogadoId, id);
        }

        ArquivoDTO fotoPerfil = buscarFotoPerfil(id);
        UsuarioResponseDTO dto = converterParaUsuarioResponseDTO(usuario, fotoPerfil);
        dto.setSeguindoUsuario(seguindoUsuario);
        return dto;
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorUsername(String username, Long usuarioLogadoId) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        boolean seguindoUsuario = false;
        if (usuarioLogadoId != null) {
            seguindoUsuario = seguidorRepository.existsBySeguidorIdAndSeguidoId(usuarioLogadoId, usuario.getId());
        }

        ArquivoDTO fotoPerfil = buscarFotoPerfil(usuario.getId());
        UsuarioResponseDTO dto = converterParaUsuarioResponseDTO(usuario, fotoPerfil);
        dto.setSeguindoUsuario(seguindoUsuario);
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarUsuarios(String termo, Pageable pageable, Long usuarioLogadoId) {
        return usuarioRepository.buscarUsuarios(termo, pageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }

        if (dto.getBio() != null) {
            usuario.setBio(dto.getBio());
        }


        usuario = usuarioRepository.save(usuario);
        ArquivoDTO fotoPerfil = buscarFotoPerfil(id);
        return converterParaUsuarioResponseDTO(usuario, fotoPerfil);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSeguidores(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        return usuarioRepository.findSeguidoresByUsuarioId(usuarioId, pageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSeguidos(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        return usuarioRepository.findSeguidosByUsuarioId(usuarioId, pageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public List<UsuarioSummaryDTO> buscarSugestoesUsuarios(Long usuarioId, int limite) {
        // Obter IDs dos usuários que o usuário já segue
        List<Usuario> seguidos = seguidorRepository.findSeguidosByUsuarioId(usuarioId, null);
        List<Long> idsExcluidos = seguidos.stream().map(Usuario::getId).collect(Collectors.toList());

        // Buscar sugestões excluindo o próprio usuário e os já seguidos
        List<Usuario> sugestoes = usuarioRepository.findSugestoesUsuarios(usuarioId, idsExcluidos, limite);
        return sugestoes.stream()
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioId))
                .collect(Collectors.toList());
    }

    private ArquivoDTO buscarFotoPerfil(Long usuarioId) {
        List<Arquivo> arquivos = arquivoRepository.findByTipoEntidadeAndEntidadeIdAndTipoArquivo(
                Arquivo.TipoEntidadeRelacionada.PERFIL, usuarioId, "imagem");

        if (!arquivos.isEmpty()) {
            Arquivo arquivo = arquivos.get(0);
            ArquivoDTO dto = new ArquivoDTO();
            dto.setId(arquivo.getId());
            dto.setNomeOriginal(arquivo.getNomeOriginal());
            dto.setUrl(arquivo.getCaminho());
            dto.setContentType(arquivo.getContentType());
            return dto;
        }
        return null;
    }

    private UsuarioResponseDTO converterParaUsuarioResponseDTO(Usuario usuario, ArquivoDTO fotoPerfil) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setBio(usuario.getBio());
        dto.setFotoPerfil(fotoPerfil);
        dto.setDataCadastro(usuario.getDataCriacao());

        // Obter contagens
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId()));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId()));
        dto.setNumeroPosts((long) usuario.getPosts().size());

        return dto;
    }

    private UsuarioSummaryDTO converterParaUsuarioSummaryDTO(Usuario usuario, Long usuarioLogadoId) {
        UsuarioSummaryDTO dto = new UsuarioSummaryDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsername());


        // Verificar se o usuário logado segue este usuário
        boolean seguindoUsuario = false;
        if (usuarioLogadoId != null) {
            seguindoUsuario = seguidorRepository.existsBySeguidorIdAndSeguidoId(usuarioLogadoId, usuario.getId());
        }
        dto.setSeguindoUsuario(seguindoUsuario);

        // Buscar foto de perfil
        ArquivoDTO fotoPerfil = buscarFotoPerfil(usuario.getId());
        dto.setFotoPerfil(fotoPerfil);

        return dto;
    }
}