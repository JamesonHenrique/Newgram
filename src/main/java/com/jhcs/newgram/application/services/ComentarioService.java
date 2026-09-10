package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.comentario.ComentarioCreateDTO;
import com.jhcs.newgram.application.dtos.comentario.ComentarioResponseDTO;
import com.jhcs.newgram.application.dtos.comentario.ComentarioUpdateDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.Comentario;
import com.jhcs.newgram.core.domain.entities.Curtida;
import com.jhcs.newgram.core.domain.entities.Post;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.ComentarioRepository;
import com.jhcs.newgram.core.domain.repositories.CurtidaRepository;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.aws.S3StorageService;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CurtidaRepository curtidaRepository;
    @Autowired
    private S3StorageService s3StorageService;

    @Transactional
    public ComentarioResponseDTO criarComentario(ComentarioCreateDTO dto, Long usuarioId) {
        Usuario autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto());
        comentario.setAutor(autor);
        comentario.setPost(post);
        comentario.setDataCriacao(LocalDateTime.now());

        if (dto.getComentarioPaiId() != null) {
            Comentario comentarioPai = comentarioRepository.findById(dto.getComentarioPaiId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comentário pai não encontrado"));

            if (!comentarioPai.getPost().getId().equals(post.getId())) {
                throw new BusinessException("O comentário pai não pertence ao post informado");
            }

            comentario.setComentarioPai(comentarioPai);
        }

        comentario = comentarioRepository.save(comentario);

        // Criar notificação para o autor do post se não for o próprio usuário
        // if (!post.getAutor().getId().equals(usuarioId)) {
        //     notificacaoService.criarNotificacaoComentario(autor, post);
        // }

        // Se for resposta, notificar o autor do comentário pai
        // if (dto.getComentarioPaiId() != null && !comentario.getComentarioPai().getAutor().getId().equals(usuarioId)) {
        //     notificacaoService.criarNotificacaoRespostaComentario(autor, comentario.getComentarioPai());
        // }

        return converterParaResponseDTO(comentario, usuarioId);
    }

    @Transactional
    public ComentarioResponseDTO atualizarComentario(Long comentarioId, ComentarioUpdateDTO dto, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário não encontrado"));

        Support.requireOwner(comentario.getAutor().getId(), usuarioId, "Você não tem permissão para editar este comentário");

        comentario.setTexto(dto.getTexto());
        comentario = comentarioRepository.save(comentario);

        return converterParaResponseDTO(comentario, usuarioId);
    }

    @Transactional
    public void excluirComentario(Long comentarioId, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário não encontrado"));

        if (!comentario.getAutor().getId().equals(usuarioId) &&
                !comentario.getPost().getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para excluir este comentário");
        }

        comentarioRepository.delete(comentario);
    }

    @Transactional(readOnly = true)

    public Page<ComentarioResponseDTO> listarComentariosPorPost(Long postId, Pageable pageable, Long usuarioId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post não encontrado");
        }

        Page<Comentario> comentarios = comentarioRepository.findComentariosPrincipaisByPostId(postId, safePageable);

        return comentarios.map(comentario -> converterParaResponseDTO(comentario, usuarioId));
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> listarRespostasPorComentario(Long comentarioId, Long usuarioId) {
        if (!comentarioRepository.existsById(comentarioId)) {
            throw new ResourceNotFoundException("Comentário não encontrado");
        }

        List<Comentario> respostas = comentarioRepository.findByComentarioPaiIdOrderByDataCriacaoAsc(comentarioId);

        return respostas.stream()
                .map(resposta -> converterParaResponseDTO(resposta, usuarioId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ComentarioResponseDTO> listarRespostasPorComentario(Long comentarioId, Pageable pageable, Long usuarioId) {
        List<ComentarioResponseDTO> todas = listarRespostasPorComentario(comentarioId, usuarioId);
        return Support.pageOf(todas, pageable);
    }

    @Transactional
    public ComentarioResponseDTO curtirComentario(Long comentarioId, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Curtida curtida = new Curtida();
        curtida.setUsuario(usuario);
        curtida.setComentario(comentario);
        curtida.setDataCriacao(LocalDateTime.now());

        // Save direto (sem exists prévio): idempotência via constraint única + catch.
        try {
            curtidaRepository.saveAndFlush(curtida);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Você já curtiu este comentário", e);
        }

        // Criar notificação para o autor do comentário
        // if (!comentario.getAutor().getId().equals(usuarioId)) {
        //     notificacaoService.criarNotificacaoCurtidaComentario(usuario, comentario);
        // }

        return converterParaResponseDTO(comentario, usuarioId);
    }

    @Transactional
    public ComentarioResponseDTO descurtirComentario(Long comentarioId, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário não encontrado"));

        if (!curtidaRepository.existsByUsuarioIdAndComentarioId(usuarioId, comentarioId)) {
            throw new BusinessException("Você não curtiu este comentário");
        }

        curtidaRepository.deleteByUsuarioIdAndComentarioId(usuarioId, comentarioId);

        return converterParaResponseDTO(comentario, usuarioId);
    }

    @Transactional(readOnly = true)
    public Page<ComentarioResponseDTO> listarComentariosPorUsuario(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        Page<Comentario> comentarios = comentarioRepository.findByAutorId(usuarioId, Support.safePage(pageable));

        return comentarios.map(comentario -> converterParaResponseDTO(comentario, usuarioLogadoId));
    }


    private ComentarioResponseDTO converterParaResponseDTO(Comentario comentario, Long usuarioLogadoId) {
        ComentarioResponseDTO dto = new ComentarioResponseDTO();
        dto.setId(comentario.getId());
        dto.setTexto(comentario.getTexto());
        dto.setDataCriacao(comentario.getDataCriacao());
        dto.setPostId(comentario.getPost().getId());

        if (comentario.getComentarioPai() != null) {
            dto.setComentarioPaiId(comentario.getComentarioPai().getId());
        }

        UsuarioSummaryDTO autorDTO = new UsuarioSummaryDTO();
        autorDTO.setId(comentario.getAutor().getId());
        autorDTO.setNome(comentario.getAutor().getNome());
        autorDTO.setUsername(comentario.getAutor().getUsername());
        autorDTO.setFotoPerfil(s3StorageService.getFileUrl(comentario.getAutor().getFotoPerfil()));

        dto.setAutor(autorDTO);

        dto.setNumeroCurtidas(curtidaRepository.countByComentarioId(comentario.getId()));
        dto.setNumeroRespostas(comentarioRepository.countRespostasByComentarioId(comentario.getId()));

        if (usuarioLogadoId != null) {
            dto.setCurtidoPeloUsuario(curtidaRepository.existsByUsuarioIdAndComentarioId(usuarioLogadoId, comentario.getId()));
        }

        return dto;
    }
}