package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.mensagem.ConversaResponseDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemCreateDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemResponseDTO;
import com.jhcs.newgram.core.domain.entities.Conversa;
import com.jhcs.newgram.core.domain.entities.Mensagem;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.TipoNotificacao;
import com.jhcs.newgram.core.domain.repositories.BloqueioRepository;
import com.jhcs.newgram.core.domain.repositories.ConversaRepository;
import com.jhcs.newgram.core.domain.repositories.MensagemRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.aws.S3StorageService;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mensagens diretas 1:1 com polling (front recarrega a cada 10s).
 * Uma conversa por par (chave ordenada + unique com catch 409).
 */
@Service
@RequiredArgsConstructor
public class ConversaService {

    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final BloqueioRepository bloqueioRepository;
    private final NotificacaoService notificacaoService;
    private final S3StorageService s3StorageService;

    @Transactional
    public ConversaResponseDTO iniciarOuObter(Long outroId, Long euId) {
        if (euId.equals(outroId)) {
            throw new BusinessException("Não é possível conversar consigo mesmo");
        }
        Usuario eu = usuarioRepository.findById(euId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        Usuario outro = usuarioRepository.findById(outroId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (bloqueioRepository.existsBloqueioEntre(euId, outroId)) {
            throw new BusinessException("Conversa indisponível");
        }

        String chave = Conversa.chavePara(euId, outroId);
        Optional<Conversa> existente = conversaRepository.findByChaveParticipantes(chave);
        if (existente.isPresent()) {
            return converterConversa(existente.get(), euId);
        }

        Conversa conversa = new Conversa();
        conversa.setChaveParticipantes(chave);
        conversa.getParticipantes().add(eu);
        conversa.getParticipantes().add(outro);
        conversa.setDataAtualizacao(LocalDateTime.now());
        try {
            conversa = conversaRepository.saveAndFlush(conversa);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Corrida: outro request criou primeiro.
            conversa = conversaRepository.findByChaveParticipantes(chave)
                    .orElseThrow(() -> new BusinessException("Falha ao abrir conversa", e));
        }
        return converterConversa(conversa, euId);
    }

    @Transactional(readOnly = true)
    public Page<ConversaResponseDTO> listarConversas(Long euId, Pageable pageable) {
        return conversaRepository.findConversasPorUsuario(euId, Support.safePage(pageable))
                .map(conversa -> converterConversa(conversa, euId));
    }

    @Transactional
    public Page<MensagemResponseDTO> listarMensagens(Long conversaId, Long euId, Pageable pageable) {
        Conversa conversa = conversaDa(euId, conversaId);
        mensagemRepository.marcarComoLidas(conversaId, euId);
        return mensagemRepository.findMensagensPorConversa(conversa.getId(), Support.safePage(pageable))
                .map(mensagem -> converterMensagem(mensagem, euId));
    }

    @Transactional
    public MensagemResponseDTO enviarMensagem(Long conversaId, MensagemCreateDTO dto, Long euId) {
        Conversa conversa = conversaDa(euId, conversaId);
        Usuario eu = usuarioRepository.findById(euId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Mensagem mensagem = new Mensagem();
        mensagem.setConversa(conversa);
        mensagem.setRemetente(eu);
        mensagem.setTexto(dto.getTexto().trim());
        mensagem.setLida(false);
        mensagem.setDataCriacao(LocalDateTime.now());
        mensagem = mensagemRepository.save(mensagem);

        conversa.setDataAtualizacao(LocalDateTime.now());
        conversaRepository.save(conversa);

        // Notifica o outro participante (desacoplada: nao desfaz o envio).
        conversa.getParticipantes().stream()
                .filter(p -> !p.getId().equals(euId))
                .findFirst()
                .ifPresent(destino -> {
                    try {
                        notificacaoService.criarNotificacao(
                                destino.getId(), euId, TipoNotificacao.MENSAGEM,
                                eu.getUsername() + " enviou uma mensagem");
                    } catch (RuntimeException e) {
                        org.slf4j.LoggerFactory.getLogger(ConversaService.class)
                                .warn("Mensagem {} salva, notificacao falhou", mensagem.getId(), e);
                    }
                });

        return converterMensagem(mensagem, euId);
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Long euId) {
        return mensagemRepository.countNaoLidasPorUsuario(euId);
    }

    private Conversa conversaDa(Long euId, Long conversaId) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));
        boolean participa = conversa.getParticipantes().stream().anyMatch(p -> p.getId().equals(euId));
        if (!participa) {
            throw new BusinessException("Você não participa desta conversa");
        }
        return conversa;
    }

    private ConversaResponseDTO converterConversa(Conversa conversa, Long euId) {
        ConversaResponseDTO dto = new ConversaResponseDTO();
        dto.setId(conversa.getId());
        dto.setDataAtualizacao(conversa.getDataAtualizacao());
        conversa.getParticipantes().stream()
                .filter(p -> !p.getId().equals(euId))
                .findFirst()
                .ifPresent(outro -> {
                    dto.setOutroParticipanteId(outro.getId());
                    dto.setOutroParticipanteUsername(outro.getUsername());
                    dto.setOutroParticipanteNome(outro.getNome());
                    dto.setOutroParticipanteFotoPerfil(s3StorageService.getFileUrl(outro.getFotoPerfil()));
                });
        var ultima = mensagemRepository.findTopByConversaIdOrderByDataCriacaoDesc(conversa.getId());
        ultima.ifPresent(mensagem -> dto.setUltimaMensagem(mensagem.getTexto()));
        dto.setNaoLidas(mensagemRepository.countNaoLidasPorConversa(conversa.getId(), euId));
        return dto;
    }

    private MensagemResponseDTO converterMensagem(Mensagem mensagem, Long euId) {
        MensagemResponseDTO dto = new MensagemResponseDTO();
        dto.setId(mensagem.getId());
        dto.setConversaId(mensagem.getConversa().getId());
        dto.setRemetenteId(mensagem.getRemetente().getId());
        dto.setTexto(mensagem.getTexto());
        dto.setLida(mensagem.isLida());
        dto.setDataCriacao(mensagem.getDataCriacao());
        dto.setMinha(mensagem.getRemetente().getId().equals(euId));
        return dto;
    }
}
