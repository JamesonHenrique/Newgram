package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.hashtag.HashtagResponseDTO;
import com.jhcs.newgram.application.dtos.hashtag.HashtagSummaryDTO;
import com.jhcs.newgram.core.domain.entities.Hashtag;
import com.jhcs.newgram.core.domain.repositories.HashtagRepository;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HashtagService {

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private PostRepository postRepository;

    @Transactional(readOnly = true)
    public HashtagResponseDTO buscarPorNome(String nome) {
        Hashtag hashtag = hashtagRepository.findByNome(nome)
                .orElseThrow(() -> new ResourceNotFoundException("Hashtag não encontrada"));

        return converterParaResponseDTO(hashtag);
    }

    @Cacheable(cacheNames = com.jhcs.newgram.infrastructure.config.CacheConfig.HASHTAGS_POPULARES)
    @Transactional(readOnly = true)
    public Page<HashtagSummaryDTO> listarHashtagsPopulares(Pageable pageable) {

        Pageable safePageable = Support.safePage(pageable, Sort.unsorted());
        Page<Object[]> hashtagsPopulares = hashtagRepository.findHashtagsPopulares(safePageable);

        return hashtagsPopulares.map(resultado -> {
            HashtagSummaryDTO dto = new HashtagSummaryDTO();
            dto.setNome((String) resultado[0]);
            dto.setQuantidadePosts(((Number) resultado[1]).longValue());
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<HashtagSummaryDTO> listarHashtagsPorPostId(Long postId, Pageable pageable) {
        List<Hashtag> hashtags = hashtagRepository.findByPostId(postId);
        List<HashtagSummaryDTO> dtos = hashtags.stream()
                .map(this::converterParaSummaryDTO)
                .collect(Collectors.toList());
        return Support.pageOf(dtos, pageable);
    }

    /** Mantido para compatibilidade: primeira pagina com 50 itens. */
    @Transactional(readOnly = true)
    public List<HashtagSummaryDTO> listarHashtagsPorPostId(Long postId) {
        return listarHashtagsPorPostId(postId,
                org.springframework.data.domain.PageRequest.of(0, Support.MAX_PAGE_SIZE)).getContent();
    }

    @Transactional(readOnly = true)
    public Page<HashtagSummaryDTO> sugerirHashtags(String termo, int limite, Pageable pageable) {
        Page<Hashtag> pagina = hashtagRepository.findByNomeContainingIgnoreCase(
                termo, org.springframework.data.domain.PageRequest.of(0, Support.safeLimit(limite)));
        List<HashtagSummaryDTO> dtos = pagina.getContent().stream()
                .map(this::converterParaSummaryDTO)
                .collect(Collectors.toList());
        return Support.pageOf(dtos, pageable);
    }

    /** Mantido para compatibilidade: lista simples limitada. */
    @Transactional(readOnly = true)
    public List<HashtagSummaryDTO> sugerirHashtags(String termo, int limite) {
        return hashtagRepository.findByNomeContainingIgnoreCase(termo,
                        org.springframework.data.domain.PageRequest.of(0, Support.safeLimit(limite)))
                .getContent()
                .stream()
                .map(this::converterParaSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<HashtagSummaryDTO> buscarHashtags(String termo, Pageable pageable) {
        Pageable safePageable = Support.safePage(pageable, Sort.unsorted());
        Page<Hashtag> hashtags = hashtagRepository.findByNomeContainingIgnoreCase(termo, safePageable);

        return hashtags.map(this::converterParaSummaryDTO);
    }



    private HashtagResponseDTO converterParaResponseDTO(Hashtag hashtag) {
        HashtagResponseDTO dto = new HashtagResponseDTO();
        dto.setId(hashtag.getId());
        dto.setNome(hashtag.getNome());
        dto.setQuantidadePosts(postRepository.countByHashtagId(hashtag.getId()));
        return dto;
    }

    private HashtagSummaryDTO converterParaSummaryDTO(Hashtag hashtag) {
        HashtagSummaryDTO dto = new HashtagSummaryDTO();
        dto.setNome(hashtag.getNome());
        dto.setQuantidadePosts(postRepository.countByHashtagId(hashtag.getId()));
        return dto;
    }
}