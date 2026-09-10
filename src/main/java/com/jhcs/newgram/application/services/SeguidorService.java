package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.seguidor.SeguidorResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.Seguidor;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.TipoNotificacao;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeguidorService {

    @Autowired
    private SeguidorRepository seguidorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Transactional(readOnly = true)
    public Page<SeguidorResponseDTO> listarSeguidores(Long usuarioId, Pageable pageable) {
        Page<Usuario> seguidores = seguidorRepository.findSeguidoresByUsuarioId(usuarioId, Support.safePage(pageable));
        return seguidores.map(seguidor -> {
                    Optional<Seguidor> relacao = seguidorRepository.findBySeguidorIdAndSeguidoId(seguidor.getId(), usuarioId);
                    SeguidorResponseDTO dto = new SeguidorResponseDTO();
                    if (relacao.isPresent()) {
                        dto.setId(relacao.get().getId());
                        dto.setDataCriacao(relacao.get().getDataCriacao());
                        dto.setNotificacoesAtivadas(relacao.get().isNotificacoesAtivadas());
                    }
                    dto.setSeguidorId(seguidor.getId());
                    dto.setSeguidorUsername(seguidor.getUsername());
                    dto.setSeguidorNome(seguidor.getNome());

                    dto.setSeguidoId(usuarioId);
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public Page<SeguidorResponseDTO> listarSeguidos(Long usuarioId, Pageable pageable) {
        Page<Usuario> seguidos = seguidorRepository.findSeguidosByUsuarioId(usuarioId, Support.safePage(pageable));
        return seguidos.map(seguido -> {
                    Optional<Seguidor> relacao = seguidorRepository.findBySeguidorIdAndSeguidoId(usuarioId, seguido.getId());
                    SeguidorResponseDTO dto = new SeguidorResponseDTO();
                    if (relacao.isPresent()) {
                        dto.setId(relacao.get().getId());
                        dto.setDataCriacao(relacao.get().getDataCriacao());
                        dto.setNotificacoesAtivadas(relacao.get().isNotificacoesAtivadas());
                    }
                    dto.setSeguidoId(seguido.getId());
                    dto.setSeguidoUsername(seguido.getUsername());
                    dto.setSeguidoNome(seguido.getNome());

                    dto.setSeguidorId(usuarioId);
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public Long contarSeguidores(Long usuarioId) {
        return seguidorRepository.countSeguidoresByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Long contarSeguidos(Long usuarioId) {
        return seguidorRepository.countSeguidosByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public boolean verificarSeguimento(Long seguidorId, Long seguidoId) {
        return seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
    }

    @Transactional
    public SeguidorResponseDTO seguir(Long seguidorId, Long seguidoId) {
        if (seguidorId.equals(seguidoId)) {
            throw new BusinessException("Não é possível seguir a si mesmo");
        }

        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguidor não encontrado"));

        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguido não encontrado"));

        Seguidor relacao = new Seguidor();
        relacao.setSeguidor(seguidor);
        relacao.setSeguido(seguido);
        relacao.setDataCriacao(LocalDateTime.now());
        relacao.setNotificacoesAtivadas(true);

        // Save direto (sem exists prévio): idempotência via constraint única + catch.
        try {
            relacao = seguidorRepository.saveAndFlush(relacao);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Já está seguindo este usuário", e);
        }

        // Notificação desacoplada: falha aqui não desfaz o follow.
        try {
            notificacaoService.criarNotificacao(
                    seguidoId,
                    seguidorId,
                    TipoNotificacao.NOVO_SEGUIDOR,
                    seguidor.getUsername() + " começou a seguir você"
            );
        } catch (RuntimeException e) {
            log.warn("Follow {}->{} persistido, mas notificação falhou: {}", seguidorId, seguidoId, e.getMessage());
        }

        return converterParaDTO(relacao);
    }

    @Transactional
    public void deixarDeSeguir(Long seguidorId, Long seguidoId) {
        if (!seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId)) {
            throw new BusinessException("Não está seguindo este usuário");
        }

        seguidorRepository.deleteBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
    }

    @Transactional
    public void alterarNotificacoes(Long seguidorId, Long seguidoId, boolean ativar) {
        Optional<Seguidor> relacaoOpt = seguidorRepository.findBySeguidorIdAndSeguidoId(seguidorId, seguidoId);

        if (relacaoOpt.isEmpty()) {
            throw new BusinessException("Não está seguindo este usuário");
        }

        Seguidor relacao = relacaoOpt.get();
        relacao.setNotificacoesAtivadas(ativar);
        seguidorRepository.save(relacao);
    }

    @Transactional(readOnly = true)
    public List<UsuarioSummaryDTO> buscarSeguidosAleatorios(Long usuarioId, int limite) {
        List<Usuario> seguidosAleatorios = seguidorRepository.findRandomSeguidosByUsuarioId(usuarioId, Support.safeLimit(limite));

        return seguidosAleatorios.stream()
                .map(seguido -> {
                    UsuarioSummaryDTO dto = new UsuarioSummaryDTO();
                    dto.setId(seguido.getId());
                    dto.setNome(seguido.getNome());
                    dto.setUsername(seguido.getUsername());


                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSeguidosAleatorios(Long usuarioId, Pageable pageable) {
        List<Usuario> seguidosAleatorios = seguidorRepository.findRandomSeguidosByUsuarioId(
                usuarioId, Support.safePage(pageable).getPageSize());

        List<UsuarioSummaryDTO> dtos = seguidosAleatorios.stream()
                .map(seguido -> {
                    UsuarioSummaryDTO dto = new UsuarioSummaryDTO();
                    dto.setId(seguido.getId());
                    dto.setNome(seguido.getNome());
                    dto.setUsername(seguido.getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
        return Support.pageOf(dtos, pageable);
    }

    private SeguidorResponseDTO converterParaDTO(Seguidor seguidor) {
        SeguidorResponseDTO dto = new SeguidorResponseDTO();
        dto.setId(seguidor.getId());
        dto.setDataCriacao(seguidor.getDataCriacao());
        dto.setNotificacoesAtivadas(seguidor.isNotificacoesAtivadas());

        dto.setSeguidorId(seguidor.getSeguidor().getId());
        dto.setSeguidorUsername(seguidor.getSeguidor().getUsername());
        dto.setSeguidorNome(seguidor.getSeguidor().getNome());


        dto.setSeguidoId(seguidor.getSeguido().getId());
        dto.setSeguidoUsername(seguidor.getSeguido().getUsername());
        dto.setSeguidoNome(seguidor.getSeguido().getNome());


        return dto;
    }
}
