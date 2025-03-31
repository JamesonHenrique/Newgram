package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.arquivo.ArquivoUploadResponseDTO;
import com.jhcs.newgram.application.dtos.storie.StorieCreateDTO;
import com.jhcs.newgram.application.dtos.storie.StorieResponseDTO;
import com.jhcs.newgram.application.dtos.storie.StorieVisualizacaoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.Arquivo;
import com.jhcs.newgram.core.domain.entities.Destaque;
import com.jhcs.newgram.core.domain.entities.Storie;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.DestaqueRepository;
import com.jhcs.newgram.core.domain.repositories.StorieRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StorieService {

    @Autowired
    private StorieRepository storieRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DestaqueRepository destaqueRepository;

    @Autowired
    private ArquivoService arquivoService;

    private static final int DURACAO_STORIE_HORAS = 24;

    @Transactional
    public StorieResponseDTO criarStorie(StorieCreateDTO dto, Long usuarioId) {
        Usuario autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Criar o storie primeiro para obter o ID
        Storie storie = new Storie();

        storie.setAutor(autor);
        storie.setDestacado(dto.isDestacar());

        // Definir data de criação e expiração
        Date agora = new Date();
        storie.setDataCriacao(agora);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(agora);
        calendar.add(Calendar.HOUR, DURACAO_STORIE_HORAS);
        storie.setDataExpiracao(calendar.getTime());

        storie = storieRepository.save(storie);

        // Fazer upload do arquivo
        ArquivoUploadResponseDTO arquivoResponse = arquivoService.uploadArquivo(
                dto.getMidia(),
                Arquivo.TipoEntidadeRelacionada.STORIE,
                storie.getId()
        );

        // Processar marcações de usuários
        if (dto.getUsuariosMarcados() != null && !dto.getUsuariosMarcados().isEmpty()) {
            processarMarcacoesUsuarios(storie, dto.getUsuariosMarcados());
        }

        // Se for destacado, criar ou adicionar a um destaque existente
        if (dto.isDestacar()) {
            List<Destaque> destaques = destaqueRepository.findByUsuarioIdOrderByNome(usuarioId);
            if (!destaques.isEmpty()) {
                // Adicionar ao primeiro destaque por padrão ou criar lógica para seleção
                Destaque destaque = destaques.get(0);
                destaque.getStories().add(storie);
                destaqueRepository.save(destaque);
            }
        }

        // Salvar as alterações
        storie = storieRepository.save(storie);

        return converterParaResponseDTO(storie, usuarioId);
    }

    @Transactional(readOnly = true)
    public StorieResponseDTO buscarPorId(Long storieId, Long usuarioId) {
        Storie storie = storieRepository.findById(storieId)
                .orElseThrow(() -> new ResourceNotFoundException("Storie não encontrado"));

        return converterParaResponseDTO(storie, usuarioId);
    }

    @Transactional
    public void excluirStorie(Long storieId, Long usuarioId) {
        Storie storie = storieRepository.findById(storieId)
                .orElseThrow(() -> new ResourceNotFoundException("Storie não encontrado"));

        // Verificar se o usuário é o autor do storie
        if (!storie.getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para excluir este storie");
        }

        // Excluir arquivos relacionados
        arquivoService.excluirArquivosPorEntidade(Arquivo.TipoEntidadeRelacionada.STORIE, storieId);

        // Excluir storie
        storieRepository.delete(storie);
    }

    @Transactional(readOnly = true)
    public List<StorieResponseDTO> listarStoriesDeSeguidosAtivos(Long usuarioId) {
        Date agora = new Date();
        List<Usuario> usuariosComStories = storieRepository.findUsuariosComStoriesAtivos(usuarioId, agora);

        List<StorieResponseDTO> resultado = new ArrayList<>();

        for (Usuario usuario : usuariosComStories) {
            List<Storie> stories = storieRepository.findByAutorIdAndDataExpiracaoAfterOrderByDataCriacaoDesc(
                    usuario.getId(), agora);

            for (Storie storie : stories) {
                resultado.add(converterParaResponseDTO(storie, usuarioId));
            }
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<StorieResponseDTO> listarStoriesDoUsuario(Long autorId, Long usuarioLogadoId) {
        Date agora = new Date();
        List<Storie> stories = storieRepository.findByAutorIdAndDataExpiracaoAfterOrderByDataCriacaoDesc(autorId, agora);

        return stories.stream()
                .map(storie -> converterParaResponseDTO(storie, usuarioLogadoId))
                .collect(Collectors.toList());
    }

    @Transactional
    public StorieResponseDTO marcarComoVisualizado(Long storieId, Long usuarioId) {
        Storie storie = storieRepository.findById(storieId)
                .orElseThrow(() -> new ResourceNotFoundException("Storie não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (storie.getVisualizadoPor().contains(usuario)) {
            throw new BusinessException("Você já visualizou este storie");
        }

        storie.getVisualizadoPor().add(usuario);
        storie = storieRepository.save(storie);

        return converterParaResponseDTO(storie, usuarioId);
    }

    @Transactional
    public StorieResponseDTO destacarStorie(Long storieId, Long destaqueId, Long usuarioId) {
        Storie storie = storieRepository.findById(storieId)
                .orElseThrow(() -> new ResourceNotFoundException("Storie não encontrado"));

        // Verificar se o usuário é o autor do storie
        if (!storie.getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para destacar este storie");
        }

        Destaque destaque = destaqueRepository.findById(destaqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Destaque não encontrado"));

        // Verificar se o usuário é dono do destaque
        if (!destaque.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para editar este destaque");
        }

        // Adicionar ao destaque
        destaque.getStories().add(storie);
        destaqueRepository.save(destaque);

        // Marcar como destacado
        storie.setDestacado(true);
        storie = storieRepository.save(storie);

        return converterParaResponseDTO(storie, usuarioId);
    }

    @Transactional(readOnly = true)
    public List<StorieResponseDTO> listarStoriesDestacados(Long usuarioId) {
        List<Storie> stories = storieRepository.findStoriesDestacados(usuarioId);

        return stories.stream()
                .map(storie -> converterParaResponseDTO(storie, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StorieResponseDTO> listarStoriesNaoVisualizados(Long usuarioId, Long autorId) {
        Date agora = new Date();
        List<Storie> stories = storieRepository.findStoriesNaoVisualizados(usuarioId, autorId, agora);

        return stories.stream()
                .map(storie -> converterParaResponseDTO(storie, usuarioId))
                .collect(Collectors.toList());
    }

    // Métodos auxiliares

    private void processarMarcacoesUsuarios(Storie storie, List<Long> usuariosIds) {
        List<Usuario> usuarios = new ArrayList<>();

        for (Long usuarioId : usuariosIds) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + usuarioId + " não encontrado"));
            usuarios.add(usuario);

            // Aqui poderia adicionar notificação para o usuário marcado
            // notificacaoService.criarNotificacaoMarcacaoStorie(storie.getAutor(), usuario, storie);
        }

        storie.setMarcacoes(usuarios);
    }

    private StorieResponseDTO converterParaResponseDTO(Storie storie, Long usuarioLogadoId) {
        StorieResponseDTO dto = new StorieResponseDTO();
        dto.setId(storie.getId());

        dto.setDataCriacao(storie.getDataCriacao());
        dto.setDataExpiracao(storie.getDataExpiracao());

        dto.setDestacado(storie.isDestacado());

        // Buscar arquivo de mídia
        List<ArquivoDTO> arquivos = arquivoService.buscarArquivosPorEntidade(
                Arquivo.TipoEntidadeRelacionada.STORIE,
                storie.getId()
        );

        if (!arquivos.isEmpty()) {
            dto.setMidia(arquivos.get(0));
        }

        // Autor
        UsuarioSummaryDTO autorDTO = new UsuarioSummaryDTO();
        autorDTO.setId(storie.getAutor().getId());
        autorDTO.setNome(storie.getAutor().getNome());
        autorDTO.setUsername(storie.getAutor().getUsername());
        autorDTO.setVerificado(storie.getAutor().isVerificado());
        dto.setAutor(autorDTO);

        // Usuários marcados
        if (storie.getMarcacoes() != null) {
            List<UsuarioSummaryDTO> usuariosMarcados = storie.getMarcacoes().stream()
                    .map(usuario -> {
                        UsuarioSummaryDTO userDto = new UsuarioSummaryDTO();
                        userDto.setId(usuario.getId());
                        userDto.setNome(usuario.getNome());
                        userDto.setUsername(usuario.getUsername());
                        userDto.setVerificado(usuario.isVerificado());
                        return userDto;
                    })
                    .collect(Collectors.toList());
            dto.setUsuariosMarcados(usuariosMarcados);
        }

        // Número de visualizações
        dto.setNumeroVisualizacoes((long) storie.getVisualizadoPor().size());

        // Verificar se o usuário logado já visualizou o storie
        if (usuarioLogadoId != null) {
            boolean visualizado = storie.getVisualizadoPor().stream()
                    .anyMatch(usuario -> usuario.getId().equals(usuarioLogadoId));
            dto.setVisualizadoPeloUsuario(visualizado);
        }

        return dto;
    }
}