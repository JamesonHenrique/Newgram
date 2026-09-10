package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.storie.StorieCreateDTO;
import com.jhcs.newgram.application.dtos.storie.StorieResponseDTO;
import com.jhcs.newgram.core.domain.entities.Destaque;
import com.jhcs.newgram.core.domain.entities.Storie;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.TipoArquivo;
import com.jhcs.newgram.core.domain.repositories.DestaqueRepository;
import com.jhcs.newgram.core.domain.repositories.StorieRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.aws.S3StorageService;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    @Autowired
    private S3StorageService s3StorageService;

    private static final int DURACAO_STORIE_HORAS = 24;

    @Transactional
    public StorieResponseDTO criarStorie(StorieCreateDTO dto, Long usuarioId) {
        Usuario autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));


        Storie storie = new Storie();

        storie.setAutor(autor);
        storie.setDestacado(dto.isDestacar());


        Date agora = new Date();
        storie.setDataCriacao(agora);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(agora);
        calendar.add(Calendar.HOUR, DURACAO_STORIE_HORAS);
        storie.setDataExpiracao(calendar.getTime());

        storie = storieRepository.save(storie);

        if (dto.getImagem() != null && !dto.getImagem().isEmpty()) {
            salvarImagem(storie.getId(), usuarioId, dto.getImagem());
        }


        if (dto.isDestacar()) {
            List<Destaque> destaques = destaqueRepository.findByUsuarioIdOrderByNome(usuarioId);
            if (!destaques.isEmpty()) {

                Destaque destaque = destaques.get(0);
                destaque.getStories().add(storie);
                destaqueRepository.save(destaque);
            }
        }


        storie = storieRepository.save(storie);

        return converterParaResponseDTO(storie, usuarioId);
    }

    public void salvarImagem(Long storieId, Long usuarioId, MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum usuario encontrado com o ID: " + usuarioId));
        Storie storie = storieRepository.findById(storieId)
                .orElseThrow(() -> new EntityNotFoundException("Nenhum storie encontrado com o ID: " + storieId));
        if (!storie.getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para adicionar imagem a este storie");
        }
        var imagem = arquivoService.saveFile(file, usuario.getUsuarioName(), TipoArquivo.STORIE);
        storie.setStorieImagemUrl(imagem);
        storieRepository.save(storie);
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

        if (!storie.getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para excluir este storie");
        }


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


        storie = storieRepository.save(storie);

        return converterParaResponseDTO(storie, usuarioId);
    }

    @Transactional
    public StorieResponseDTO destacarStorie(Long storieId, Long destaqueId, Long usuarioId) {
        Storie storie = storieRepository.findById(storieId)
                .orElseThrow(() -> new ResourceNotFoundException("Storie não encontrado"));

        if (!storie.getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para destacar este storie");
        }

        Destaque destaque = destaqueRepository.findById(destaqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Destaque não encontrado"));

        if (!destaque.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para editar este destaque");
        }

        destaque.getStories().add(storie);
        destaqueRepository.save(destaque);

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


    private void processarMarcacoesUsuarios(Storie storie, List<Long> usuariosIds) {
        List<Usuario> usuarios = new ArrayList<>();

        for (Long usuarioId : usuariosIds) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + usuarioId + " não encontrado"));
            usuarios.add(usuario);


        }


    }

    private StorieResponseDTO converterParaResponseDTO(Storie storie, Long usuarioLogadoId) {
        StorieResponseDTO dto = new StorieResponseDTO();
        dto.setId(storie.getId());
        dto.setDataCriacao(storie.getDataCriacao());
        dto.setDataExpiracao(storie.getDataExpiracao());
        dto.setDestacado(storie.isDestacado());
        dto.setStorieImagemUrl(s3StorageService.getFileUrl(storie.getStorieImagemUrl()));
        dto.setAutorId(storie.getAutor().getId());
        dto.setAutorUsername(storie.getAutor().getUsername());

        return dto;
    }
}