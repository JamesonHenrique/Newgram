package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.statususuario.StatusUsuarioResponseDTO;
import com.jhcs.newgram.core.domain.entities.StatusUsuario;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.StatusUsuarioRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StatusUsuarioService {

    @Autowired
    private StatusUsuarioRepository statusUsuarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public StatusUsuarioResponseDTO buscarStatusPorUsuarioId(Long usuarioId) {
        StatusUsuario statusUsuario = statusUsuarioRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Status do usuário não encontrado"));

        return converterParaDTO(statusUsuario);
    }

    @Transactional(readOnly = true)
    public List<StatusUsuarioResponseDTO> listarUsuariosOnline(List<Long> usuariosIds) {
        List<StatusUsuario> statusOnline = statusUsuarioRepository.findUsuariosOnline(usuariosIds);
        return statusOnline.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StatusUsuarioResponseDTO> listarUsuariosRecentementeAtivos(LocalDateTime dataLimite, List<Long> usuariosIds) {
        List<StatusUsuario> statusRecentes = statusUsuarioRepository.findUsuariosRecentementeAtivos(dataLimite, usuariosIds);
        return statusRecentes.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> listarTodosUsuariosOnlineIds() {
        return statusUsuarioRepository.findAllUsuariosOnlineIds();
    }

    @Transactional
    public StatusUsuarioResponseDTO atualizarStatus(Long usuarioId, boolean online, String statusPersonalizado) {
        Optional<StatusUsuario> statusExistente = statusUsuarioRepository.findByUsuarioId(usuarioId);
        StatusUsuario statusUsuario;

        if (statusExistente.isPresent()) {
            statusUsuario = statusExistente.get();
            statusUsuario.setOnline(online);
            statusUsuario.setUltimoAcesso(LocalDateTime.now());

            if (statusPersonalizado != null) {
                statusUsuario.setStatusPersonalizado(statusPersonalizado);
            }
        } else {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

            statusUsuario = new StatusUsuario();
            statusUsuario.setUsuario(usuario);
            statusUsuario.setOnline(online);
            statusUsuario.setUltimoAcesso(LocalDateTime.now());
            statusUsuario.setStatusPersonalizado(statusPersonalizado);
        }

        statusUsuario = statusUsuarioRepository.save(statusUsuario);
        return converterParaDTO(statusUsuario);
    }

    @Transactional
    public void registrarAcesso(Long usuarioId) {
        Optional<StatusUsuario> statusExistente = statusUsuarioRepository.findByUsuarioId(usuarioId);
        // Debounce de 5 min: evita UPDATE a cada request no feed.
        LocalDateTime agora = LocalDateTime.now();
        StatusUsuario statusUsuario;
        if (statusExistente.isPresent()) {
            statusUsuario = statusExistente.get();
            if (statusUsuario.isOnline()
                    && statusUsuario.getUltimoAcesso() != null
                    && Duration.between(statusUsuario.getUltimoAcesso(), agora).toMinutes() < 5) {
                return;
            }
            statusUsuario.setUltimoAcesso(agora);
            statusUsuario.setOnline(true);
        } else {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

            statusUsuario = new StatusUsuario();
            statusUsuario.setUsuario(usuario);
            statusUsuario.setOnline(true);
            statusUsuario.setUltimoAcesso(agora);
        }

        statusUsuarioRepository.save(statusUsuario);
    }

    @Transactional
    public void registrarSaida(Long usuarioId) {
        Optional<StatusUsuario> statusExistente = statusUsuarioRepository.findByUsuarioId(usuarioId);

        if (statusExistente.isPresent()) {
            statusUsuario = statusExistente.get();
            statusUsuario.setOnline(false);
            statusUsuario.setUltimoAcesso(LocalDateTime.now());
            statusUsuarioRepository.save(statusUsuario);
        }
    }

    private StatusUsuarioResponseDTO converterParaDTO(StatusUsuario statusUsuario) {
        StatusUsuarioResponseDTO dto = new StatusUsuarioResponseDTO();
        dto.setId(statusUsuario.getId());
        dto.setUsuarioId(statusUsuario.getUsuario().getId());
        dto.setOnline(statusUsuario.isOnline());
        dto.setUltimoAcesso(statusUsuario.getUltimoAcesso());
        dto.setStatusPersonalizado(statusUsuario.getStatusPersonalizado());
        dto.setUsername(statusUsuario.getUsuario().getUsername());
        return dto;
    }
}