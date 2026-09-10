package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.usuario.UsuarioComumDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioUpdateDTO;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.StatusSeguimento;
import com.jhcs.newgram.core.domain.enums.TipoArquivo;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.aws.S3StorageService;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private PostRepository postRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private S3StorageService s3StorageService;


    @Transactional(readOnly = true)
    public UsuarioSummaryDTO buscarUsuarioPorId(Long id, Long usuarioLogadoId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        UsuarioSummaryDTO dto = converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId);
        dto.setSeguindoUsuario(segue(usuarioLogadoId, id));
        return dto;
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorUsername(String username, Long usuarioLogadoId) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        UsuarioResponseDTO dto = converterParaUsuarioResponseDTO(usuario, usuarioLogadoId);
        dto.setSeguindoUsuario(segue(usuarioLogadoId, usuario.getId()));
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarUsuarios(String termo, Pageable pageable, Long usuarioLogadoId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        return usuarioRepository.buscarUsuarios(termo, safePageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

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
        Pageable safePageable = Support.safePage(pageable, sort);
        return usuarioRepository.findSeguidoresByUsuarioId(usuarioId, StatusSeguimento.ACEITO, safePageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSeguidos(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        return usuarioRepository.findSeguidosByUsuarioId(usuarioId, StatusSeguimento.ACEITO, safePageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSugestoesUsuarios(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Usuario> sugestoes =
                usuarioRepository.findSugestoesUsuarios(usuarioId, StatusSeguimento.ACEITO, safePageable);
        return sugestoes.map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarUsuariosMaisFamosos(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Usuario> famosos =
                usuarioRepository.findUsuariosMaisFamosos(safePageable, usuarioId, StatusSeguimento.ACEITO);
        return famosos.map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioComumDTO> buscarUsuariosPorAmigosEmComum(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Object[]> results = usuarioRepository.findUsuariosPorAmigosEmComum(
                usuarioId, StatusSeguimento.ACEITO, safePageable);
        return results.map(result -> {
            Usuario usuario = (Usuario) result[0];
            Long commonFollowers = (Long) result[1];
            return converterParaUsuarioComumDTO(usuario, commonFollowers.intValue(), usuarioId);
        });
    }

    public void salvarFotoDePerfil(Long id, MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum usuario encontrado com o ID: " + id));

        var fotoPerfil = arquivoService.saveFile(file, usuario.getUsuarioName(), TipoArquivo.FOTO_PERFIL);
        usuario.setFotoPerfil(fotoPerfil);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizarPrivado(Long id, boolean privado, Long usuarioLogadoId) {
        Support.requireOwner(id, usuarioLogadoId, "Você só pode alterar a própria conta");
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        usuario.setPrivado(privado);
        return converterParaUsuarioResponseDTO(usuarioRepository.save(usuario), id);
    }

    private boolean segue(Long usuarioLogadoId, Long alvoId) {
        return usuarioLogadoId != null && seguidorRepository.existsBySeguidorIdAndSeguidoIdAndStatus(
                usuarioLogadoId, alvoId, StatusSeguimento.ACEITO);
    }

    private UsuarioComumDTO converterParaUsuarioComumDTO(Usuario usuario, int commonFollowers, Long usuarioLogadoId) {
        UsuarioComumDTO dto = new UsuarioComumDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsuarioName());
        dto.setCommonFollowers(commonFollowers);
        dto.setFotoPerfil(s3StorageService.getFileUrl(usuario.getFotoPerfil()));
        dto.setSeguindoUsuario(segue(usuarioLogadoId, usuario.getId()));
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
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId(), StatusSeguimento.ACEITO));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId(), StatusSeguimento.ACEITO));
        dto.setNumeroPosts(postRepository.countPostsByUsuarioId(usuario.getId()));
        dto.setSeguindoUsuario(segue(usuarioLogadoId, usuario.getId()));
        dto.setPrivado(usuario.isPrivado());
        return dto;
    }

    private UsuarioSummaryDTO converterParaUsuarioSummaryDTO(Usuario usuario, Long usuarioLogadoId) {
        UsuarioSummaryDTO dto = new UsuarioSummaryDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsuarioName());
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId(), StatusSeguimento.ACEITO));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId(), StatusSeguimento.ACEITO));
        dto.setNumeroPosts(postRepository.countPostsByUsuarioId(usuario.getId()));

        dto.setFotoPerfil(s3StorageService.getFileUrl(usuario.getFotoPerfil()));
        dto.setSeguindoUsuario(segue(usuarioLogadoId, usuario.getId()));
        dto.setPrivado(usuario.isPrivado());

        return dto;
    }
}
