package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.busca.BuscarResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Busca global: pessoas + posts (com privacidade) + hashtags em uma chamada.
 */
@Service
@RequiredArgsConstructor
public class BuscarService {

    private final UsuarioService usuarioService;
    private final PostService postService;
    private final HashtagService hashtagService;

    @Transactional(readOnly = true)
    public BuscarResponseDTO buscar(String termo, Long viewerId, Pageable pageable) {
        BuscarResponseDTO resposta = new BuscarResponseDTO();
        resposta.setUsuarios(usuarioService.buscarUsuarios(termo, pageable, viewerId));
        resposta.setPosts(postService.listarPostsPorLegenda(termo, pageable, viewerId));
        resposta.setHashtags(hashtagService.buscarHashtags(termo.startsWith("#") ? termo.substring(1) : termo, pageable));
        return resposta;
    }
}
