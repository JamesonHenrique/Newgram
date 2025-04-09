package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemCreateDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.Arquivo;
import com.jhcs.newgram.core.domain.entities.Conversa;
import com.jhcs.newgram.core.domain.entities.Mensagem;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.ConversaRepository;
import com.jhcs.newgram.core.domain.repositories.MensagemRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class MensagemService {

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Transactional
    public MensagemResponseDTO enviarMensagem(Long conversaId, MensagemCreateDTO dto, Long usuarioId) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));

        if (!isParticipante(conversa, usuarioId)) {
            throw new UnauthorizedException("Você não é participante desta conversa");
        }

        Usuario remetente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Mensagem mensagem = new Mensagem();
        mensagem.setConteudo(dto.getConteudo());
        mensagem.setTipo(dto.getTipo());
        mensagem.setRemetente(remetente);
        mensagem.setConversa(conversa);
        mensagem.setDataEnvio(new Date());
        mensagem.setVisualizada(false);
        mensagem.setEntregue(true);

        mensagem = mensagemRepository.save(mensagem);


        conversa.setUltimaInteracao(new Date());
        conversaRepository.save(conversa);

        return converterParaMensagemResponseDTO(mensagem, usuarioId);
    }

    @Transactional(readOnly = true)
    public Page<MensagemResponseDTO> listarMensagens(Long conversaId, Long usuarioId, Pageable pageable) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));

        if (!isParticipante(conversa, usuarioId)) {
            throw new UnauthorizedException("Você não é participante desta conversa");
        }

        Page<Mensagem> mensagens = mensagemRepository.findByConversaIdOrderByDataEnvioDesc(conversaId, pageable);

        return mensagens.map(mensagem -> converterParaMensagemResponseDTO(mensagem, usuarioId));
    }

    @Transactional
    public void marcarMensagensComoLidas(Long conversaId, Long usuarioId) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));

        if (!isParticipante(conversa, usuarioId)) {
            throw new UnauthorizedException("Você não é participante desta conversa");
        }

        mensagemRepository.marcarMensagensComoVisualizadas(conversaId, usuarioId);
    }

    @Transactional
    public void excluirMensagem(Long mensagemId, Long usuarioId, boolean paraTodasPessoas) {
        Mensagem mensagem = mensagemRepository.findById(mensagemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensagem não encontrada"));

        if (mensagem.getRemetente() == null || !mensagem.getRemetente().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não pode excluir esta mensagem");
        }

        if (paraTodasPessoas) {
            mensagemRepository.delete(mensagem);
        } else {
            mensagem.setDeletadaPeloRemetente(true);
            mensagemRepository.save(mensagem);
        }
    }

    private boolean isParticipante(Conversa conversa, Long usuarioId) {
        return conversa.getParticipantes().stream()
                .anyMatch(participante -> participante.getId().equals(usuarioId));
    }

    private MensagemResponseDTO converterParaMensagemResponseDTO(Mensagem mensagem, Long usuarioId) {
        MensagemResponseDTO dto = new MensagemResponseDTO();
        dto.setId(mensagem.getId());
        dto.setConteudo(mensagem.getConteudo());
        dto.setTipo(mensagem.getTipo());
        dto.setDataEnvio(mensagem.getDataEnvio());
        dto.setVisualizada(mensagem.isVisualizada());
        dto.setEntregue(mensagem.isEntregue());
        dto.setDeletada(mensagem.isDeletadaPeloRemetente() &&
                (mensagem.getRemetente() != null && mensagem.getRemetente().getId().equals(usuarioId)));

        if (mensagem.getRemetente() != null) {
            UsuarioSummaryDTO remetenteDTO = new UsuarioSummaryDTO();
            remetenteDTO.setId(mensagem.getRemetente().getId());
            remetenteDTO.setNome(mensagem.getRemetente().getNome());
            remetenteDTO.setUsername(mensagem.getRemetente().getUsername());



            dto.setRemetente(remetenteDTO);
        }




        return dto;
    }
}