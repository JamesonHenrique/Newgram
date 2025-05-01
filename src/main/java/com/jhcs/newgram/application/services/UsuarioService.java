package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.usuario.UsuarioComumDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioUpdateDTO;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.TipoArquivo;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.repositories.StatusUsuarioRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.aws.S3StorageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SeguidorRepository seguidorRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private StatusUsuarioRepository statusUsuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private S3StorageService s3StorageService;


    @Transactional(readOnly = true)
    public UsuarioSummaryDTO buscarUsuarioPorId(Long id, Long usuarioLogadoId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        boolean seguindoUsuario = false;
        if (usuarioLogadoId != null) {
            seguindoUsuario = seguidorRepository.existsBySeguidorIdAndSeguidoId(usuarioLogadoId, id);
        }
        UsuarioSummaryDTO dto = converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId);
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


        UsuarioResponseDTO dto = converterParaUsuarioResponseDTO(usuario, usuarioLogadoId);
        dto.setSeguindoUsuario(seguindoUsuario);
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarUsuarios(String termo, Pageable pageable, Long usuarioLogadoId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );
        return usuarioRepository.buscarUsuarios(termo, safePageable)
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
        if (dto.getFotoPerfil() != null && !dto.getFotoPerfil().isEmpty()) {
            salvarFotoDePerfil(usuario.getId(), dto.getFotoPerfil());
        }


        usuario = usuarioRepository.save(usuario);
        return converterParaUsuarioResponseDTO(usuario, usuario.getId());
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSeguidores(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );
        return usuarioRepository.findSeguidoresByUsuarioId(usuarioId, safePageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSeguidos(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );
        return usuarioRepository.findSeguidosByUsuarioId(usuarioId, safePageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSugestoesUsuarios(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );
        Page<Usuario> sugestoes = usuarioRepository.findSugestoesUsuarios(usuarioId, safePageable);
        return sugestoes.map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarUsuariosMaisFamosos(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );
        Page<Usuario> famosos = usuarioRepository.findUsuariosMaisFamosos(safePageable, usuarioId);
        return famosos.map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioId));
    }



    @Transactional(readOnly = true)
    public Page<UsuarioComumDTO> buscarUsuariosPorAmigosEmComum(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );
        Page<Object[]> results = usuarioRepository.findUsuariosPorAmigosEmComum(usuarioId, safePageable);
        return results.map(result -> {
            Usuario usuario = (Usuario) result[0];
            Long commonFollowers = (Long) result[1];
            return converterParaUsuarioComumDTO(usuario, commonFollowers.intValue(), usuarioId);
        });
    }

    public void salvarFotoDePerfil(Long id, MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum usuario encontrado com o ID: " + id));

        var fotoPerfil = arquivoService.saveFile(file, usuario.getUsuarioName(), TipoArquivo.FOTO_PERFIL);
        usuario.setFotoPerfil(fotoPerfil);
        usuarioRepository.save(usuario);
    }

    private UsuarioComumDTO converterParaUsuarioComumDTO(Usuario usuario, int commonFollowers, Long usuarioLogadoId) {
        UsuarioComumDTO dto = new UsuarioComumDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsuarioName());
        dto.setCommonFollowers(commonFollowers);
        dto.setFotoPerfil(s3StorageService.getFileUrl(usuario.getFotoPerfil()));
        boolean seguindoUsuario = false;
        if (usuario.getId() != null) {
            seguindoUsuario = seguidorRepository.existsBySeguidorIdAndSeguidoId(usuarioLogadoId, usuario.getId());
        }
        dto.setSeguindoUsuario(seguindoUsuario);
        return dto;
    }

    private UsuarioResponseDTO converterParaUsuarioResponseDTO(Usuario usuario, Long usuarioLogadoId) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsuarioName());
        dto.setEmail(usuario.getEmail());
        dto.setBio(usuario.getBio());

        dto.setDataCadastro(usuario.getDataCriacao());

        dto.setFotoPerfil(s3StorageService.getFileUrl(usuario.getFotoPerfil()));
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId()));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId()));
        dto.setNumeroPosts((long) usuario.getPosts().size());
        boolean seguindoUsuario = false;
        if (usuarioLogadoId != null) {
            seguindoUsuario = seguidorRepository.existsBySeguidorIdAndSeguidoId(usuarioLogadoId, usuario.getId());
        }
        dto.setSeguindoUsuario(seguindoUsuario);
        return dto;
    }

    private UsuarioSummaryDTO converterParaUsuarioSummaryDTO(Usuario usuario, Long usuarioLogadoId) {
        UsuarioSummaryDTO dto = new UsuarioSummaryDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsuarioName());
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId()));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId()));
        dto.setNumeroPosts((long) usuario.getPosts().size());

        dto.setFotoPerfil(s3StorageService.getFileUrl(usuario.getFotoPerfil()));
        boolean seguindoUsuario = false;
        if (usuarioLogadoId != null) {
            seguindoUsuario = seguidorRepository.existsBySeguidorIdAndSeguidoId(usuarioLogadoId, usuario.getId());
        }
        dto.setSeguindoUsuario(seguindoUsuario);


        return dto;
    }


}