package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.moderacao.BloqueioResponseDTO;
import com.jhcs.newgram.application.dtos.moderacao.DenunciaCreateDTO;
import com.jhcs.newgram.application.dtos.moderacao.DenunciaResponseDTO;
import com.jhcs.newgram.core.domain.entities.Bloqueio;
import com.jhcs.newgram.core.domain.entities.Denuncia;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.AlvoDenuncia;
import com.jhcs.newgram.core.domain.enums.StatusDenuncia;
import com.jhcs.newgram.core.domain.repositories.BloqueioRepository;
import com.jhcs.newgram.core.domain.repositories.ComentarioRepository;
import com.jhcs.newgram.core.domain.repositories.DenunciaRepository;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Moderação: denúncias (fila) e bloqueios (rompem vínculo e somem do alcance).
 */
@Service
@RequiredArgsConstructor
public class ModeracaoService {

    private final DenunciaRepository denunciaRepository;
    private final BloqueioRepository bloqueioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PostRepository postRepository;
    private final ComentarioRepository comentarioRepository;
    private final SeguidorRepository seguidorRepository;

    @Transactional
    public DenunciaResponseDTO denunciar(DenunciaCreateDTO dto, Long denuncianteId) {
        Usuario denunciante = usuarioRepository.findById(denuncianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        validarAlvo(dto.getTipoAlvo(), dto.getAlvoId());

        Denuncia denuncia = new Denuncia();
        denuncia.setDenunciante(denunciante);
        denuncia.setTipoAlvo(dto.getTipoAlvo());
        denuncia.setAlvoId(dto.getAlvoId());
        denuncia.setMotivo(dto.getMotivo().trim());
        denuncia.setDescricao(dto.getDescricao());
        denuncia.setStatus(StatusDenuncia.ABERTA);
        denuncia.setDataCriacao(LocalDateTime.now());

        try {
            denuncia = denunciaRepository.saveAndFlush(denuncia);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Você já denunciou este conteúdo", e);
        }
        return converterDenuncia(denuncia);
    }

    @Transactional(readOnly = true)
    public Page<DenunciaResponseDTO> listarMinhasDenuncias(Long denuncianteId, Pageable pageable) {
        return denunciaRepository.findByDenuncianteIdOrderByDataCriacaoDesc(
                denuncianteId, Support.safePage(pageable)).map(this::converterDenuncia);
    }

    /** Fila de moderação (ADMIN): abertas/pendentes primeiro por padrão. */
    @Transactional(readOnly = true)
    public Page<DenunciaResponseDTO> listarFila(StatusDenuncia status, Pageable pageable) {
        if (status == null) {
            return denunciaRepository.findAll(Support.safePage(pageable)).map(this::converterDenuncia);
        }
        return denunciaRepository.findByStatusOrderByDataCriacaoDesc(status, Support.safePage(pageable))
                .map(this::converterDenuncia);
    }

    /** ADMIN: move a denúncia na fila (EM_ANALISE, RESOLVIDA, REJEITADA). */
    @Transactional
    public DenunciaResponseDTO resolver(Long denunciaId, StatusDenuncia novoStatus) {
        if (novoStatus == null || novoStatus == StatusDenuncia.ABERTA) {
            throw new BusinessException("Status inválido para resolução");
        }
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Denúncia não encontrada"));
        denuncia.setStatus(novoStatus);
        return converterDenuncia(denunciaRepository.save(denuncia));
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(
            cacheNames = com.jhcs.newgram.infrastructure.config.CacheConfig.CONTAGENS_SEGUIDORES, allEntries = true)
    public void bloquear(Long bloqueadorId, Long bloqueadoId) {
        if (bloqueadorId.equals(bloqueadoId)) {
            throw new BusinessException("Não é possível bloquear a si mesmo");
        }
        Usuario bloqueador = usuarioRepository.findById(bloqueadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        Usuario bloqueado = usuarioRepository.findById(bloqueadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Bloqueio bloqueio = new Bloqueio();
        bloqueio.setBloqueador(bloqueador);
        bloqueio.setBloqueado(bloqueado);
        bloqueio.setDataCriacao(LocalDateTime.now());
        try {
            bloqueioRepository.saveAndFlush(bloqueio);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Usuário já bloqueado", e);
        }

        // Bloqueio rompe o vínculo nos dois sentidos (inclui pendentes).
        seguidorRepository.deleteBySeguidorIdAndSeguidoId(bloqueadorId, bloqueadoId);
        seguidorRepository.deleteBySeguidorIdAndSeguidoId(bloqueadoId, bloqueadorId);
    }

    @Transactional
    public void desbloquear(Long bloqueadorId, Long bloqueadoId) {
        if (!bloqueioRepository.existsByBloqueadorIdAndBloqueadoId(bloqueadorId, bloqueadoId)) {
            throw new BusinessException("Usuário não está bloqueado");
        }
        bloqueioRepository.deleteByBloqueadorIdAndBloqueadoId(bloqueadorId, bloqueadoId);
    }

    @Transactional(readOnly = true)
    public boolean bloqueadoPor(Long bloqueadorId, Long bloqueadoId) {
        return bloqueioRepository.existsByBloqueadorIdAndBloqueadoId(bloqueadorId, bloqueadoId);
    }

    @Transactional(readOnly = true)
    public boolean bloqueioEntre(Long a, Long b) {
        return bloqueioRepository.existsBloqueioEntre(a, b);
    }

    @Transactional(readOnly = true)
    public Page<BloqueioResponseDTO> listarBloqueios(Long usuarioId, Pageable pageable) {
        return bloqueioRepository.findBloqueiosPorUsuario(usuarioId, Support.safePage(pageable))
                .map(this::converterBloqueio);
    }

    private void validarAlvo(AlvoDenuncia tipo, Long alvoId) {
        boolean existe = switch (tipo) {
            case POST -> postRepository.existsById(alvoId);
            case COMENTARIO -> comentarioRepository.existsById(alvoId);
            case USUARIO -> usuarioRepository.existsById(alvoId);
        };
        if (!existe) {
            throw new ResourceNotFoundException("Alvo da denúncia não encontrado");
        }
    }

    private DenunciaResponseDTO converterDenuncia(Denuncia denuncia) {
        DenunciaResponseDTO dto = new DenunciaResponseDTO();
        dto.setId(denuncia.getId());
        dto.setTipoAlvo(denuncia.getTipoAlvo());
        dto.setAlvoId(denuncia.getAlvoId());
        dto.setMotivo(denuncia.getMotivo());
        dto.setStatus(denuncia.getStatus());
        dto.setDataCriacao(denuncia.getDataCriacao());
        return dto;
    }

    private BloqueioResponseDTO converterBloqueio(Bloqueio bloqueio) {
        BloqueioResponseDTO dto = new BloqueioResponseDTO();
        dto.setId(bloqueio.getId());
        dto.setBloqueadoId(bloqueio.getBloqueado().getId());
        dto.setBloqueadoUsername(bloqueio.getBloqueado().getUsername());
        dto.setBloqueadoNome(bloqueio.getBloqueado().getNome());
        dto.setDataCriacao(bloqueio.getDataCriacao());
        return dto;
    }
}
