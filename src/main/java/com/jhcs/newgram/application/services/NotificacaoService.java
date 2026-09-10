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
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PushNotificationService pushService;

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
    public void marcarComoVisualizada(Long notificacaoId, Long usuarioId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));

        Support.requireOwner(notificacao.getDestinatario().getId(), usuarioId,
                "Você não tem permissão para alterar esta notificação");
        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void criarNotificacao(Long destinatarioId, Long remetenteId, TipoNotificacao tipo, String conteudo) {
        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Destinatário não encontrado"));

        Usuario remetente = usuarioRepository.findById(remetenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Remetente não encontrado"));

        Notificacao notificacao = new Notificacao();
        notificacao.setDestinatario(destinatario);
        notificacao.setRemetente(remetente);
        notificacao.setTipo(tipo);
        notificacao.setConteudo(conteudo);
        notificacao.setDataCriacao(LocalDateTime.now());
        notificacao.setLida(false);

        notificacao = notificacaoRepository.save(notificacao);

        // Fan-out em tempo real + push (desacoplados: falha não desfaz o save).
        try {
            NotificacaoResponseDTO dto = converterParaNotificacaoResponseDTO(notificacao);
            messagingTemplate.convertAndSend("/topic/notificacoes." + destinatarioId, dto);
            pushService.enviarParaUsuario(destinatarioId, "Newgram", conteudo);
        } catch (RuntimeException e) {
            log.warn("Notificacao {} salva, mas fan-out falhou", notificacao.getId(), e);
        }
    }

    @Transactional
    public void deletarNotificacao(Long notificacaoId, Long usuarioId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));
        Support.requireOwner(notificacao.getDestinatario().getId(), usuarioId,
                "Você não tem permissão para excluir esta notificação");
        notificacaoRepository.delete(notificacao);
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