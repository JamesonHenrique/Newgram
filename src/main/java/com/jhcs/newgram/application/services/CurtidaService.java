package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.curtida.CurtidaCreateDTO;
import com.jhcs.newgram.application.dtos.curtida.CurtidaResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.Comentario;
import com.jhcs.newgram.core.domain.entities.Curtida;
import com.jhcs.newgram.core.domain.entities.Post;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.ComentarioRepository;
import com.jhcs.newgram.core.domain.repositories.CurtidaRepository;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CurtidaService {

    @Autowired
    private CurtidaRepository curtidaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Transactional
    public CurtidaResponseDTO curtir(CurtidaCreateDTO dto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (dto.getPostId() != null && dto.getComentarioId() != null) {
            throw new BusinessException("Uma curtida deve ser associada a um post ou a um comentário, não ambos");
        }

        if (dto.getPostId() == null && dto.getComentarioId() == null) {
            throw new BusinessException("É necessário especificar um post ou um comentário para curtir");
        }

        Curtida curtida = new Curtida();
        curtida.setUsuario(usuario);
        curtida.setDataCriacao(LocalDateTime.now());

        boolean alvoEhPost = dto.getPostId() != null;
        if (alvoEhPost) {
            Post post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

            curtida.setPost(post);
        }
        else {
            Comentario comentario = comentarioRepository.findById(dto.getComentarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comentário não encontrado"));

            curtida.setComentario(comentario);
        }

        // Save direto (sem exists prévio): idempotência via constraint única + catch.
        try {
            curtida = curtidaRepository.saveAndFlush(curtida);
        } catch (DataIntegrityViolationException e) {
            if (alvoEhPost) {
                throw new BusinessException("Você já curtiu este post", e);
            }
            throw new BusinessException("Você já curtiu este comentário", e);
        }

        return converterParaResponseDTO(curtida);
    }

    @Transactional
    public void removerCurtida(Long postId, Long comentarioId, Long usuarioId) {
        if (postId != null) {
            if (!curtidaRepository.existsByUsuarioIdAndPostId(usuarioId, postId)) {
                throw new BusinessException("Você não curtiu este post");
            }
            curtidaRepository.deleteByUsuarioIdAndPostId(usuarioId, postId);
        } else if (comentarioId != null) {
            if (!curtidaRepository.existsByUsuarioIdAndComentarioId(usuarioId, comentarioId)) {
                throw new BusinessException("Você não curtiu este comentário");
            }
            curtidaRepository.deleteByUsuarioIdAndComentarioId(usuarioId, comentarioId);
        } else {
            throw new BusinessException("É necessário especificar um post ou um comentário");
        }
    }

    @Transactional(readOnly = true)
    public Page<CurtidaResponseDTO> listarCurtidasPorPost(Long postId, Pageable pageable) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post não encontrado");
        }

        Page<Curtida> curtidas = curtidaRepository.findByPostIdOrderByDataCriacaoDesc(postId, Support.safePage(pageable));
        return curtidas.map(this::converterParaResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<CurtidaResponseDTO> listarCurtidasPorComentario(Long comentarioId, Pageable pageable) {
        if (!comentarioRepository.existsById(comentarioId)) {
            throw new ResourceNotFoundException("Comentário não encontrado");
        }

        Page<Curtida> curtidas = curtidaRepository.findByComentarioIdOrderByDataCriacaoDesc(comentarioId, Support.safePage(pageable));
        return curtidas.map(this::converterParaResponseDTO);
    }

    @Transactional(readOnly = true)
    public boolean verificarCurtidaPost(Long postId, Long usuarioId) {
        return curtidaRepository.existsByUsuarioIdAndPostId(usuarioId, postId);
    }

    @Transactional(readOnly = true)
    public boolean verificarCurtidaComentario(Long comentarioId, Long usuarioId) {
        return curtidaRepository.existsByUsuarioIdAndComentarioId(usuarioId, comentarioId);
    }

    private CurtidaResponseDTO converterParaResponseDTO(Curtida curtida) {
        CurtidaResponseDTO dto = new CurtidaResponseDTO();
        dto.setId(curtida.getId());
        dto.setDataCriacao(curtida.getDataCriacao());

        if (curtida.getPost() != null) {
            dto.setPostId(curtida.getPost().getId());
        }

        if (curtida.getComentario() != null) {
            dto.setComentarioId(curtida.getComentario().getId());
        }

        UsuarioSummaryDTO usuarioDTO = new UsuarioSummaryDTO();
        usuarioDTO.setId(curtida.getUsuario().getId());
        usuarioDTO.setNome(curtida.getUsuario().getNome());
        usuarioDTO.setUsername(curtida.getUsuario().getUsername());


        dto.setUsuario(usuarioDTO);

        return dto;
    }
}
