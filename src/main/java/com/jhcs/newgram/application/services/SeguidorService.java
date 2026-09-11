package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.seguidor.SeguidorResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.Seguidor;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.StatusSeguimento;
import com.jhcs.newgram.core.domain.enums.TipoNotificacao;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeguidorService {

    private final SeguidorRepository seguidorRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    @Transactional(readOnly = true)
    public Page<SeguidorResponseDTO> listarSeguidores(Long usuarioId, Pageable pageable) {
        return seguidorRepository
                .findSeguidoresByUsuarioId(usuarioId, StatusSeguimento.ACEITO, Support.safePage(pageable))
                .map(seguidor -> {
                    Optional<Seguidor> relacao =
                            seguidorRepository.findBySeguidorIdAndSeguidoId(seguidor.getId(), usuarioId);
                    SeguidorResponseDTO dto = new SeguidorResponseDTO();
                    relacao.ifPresent(r -> {
                        dto.setId(r.getId());
                        dto.setDataCriacao(r.getDataCriacao());
                        dto.setNotificacoesAtivadas(r.isNotificacoesAtivadas());
                    });
                    dto.setSeguidorId(seguidor.getId());
                    dto.setSeguidorUsername(seguidor.getUsername());
                    dto.setSeguidorNome(seguidor.getNome());
                    dto.setSeguidoId(usuarioId);
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public Page<SeguidorResponseDTO> listarSeguidos(Long usuarioId, Pageable pageable) {
        return seguidorRepository
                .findSeguidosByUsuarioId(usuarioId, StatusSeguimento.ACEITO, Support.safePage(pageable))
                .map(seguido -> {
                    Optional<Seguidor> relacao =
                            seguidorRepository.findBySeguidorIdAndSeguidoId(usuarioId, seguido.getId());
                    SeguidorResponseDTO dto = new SeguidorResponseDTO();
                    relacao.ifPresent(r -> {
                        dto.setId(r.getId());
                        dto.setDataCriacao(r.getDataCriacao());
                        dto.setNotificacoesAtivadas(r.isNotificacoesAtivadas());
                    });
                    dto.setSeguidoId(seguido.getId());
                    dto.setSeguidoUsername(seguido.getUsername());
                    dto.setSeguidoNome(seguido.getNome());
                    dto.setSeguidorId(usuarioId);
                    return dto;
                });
    }

    @Cacheable(cacheNames = com.jhcs.newgram.infrastructure.config.CacheConfig.CONTAGENS_SEGUIDORES, key = "'seg:' + #usuarioId")
    @Transactional(readOnly = true)
    public Long contarSeguidores(Long usuarioId) {
        return seguidorRepository.countSeguidoresByUsuarioId(usuarioId, StatusSeguimento.ACEITO);
    }

    @Cacheable(cacheNames = com.jhcs.newgram.infrastructure.config.CacheConfig.CONTAGENS_SEGUIDORES, key = "'sdo:' + #usuarioId")
    @Transactional(readOnly = true)
    public Long contarSeguidos(Long usuarioId) {
        return seguidorRepository.countSeguidosByUsuarioId(usuarioId, StatusSeguimento.ACEITO);
    }

    @Transactional(readOnly = true)
    public boolean verificarSeguimento(Long seguidorId, Long seguidoId) {
        return seguidorRepository.existsBySeguidorIdAndSeguidoIdAndStatus(
                seguidorId, seguidoId, StatusSeguimento.ACEITO);
    }

    @Transactional(readOnly = true)
    public boolean verificarSolicitacaoPendente(Long seguidorId, Long seguidoId) {
        return seguidorRepository.existsBySeguidorIdAndSeguidoIdAndStatus(
                seguidorId, seguidoId, StatusSeguimento.PENDENTE);
    }

    @Transactional(readOnly = true)
    public Page<SeguidorResponseDTO> listarSolicitacoesRecebidas(Long usuarioId, Pageable pageable) {
        return seguidorRepository.findSolicitacoesRecebidas(usuarioId, Support.safePage(pageable))
                .map(this::converterParaDTO);
    }

    @Transactional(readOnly = true)
    public long contarSolicitacoesRecebidas(Long usuarioId) {
        return seguidorRepository.countSolicitacoesRecebidas(usuarioId);
    }

    @CacheEvict(cacheNames = com.jhcs.newgram.infrastructure.config.CacheConfig.CONTAGENS_SEGUIDORES, allEntries = true)
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

        // Conta privada: vira solicitacao pendente de aceite.
        boolean privada = seguido.isPrivado();
        relacao.setStatus(privada ? StatusSeguimento.PENDENTE : StatusSeguimento.ACEITO);

        try {
            relacao = seguidorRepository.saveAndFlush(relacao);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Já está seguindo este usuário", e);
        }

        // Notificacao nao desfaz o follow se falhar: desacoplada do resultado.
        try {
            notificacaoService.criarNotificacao(
                    seguidoId,
                    seguidorId,
                    privada ? TipoNotificacao.SOLICITACAO_SEGUIMENTO : TipoNotificacao.NOVO_SEGUIDOR,
                    privada
                            ? seguidor.getUsername() + " quer seguir você"
                            : seguidor.getUsername() + " começou a seguir você");
        } catch (RuntimeException e) {
            log.warn("Follow {}->{} salvo, mas notificacao falhou", seguidorId, seguidoId, e);
        }

        return converterParaDTO(relacao);
    }

    @Transactional
    public SeguidorResponseDTO aceitarSolicitacao(Long solicitacaoId, Long usuarioId) {
        Seguidor relacao = seguidorRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        Support.requireOwner(relacao.getSeguido().getId(), usuarioId,
                "Você não tem permissão para aceitar esta solicitação");
        if (relacao.getStatus() != StatusSeguimento.PENDENTE) {
            throw new BusinessException("Solicitação já foi respondida");
        }
        relacao.setStatus(StatusSeguimento.ACEITO);
        return converterParaDTO(seguidorRepository.save(relacao));
    }

    @Transactional
    public void rejeitarSolicitacao(Long solicitacaoId, Long usuarioId) {
        Seguidor relacao = seguidorRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        Support.requireOwner(relacao.getSeguido().getId(), usuarioId,
                "Você não tem permissão para rejeitar esta solicitação");
        if (relacao.getStatus() != StatusSeguimento.PENDENTE) {
            throw new BusinessException("Solicitação já foi respondida");
        }
        seguidorRepository.delete(relacao);
    }

    @CacheEvict(cacheNames = com.jhcs.newgram.infrastructure.config.CacheConfig.CONTAGENS_SEGUIDORES, allEntries = true)
    @Transactional
    public void deixarDeSeguir(Long seguidorId, Long seguidoId) {
        if (!seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId)) {
            throw new BusinessException("Não está seguindo este usuário");
        }
        seguidorRepository.deleteBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
    }

    @Transactional
    public void alterarNotificacoes(Long seguidorId, Long seguidoId, boolean ativar) {
        Seguidor relacao = seguidorRepository.findBySeguidorIdAndSeguidoId(seguidorId, seguidoId)
                .orElseThrow(() -> new BusinessException("Não está seguindo este usuário"));
        relacao.setNotificacoesAtivadas(ativar);
        seguidorRepository.save(relacao);
    }

    @Transactional(readOnly = true)
    public List<UsuarioSummaryDTO> buscarSeguidosAleatorios(Long usuarioId, int limite) {
        List<Usuario> seguidosAleatorios = seguidorRepository.findRandomSeguidosByUsuarioId(
                usuarioId, org.springframework.data.domain.PageRequest.of(0, Support.safeLimit(limite)));

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
