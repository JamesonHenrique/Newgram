package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.salvos.SalvosResponseDTO;
import com.jhcs.newgram.core.domain.entities.Post;
import com.jhcs.newgram.core.domain.entities.Salvos;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.core.domain.repositories.SalvosRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SalvosService {

    @Autowired
    private SalvosRepository salvosRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<SalvosResponseDTO> listarPostsSalvosPorUsuario(Long usuarioId, Pageable pageable) {
        Page<Salvos> salvos = salvosRepository.findByUsuarioIdOrderByDataSalvoDesc(usuarioId, Support.safePage(pageable));
        return salvos.map(this::converterParaSalvosResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<SalvosResponseDTO> listarPostsSalvosPorColecao(Long usuarioId, String colecao, Pageable pageable) {
        Page<Salvos> salvos = salvosRepository.findByUsuarioIdAndColecaoOrderByDataSalvoDesc(usuarioId, colecao, Support.safePage(pageable));
        return salvos.map(this::converterParaSalvosResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<String> listarColecoesPorUsuario(Long usuarioId) {
        return salvosRepository.findColecoesByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Long contarPostsSalvos(Long usuarioId) {
        return salvosRepository.countSalvosByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Long contarPostsSalvosPorColecao(Long usuarioId, String colecao) {
        return salvosRepository.countSalvosByUsuarioIdAndColecao(usuarioId, colecao);
    }

    @Transactional(readOnly = true)
    public boolean verificarPostSalvo(Long usuarioId, Long postId) {
        return salvosRepository.existsByUsuarioIdAndPostId(usuarioId, postId);
    }

    @Transactional
    public void salvarPost(Long usuarioId, Long postId, String colecao) {
        // UPSERT idempotente (unificado com PostService): já salvo apenas atualiza a coleção.
        Optional<Salvos> salvoExistente = salvosRepository.findByUsuarioIdAndPostId(usuarioId, postId);
        if (salvoExistente.isPresent()) {
            Salvos salvo = salvoExistente.get();
            if (colecao != null && !colecao.isEmpty()) {
                salvo.setColecao(colecao);
                salvosRepository.save(salvo);
            }
            return;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        Salvos salvos = new Salvos();
        salvos.setUsuario(usuario);
        salvos.setPost(post);
        salvos.setDataSalvo(LocalDateTime.now());

        if (colecao != null && !colecao.isEmpty()) {
            salvos.setColecao(colecao);
        }

        try {
            salvosRepository.saveAndFlush(salvos);
        } catch (DataIntegrityViolationException e) {
            // Corrida: outro request salvou entre o find e o save → atualiza a coleção.
            salvosRepository.findByUsuarioIdAndPostId(usuarioId, postId).ifPresent(s -> {
                if (colecao != null && !colecao.isEmpty()) {
                    s.setColecao(colecao);
                    salvosRepository.save(s);
                }
            });
        }
    }

    @Transactional
    public void removerPostSalvo(Long usuarioId, Long postId) {
        salvosRepository.deleteByUsuarioIdAndPostId(usuarioId, postId);
    }

    private SalvosResponseDTO converterParaSalvosResponseDTO(Salvos salvos) {
        SalvosResponseDTO dto = new SalvosResponseDTO();
        dto.setId(salvos.getId());
        dto.setPostId(salvos.getPost().getId());
        dto.setUsuarioId(salvos.getUsuario().getId());
        dto.setDataSalvo(salvos.getDataSalvo());
        dto.setColecao(salvos.getColecao());

        dto.setLegendaPost(salvos.getPost().getLegenda());

        return dto;
    }
}
