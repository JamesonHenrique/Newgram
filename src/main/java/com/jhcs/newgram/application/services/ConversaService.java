package com.jhcs.newgram.application.services;



import com.jhcs.newgram.application.dtos.conversa.ConversaDTO;
import com.jhcs.newgram.core.domain.entities.Conversa;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.ConversaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversaService {

    @Autowired
    private ConversaRepository conversaRepository;

    @Transactional
    public Conversa criarConversa(Usuario criador, Usuario participante) {
        Conversa conversa = new Conversa();
        conversa.setCriador(criador);
        conversa.getParticipantes().add(criador);
        conversa.getParticipantes().add(participante);
        conversa.setDataCriacao(new Date());
        conversa.setUltimaInteracao(new Date());
        conversa.setGrupo(false);
        return conversaRepository.save(conversa);
    }

    public Optional<Conversa> buscarConversaEntreUsuarios(Long usuario1Id, Long usuario2Id) {
        return conversaRepository.findByParticipantesIds(usuario1Id, usuario2Id);
    }

    public List<ConversaDTO> listarConversasPorUsuario(Long usuarioId) {
        return conversaRepository.findByParticipanteId(usuarioId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ConversaDTO convertToDTO(Conversa conversa) {
        ConversaDTO dto = new ConversaDTO();
        dto.setId(conversa.getId());
        dto.setDataCriacao(conversa.getDataCriacao());
        dto.setUltimaInteracao(conversa.getUltimaInteracao());
        dto.setGrupo(conversa.isGrupo());
        dto.setNomeGrupo(conversa.getNomeGrupo());
        dto.setCriadorId(conversa.getCriador().getId());
        dto.setParticipantesIds(conversa.getParticipantes().stream()
                .map(Usuario::getId)
                .collect(Collectors.toList()));
        return dto;
    }
}