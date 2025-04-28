package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.mensagem.MensagemDTO;
import com.jhcs.newgram.core.domain.entities.Conversa;
import com.jhcs.newgram.core.domain.entities.Mensagem;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.TipoMensagem;

import com.jhcs.newgram.core.domain.repositories.MensagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensagemService {

    @Autowired
    private MensagemRepository mensagemRepository;

    @Transactional
    public Mensagem enviarMensagem(Conversa conversa, Usuario remetente, Usuario destinatario, String conteudo, TipoMensagem tipo) {
        Mensagem mensagem = new Mensagem();
        mensagem.setConversa(conversa);
        mensagem.setRemetente(remetente);
        mensagem.setDestinatario(destinatario);
        mensagem.setConteudo(conteudo);
        mensagem.setTipo(tipo);
        mensagem.setEntregue(false);
        mensagem.setVisualizada(false);

        conversa.setUltimaInteracao(new Date());

        return mensagemRepository.save(mensagem);
    }

    @Transactional
    public void marcarComoEntregue(Long mensagemId) {
        Mensagem mensagem = mensagemRepository.findById(mensagemId)
                .orElseThrow(() -> new RuntimeException("Mensagem não encontrada"));
        mensagem.setEntregue(true);
        mensagemRepository.save(mensagem);
    }

    @Transactional
    public void marcarComoVisualizada(Long mensagemId) {
        Mensagem mensagem = mensagemRepository.findById(mensagemId)
                .orElseThrow(() -> new RuntimeException("Mensagem não encontrada"));
        mensagem.setVisualizada(true);
        mensagemRepository.save(mensagem);
    }

    public List<MensagemDTO> listarMensagensPorConversa(Long conversaId) {
        return mensagemRepository.findByConversaIdOrderByDataEnvioAsc(conversaId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MensagemDTO convertToDTO(Mensagem mensagem) {
        MensagemDTO dto = new MensagemDTO();
        dto.setId(mensagem.getId());
        dto.setConversaId(mensagem.getConversa().getId());
        dto.setRemetenteId(mensagem.getRemetente().getId());
        dto.setRemetenteNome(mensagem.getRemetente().getNome());
        dto.setDestinatarioId(mensagem.getDestinatario().getId());
        dto.setDestinatarioNome(mensagem.getDestinatario().getNome());
        dto.setConteudo(mensagem.getConteudo());
        dto.setDataEnvio(mensagem.getDataEnvio());
        dto.setVisualizada(mensagem.isVisualizada());
        dto.setEntregue(mensagem.isEntregue());
        dto.setTipo(mensagem.getTipo());
        dto.setUrlMidia(mensagem.getUrlMidia());
        return dto;
    }
}