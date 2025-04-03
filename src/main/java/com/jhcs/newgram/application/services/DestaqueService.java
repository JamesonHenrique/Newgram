package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.destaque.DestaqueCreateDTO;
import com.jhcs.newgram.application.dtos.destaque.DestaqueResponseDTO;
import com.jhcs.newgram.application.dtos.destaque.DestaqueSummaryDTO;
import com.jhcs.newgram.application.dtos.destaque.DestaqueUpdateDTO;
import com.jhcs.newgram.application.dtos.storie.StorieResponseDTO;
import com.jhcs.newgram.core.domain.entities.Arquivo;
import com.jhcs.newgram.core.domain.entities.Destaque;
import com.jhcs.newgram.core.domain.entities.Storie;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.DestaqueRepository;
import com.jhcs.newgram.core.domain.repositories.StorieRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DestaqueService {

    @Autowired
    private DestaqueRepository destaqueRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private StorieRepository storieRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Transactional
    public DestaqueResponseDTO criarDestaque(DestaqueCreateDTO dto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Destaque destaque = new Destaque();
        destaque.setNome(dto.getNome());
        destaque.setDataCriacao(new Date());
        destaque.setUsuario(usuario);
        destaque.setStories(new ArrayList<>());

        // Adicionar stories ao destaque, se fornecidos
        if (dto.getStoriesIds() != null && !dto.getStoriesIds().isEmpty()) {
            for (Long storieId : dto.getStoriesIds()) {
                Storie storie = storieRepository.findById(storieId)
                        .orElseThrow(() -> new ResourceNotFoundException("Storie não encontrado: " + storieId));

                // Verificar se o story pertence ao usuário
                if (!storie.getAutor().getId().equals(usuarioId)) {
                    throw new BusinessException("Você só pode adicionar seus próprios stories ao destaque");
                }

                destaque.getStories().add(storie);
            }
        }

        destaque = destaqueRepository.save(destaque);
        return converterParaResponseDTO(destaque, true);
    }

    @Transactional
    public DestaqueResponseDTO atualizarDestaque(Long destaqueId, DestaqueUpdateDTO dto, Long usuarioId) {
        Destaque destaque = destaqueRepository.findByIdWithStories(destaqueId);

        if (destaque == null) {
            throw new ResourceNotFoundException("Destaque não encontrado");
        }

        if (!destaque.getUsuario().getId().equals(usuarioId)) {
            throw new BusinessException("Você não tem permissão para editar este destaque");
        }

        if (dto.getNome() != null && !dto.getNome().isEmpty()) {
            destaque.setNome(dto.getNome());
        }


        // Adicionar novos stories
        if (dto.getStoriesParaAdicionar() != null && !dto.getStoriesParaAdicionar().isEmpty()) {
            for (Long storieId : dto.getStoriesParaAdicionar()) {
                Storie storie = storieRepository.findById(storieId)
                        .orElseThrow(() -> new ResourceNotFoundException("Storie não encontrado: " + storieId));

                // Verificar se o story pertence ao usuário
                if (!storie.getAutor().getId().equals(usuarioId)) {
                    throw new BusinessException("Você só pode adicionar seus próprios stories ao destaque");
                }

                if (!destaque.getStories().contains(storie)) {
                    destaque.getStories().add(storie);
                }
            }
        }

        // Remover stories
        if (dto.getStoriesParaRemover() != null && !dto.getStoriesParaRemover().isEmpty()) {
            for (Long storieId : dto.getStoriesParaRemover()) {
                destaque.getStories().removeIf(storie -> storie.getId().equals(storieId));
            }
        }

        destaque = destaqueRepository.save(destaque);
        return converterParaResponseDTO(destaque, true);
    }

    @Transactional
    public void excluirDestaque(Long destaqueId, Long usuarioId) {
        Destaque destaque = destaqueRepository.findById(destaqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Destaque não encontrado"));

        if (!destaque.getUsuario().getId().equals(usuarioId)) {
            throw new BusinessException("Você não tem permissão para excluir este destaque");
        }

        // Remover a capa do destaque, se existir
        arquivoService.excluirArquivosPorEntidade(Arquivo.TipoEntidadeRelacionada.DESTAQUE, destaqueId);

        destaqueRepository.delete(destaque);
    }

    @Transactional(readOnly = true)
    public List<DestaqueSummaryDTO> listarDestaquesPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        List<Destaque> destaques = destaqueRepository.findByUsuarioIdOrderByNome(usuarioId);
        return destaques.stream()
                .map(this::converterParaSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DestaqueResponseDTO buscarDestaquePorId(Long destaqueId, Long usuarioLogadoId) {
        Destaque destaque = destaqueRepository.findByIdWithStories(destaqueId);

        if (destaque == null) {
            throw new ResourceNotFoundException("Destaque não encontrado");
        }

        // Verificar se o usuário tem permissão para ver todos os detalhes (stories)
        boolean mostrarDetalhes = destaque.getUsuario().getId().equals(usuarioLogadoId);

        return converterParaResponseDTO(destaque, mostrarDetalhes);
    }

    @Transactional
    public DestaqueResponseDTO adicionarCapaDestaque(Long destaqueId, MultipartFile arquivo, Long usuarioId) {
        Destaque destaque = destaqueRepository.findById(destaqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Destaque não encontrado"));

        if (!destaque.getUsuario().getId().equals(usuarioId)) {
            throw new BusinessException("Você não tem permissão para editar este destaque");
        }

        // Remover capa antiga, se existir
        arquivoService.excluirArquivosPorEntidade(Arquivo.TipoEntidadeRelacionada.DESTAQUE, destaqueId);

        // Salvar nova capa
        arquivoService.uploadArquivo(arquivo, Arquivo.TipoEntidadeRelacionada.DESTAQUE, destaqueId);

        return buscarDestaquePorId(destaqueId, usuarioId);
    }

    @Transactional(readOnly = true)
    public Long contarStoriesPorDestaque(Long destaqueId) {
        if (!destaqueRepository.existsById(destaqueId)) {
            throw new ResourceNotFoundException("Destaque não encontrado");
        }

        return destaqueRepository.countStoriesByDestaqueId(destaqueId);
    }

    @Transactional(readOnly = true)
    public List<StorieResponseDTO> listarStoriesPorDestaque(Long destaqueId, Long usuarioLogadoId) {
        Destaque destaque = destaqueRepository.findById(destaqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Destaque não encontrado"));

        // Verificar se o usuário tem permissão para ver os stories
        if (!destaque.getUsuario().getId().equals(usuarioLogadoId)) {
            throw new BusinessException("Você não tem permissão para visualizar os stories deste destaque");
        }

        List<Storie> stories = destaqueRepository.findStoriesByDestaqueId(destaqueId);
        return stories.stream()
                .map(storie -> converterParaStorieResponseDTO(storie, usuarioLogadoId))
                .collect(Collectors.toList());
    }

    private DestaqueSummaryDTO converterParaSummaryDTO(Destaque destaque) {
        DestaqueSummaryDTO dto = new DestaqueSummaryDTO();
        dto.setId(destaque.getId());
        dto.setNome(destaque.getNome());
        dto.setDataCriacao(destaque.getDataCriacao());

        // Obter quantidade de stories
        Long quantidadeStories = destaqueRepository.countStoriesByDestaqueId(destaque.getId());
        dto.setQuantidadeStories(quantidadeStories.intValue());

        // Obter capa do destaque
        List<ArquivoDTO> arquivos = arquivoService.buscarArquivosPorEntidade(
                Arquivo.TipoEntidadeRelacionada.DESTAQUE,
                destaque.getId()
        );
        if (!arquivos.isEmpty()) {
            dto.setCapaDestaque(arquivos.get(0));
        }

        return dto;
    }

    private DestaqueResponseDTO converterParaResponseDTO(Destaque destaque, boolean incluirStories) {
        DestaqueResponseDTO dto = new DestaqueResponseDTO();
        dto.setId(destaque.getId());
        dto.setNome(destaque.getNome());
        dto.setDataCriacao(destaque.getDataCriacao());
        dto.setUsuarioId(destaque.getUsuario().getId());
        dto.setUsernameUsuario(destaque.getUsuario().getUsername());

        // Obter quantidade de stories
        Long quantidadeStories = destaqueRepository.countStoriesByDestaqueId(destaque.getId());
        dto.setQuantidadeStories(quantidadeStories.intValue());

        // Obter capa do destaque
        List<ArquivoDTO> arquivos = arquivoService.buscarArquivosPorEntidade(
                Arquivo.TipoEntidadeRelacionada.DESTAQUE,
                destaque.getId()
        );
        if (!arquivos.isEmpty()) {
            dto.setCapaDestaque(arquivos.get(0));
        }

        // Incluir os stories, se solicitado
        if (incluirStories && destaque.getStories() != null) {
            dto.setStories(destaque.getStories().stream()
                    .map(storie -> converterParaStorieResponseDTO(storie, destaque.getUsuario().getId()))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private StorieResponseDTO converterParaStorieResponseDTO(Storie storie, Long usuarioLogadoId) {
        StorieResponseDTO dto = new StorieResponseDTO();
        dto.setId(storie.getId());
        dto.setDataCriacao(storie.getDataCriacao());
        dto.setDataExpiracao(storie.getDataExpiracao());
        dto.setDestacado(storie.isDestacado());

        // Verificar se o storie foi visualizado pelo usuário
        boolean visualizado = storie.getVisualizadoPor().stream()
                .anyMatch(usuario -> usuario.getId().equals(usuarioLogadoId));
        dto.setVisualizadoPeloUsuario(visualizado);

        // Contagem de visualizações
        dto.setNumeroVisualizacoes((long) storie.getVisualizadoPor().size());

        // Obter mídia do storie
        List<ArquivoDTO> arquivos = arquivoService.buscarArquivosPorEntidade(
                Arquivo.TipoEntidadeRelacionada.STORIE,
                storie.getId()
        );
        if (!arquivos.isEmpty()) {
            dto.setMidia(arquivos.get(0));
        }

        return dto;
    }
}