package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.comentario.ComentarioCreateDTO;
import com.jhcs.newgram.application.dtos.comentario.ComentarioResponseDTO;
import com.jhcs.newgram.application.dtos.comentario.ComentarioUpdateDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.*;
import com.jhcs.newgram.core.domain.repositories.ComentarioRepository;
import com.jhcs.newgram.core.domain.repositories.CurtidaRepository;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
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
    private ArquivoService arquivoService;

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
        comentario.setDataCriacao(new Date());

        // Se for uma resposta a outro comentário
        if (dto.getComentarioPaiId() != null) {
            Comentario comentarioPai = comentarioRepository.findById(dto.getComentarioPaiId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comentário pai não encontrado"));

            // Verificar se o comentário pai pertence ao mesmo post
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

        // Verificar se o usuário é o autor do comentário
        if (!comentario.getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para editar este comentário");
        }

        comentario.setTexto(dto.getTexto());
        comentario = comentarioRepository.save(comentario);

        return converterParaResponseDTO(comentario, usuarioId);
    }

    @Transactional
    public void excluirComentario(Long comentarioId, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário não encontrado"));

        // Verificar se o usuário é o autor do comentário ou do post
        if (!comentario.getAutor().getId().equals(usuarioId) &&
                !comentario.getPost().getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para excluir este comentário");
        }

        comentarioRepository.delete(comentario);
    }

    @Transactional(readOnly = true)
    public Page<ComentarioResponseDTO> listarComentariosPorPost(Long postId, Pageable pageable, Long usuarioId) {
        // Verificar se o post existe
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post não encontrado");
        }

        // Buscar apenas comentários principais (não respostas)
        Page<Comentario> comentarios = (Page<Comentario>) comentarioRepository.findComentariosPrincipaisByPostId(postId, pageable);

        return comentarios.map(comentario -> converterParaResponseDTO(comentario, usuarioId));
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> listarRespostasPorComentario(Long comentarioId, Long usuarioId) {
        // Verificar se o comentário existe
        if (!comentarioRepository.existsById(comentarioId)) {
            throw new ResourceNotFoundException("Comentário não encontrado");
        }

        List<Comentario> respostas = comentarioRepository.findByComentarioPaiIdOrderByDataCriacaoAsc(comentarioId);

        return respostas.stream()
                .map(resposta -> converterParaResponseDTO(resposta, usuarioId))
                .collect(Collectors.toList());
    }

    @Transactional
    public ComentarioResponseDTO curtirComentario(Long comentarioId, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (curtidaRepository.existsByUsuarioIdAndComentarioId(usuarioId, comentarioId)) {
            throw new BusinessException("Você já curtiu este comentário");
        }

        Curtida curtida = new Curtida();
        curtida.setUsuario(usuario);
        curtida.setComentario(comentario);
        curtida.setDataCriacao(new Date());

        curtidaRepository.save(curtida);

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
        // Verificar se o usuário existe
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        Page<Comentario> comentarios = comentarioRepository.findByAutorId(usuarioId, pageable);

        return comentarios.map(comentario -> converterParaResponseDTO(comentario, usuarioLogadoId));
    }

    // Métodos auxiliares

    private ComentarioResponseDTO converterParaResponseDTO(Comentario comentario, Long usuarioLogadoId) {
        ComentarioResponseDTO dto = new ComentarioResponseDTO();
        dto.setId(comentario.getId());
        dto.setTexto(comentario.getTexto());
        dto.setDataCriacao(comentario.getDataCriacao());
        dto.setPostId(comentario.getPost().getId());

        if (comentario.getComentarioPai() != null) {
            dto.setComentarioPaiId(comentario.getComentarioPai().getId());
        }

        // Autor
        UsuarioSummaryDTO autorDTO = new UsuarioSummaryDTO();
        autorDTO.setId(comentario.getAutor().getId());
        autorDTO.setNome(comentario.getAutor().getNome());
        autorDTO.setUsername(comentario.getAutor().getUsername());

        dto.setAutor(autorDTO);

        // Estatísticas
        dto.setNumeroCurtidas(curtidaRepository.countByComentarioId(comentario.getId()));
        dto.setNumeroRespostas(comentarioRepository.countRespostasByComentarioId(comentario.getId()));

        // Verificar se o usuário curtiu o comentário
        if (usuarioLogadoId != null) {
            dto.setCurtidoPeloUsuario(curtidaRepository.existsByUsuarioIdAndComentarioId(usuarioLogadoId, comentario.getId()));
        }

        return dto;
    }

    private ComentarioResponseDTO converterParaResponseDTOComRespostas(Comentario comentario, Long usuarioLogadoId) {
        ComentarioResponseDTO dto = converterParaResponseDTO(comentario, usuarioLogadoId);

        // Adicionar respostas (limitadas às primeiras 3, por exemplo)
        List<Comentario> respostas = comentarioRepository.findByComentarioPaiIdOrderByDataCriacaoAsc(comentario.getId());
        List<ComentarioResponseDTO> respostasDTO = new ArrayList<>();

        // Limitar a quantidade de respostas iniciais para não sobrecarregar
        int limite = Math.min(respostas.size(), 3);
        for (int i = 0; i < limite; i++) {
            respostasDTO.add(converterParaResponseDTO(respostas.get(i), usuarioLogadoId));
        }

        dto.setRespostas(respostasDTO);
        return dto;
    }
}