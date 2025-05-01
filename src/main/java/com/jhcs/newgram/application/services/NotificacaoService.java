package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.notificacao.NotificacaoResponseDTO;
import com.jhcs.newgram.core.domain.entities.Notificacao;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.TipoNotificacao;
import com.jhcs.newgram.core.domain.repositories.NotificacaoRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<NotificacaoResponseDTO> listarNotificacoesPorUsuario(Long usuarioId, Pageable pageable) {
        Page<Notificacao> notificacoes = notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(usuarioId, pageable);
        return notificacoes.map(this::converterParaNotificacaoResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<NotificacaoResponseDTO> listarNotificacoesNaoVisualizadas(Long usuarioId, Pageable pageable) {
        Page<Notificacao> notificacoes = notificacaoRepository.findNotificacoesNaoVisualizadas(usuarioId, pageable);
        return notificacoes.map(this::converterParaNotificacaoResponseDTO);
    }

    @Transactional(readOnly = true)
    public long contarNotificacoesNaoLidas(Long usuarioId) {
        return notificacaoRepository.countByDestinatarioIdAndLidaFalse(usuarioId);
    }

    @Transactional
    public int marcarTodasComoVisualizadas(Long usuarioId) {
        return notificacaoRepository.marcarTodasComoVisualizadas(usuarioId);
    }

    @Transactional
    public void marcarComoVisualizada(Long notificacaoId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void criarNotificacao(Long destinatarioId, Long remetenteId, TipoNotificacao tipo, String conteudo) {
        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new RuntimeException("Destinatário não encontrado"));

        Usuario remetente = usuarioRepository.findById(remetenteId)
                .orElseThrow(() -> new RuntimeException("Remetente não encontrado"));

        Notificacao notificacao = new Notificacao();
        notificacao.setDestinatario(destinatario);
        notificacao.setRemetente(remetente);
        notificacao.setTipo(tipo);
        notificacao.setConteudo(conteudo);
        notificacao.setDataCriacao(new Date());
        notificacao.setLida(false);

        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void deletarNotificacao(Long notificacaoId) {
        notificacaoRepository.deleteById(notificacaoId);
    }

    private NotificacaoResponseDTO converterParaNotificacaoResponseDTO(Notificacao notificacao) {
        NotificacaoResponseDTO dto = new NotificacaoResponseDTO();
        dto.setId(notificacao.getId());
        dto.setTipo(notificacao.getTipo());
        dto.setConteudo(notificacao.getConteudo());
        dto.setDataCriacao(notificacao.getDataCriacao());
        dto.setLida(notificacao.isLida());
        dto.setDestinatarioId(notificacao.getDestinatario().getId());

        if (notificacao.getRemetente() != null) {
            dto.setRemetenteId(notificacao.getRemetente().getId());
            dto.setNomeRemetente(notificacao.getRemetente().getNome());
            dto.setUsernameRemetente(notificacao.getRemetente().getUsername());
        }

        return dto;
    }
}