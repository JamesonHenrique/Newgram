package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.post.PostCreateDTO;
import com.jhcs.newgram.application.dtos.post.PostResponseDTO;
import com.jhcs.newgram.application.dtos.post.PostSummaryDTO;
import com.jhcs.newgram.application.dtos.post.PostUpdateDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.*;
import com.jhcs.newgram.core.domain.enums.TipoArquivo;
import com.jhcs.newgram.core.domain.repositories.*;
import com.jhcs.newgram.infrastructure.aws.S3StorageService;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private CurtidaRepository curtidaRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private SalvosRepository salvosRepository;

    @Autowired
    private ArquivoService arquivoService;
    @Autowired
    private S3StorageService s3StorageService;
    @Transactional
    public PostResponseDTO criarPost(PostCreateDTO dto, Long usuarioId) {
        Usuario autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Post post = new Post();
        post.setLegenda(dto.getLegenda());
        post.setLocalizacao(dto.getLocalizacao());
        post.setVisibilidade(dto.getVisibilidade());
        post.setAutor(autor);
        post.setDataCriacao(LocalDateTime.now());
        post.setArquivado(false);

        if (dto.getHashtags() != null && !dto.getHashtags().isEmpty()) {
            processarHashtags(post, dto.getHashtags());
        } else if (dto.getLegenda() != null) {
            processarHashtagsDaLegenda(post, dto.getLegenda());
        }



        post = postRepository.save(post);

        return converterParaResponseDTO(post, usuarioId);
    }

    @Transactional
    public PostResponseDTO atualizarPost(Long postId, PostUpdateDTO dto, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        Support.requireOwner(post.getAutor().getId(), usuarioId, "Você não tem permissão para editar este post");

        if (dto.getLegenda() != null) {
            post.setLegenda(dto.getLegenda());
        }

        if (dto.getLocalizacao() != null) {
            post.setLocalizacao(dto.getLocalizacao());
        }

        if (dto.getVisibilidade() != null) {
            post.setVisibilidade(dto.getVisibilidade());
        }

        if (dto.getHashtags() != null) {
            post.getHashtags().clear();
            processarHashtags(post, dto.getHashtags());
        }

        if (dto.getUsuariosMarcados() != null) {
            post.getMarcacoes().clear();
            processarMarcacoesUsuarios(post, dto.getUsuariosMarcados());
        }

        post = postRepository.save(post);

        return converterParaResponseDTO(post, usuarioId);
    }

    @Transactional
    public void excluirPost(Long postId, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        Support.requireOwner(post.getAutor().getId(), usuarioId, "Você não tem permissão para excluir este post");

        postRepository.delete(post);
    }

    @Transactional
    public PostResponseDTO arquivarPost(Long postId, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));


        Support.requireOwner(post.getAutor().getId(), usuarioId, "Você não tem permissão para arquivar este post");

        post.setArquivado(true);
        post = postRepository.save(post);

        return converterParaResponseDTO(post, usuarioId);
    }

    @Transactional
    public PostResponseDTO desarquivarPost(Long postId, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        Support.requireOwner(post.getAutor().getId(), usuarioId, "Você não tem permissão para desarquivar este post");

        post.setArquivado(false);
        post = postRepository.save(post);

        return converterParaResponseDTO(post, usuarioId);
    }

    @Transactional(readOnly = true)
    public PostResponseDTO buscarPorId(Long postId, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        return converterParaResponseDTO(post, usuarioId);
    }
    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsRecomendados(Long usuarioId, Pageable pageable) {

        Pageable safePageable = Support.safePage(pageable);
        Page<Post> posts = postRepository.buscarPostsRecomendadosParaUsuario(
                usuarioId,
                safePageable
        );


        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }
    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsPorLegenda(String termo, Pageable pageable, Long usuarioId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Post> posts = postRepository.buscarPostsPorLegenda(termo, safePageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }
    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsDoUsuario(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Post> posts = postRepository.findByAutorId(usuarioId, safePageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarFeedDoUsuario(Long usuarioId, Pageable pageable) {
        Pageable safePageable = Support.safePage(pageable);
        Page<Post> posts = postRepository.findFeedByUsuarioId(usuarioId, safePageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsPopulares(Pageable pageable, Long usuarioId) {

        Pageable safePageable = Support.safePage(pageable);
        Page<Post> posts = postRepository.findPostsPopulares(safePageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }
    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsTendencias(Pageable pageable, Long usuarioId) {
        LocalDateTime dataCorte = LocalDateTime.now().minusHours(48);
        int minimoInteracoes = 10;

        Pageable safePageable = Support.safePage(pageable);
        Page<Post> posts = postRepository.findPostsTendencias(
                dataCorte,
                minimoInteracoes,
                safePageable
        );

        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }
    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsPopularesSeguidores(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);

        Page<Post> posts = postRepository.findPopularPostsFromFollowing(usuarioId, safePageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }
    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsPorHashtag(String hashtag, Pageable pageable, Long usuarioId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Post> posts = postRepository.findByHashtag(hashtag, safePageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsPorLocalizacao(String localizacao, Pageable pageable, Long usuarioId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Post> posts = postRepository.findByLocalizacao(localizacao, safePageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsSalvos(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Post> posts = postRepository.findSalvosByUsuarioId(usuarioId, safePageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsSalvosPorColecao(Long usuarioId, String colecao, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Post> posts = postRepository.findSalvosByUsuarioIdAndColecao(usuarioId, colecao, safePageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional
    public PostResponseDTO curtirPost(Long postId, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Curtida curtida = new Curtida();
        curtida.setUsuario(usuario);
        curtida.setPost(post);
        curtida.setDataCriacao(LocalDateTime.now());

        try {
            curtidaRepository.saveAndFlush(curtida);
        } catch (DataIntegrityViolationException e) {
            // Corrida entre exists e save (curtida única por usuário/post).
            throw new BusinessException("Você já curtiu este post", e);
        }

        // Criar notificação para o autor do post
        // notificacaoService.criarNotificacaoCurtida(usuario, post);

        return converterParaResponseDTO(post, usuarioId);
    }

    @Transactional
    public PostResponseDTO descurtirPost(Long postId, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        if (!curtidaRepository.existsByUsuarioIdAndPostId(usuarioId, postId)) {
            throw new BusinessException("Você não curtiu este post");
        }

        curtidaRepository.deleteByUsuarioIdAndPostId(usuarioId, postId);

        return converterParaResponseDTO(post, usuarioId);
    }

    @Transactional
    public void salvarPost(Long postId, Long usuarioId, String colecao) {
        // UPSERT idempotente: já salvo apenas atualiza a coleção (unificado com SalvosService).
        Optional<Salvos> existente = salvosRepository.findByUsuarioIdAndPostId(usuarioId, postId);
        if (existente.isPresent()) {
            Salvos salvoExistente = existente.get();
            if (colecao != null && !colecao.isEmpty()) {
                salvoExistente.setColecao(colecao);
                salvosRepository.save(salvoExistente);
            }
            return;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Salvos salvo = new Salvos();
        salvo.setUsuario(usuario);
        salvo.setPost(post);
        salvo.setDataSalvo(LocalDateTime.now());
        salvo.setColecao(colecao);

        try {
            salvosRepository.saveAndFlush(salvo);
        } catch (DataIntegrityViolationException e) {
            // Corrida: outro request salvou entre o find e o save → atualiza a coleção.
            salvosRepository.findByUsuarioIdAndPostId(usuarioId, postId).ifPresent(s -> {
                if (colecao != null && !colecao.isEmpty()) {
                    s.setColecao(colecao);
                    salvosRepository.save(s);
                }
            });
        }
    }

    @Transactional
    public void removerPostSalvo(Long postId, Long usuarioId) {
        if (!salvosRepository.existsByUsuarioIdAndPostId(usuarioId, postId)) {
            throw new BusinessException("Você não salvou este post");
        }

        salvosRepository.deleteByUsuarioIdAndPostId(usuarioId, postId);
    }

    @Transactional
    public void salvarImagemDoPost(Long postId, MultipartFile file, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum usuário encontrado com o ID: " + usuarioId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum post encontrado com o ID: " + postId));

        Support.requireOwner(post.getAutor().getId(), usuarioId, "Você não tem permissão para adicionar imagem a este post");

        var imagemPost = arquivoService.saveFile(file, usuario.getUsuarioName(), TipoArquivo.POST);
        post.setImagemUrl(imagemPost);

        postRepository.save(post);
    }
    private void processarHashtags(Post post, List<String> hashtags) {
        List<Hashtag> hashtagEntities = new ArrayList<>();

        for (String tagNome : hashtags) {
            if (tagNome.startsWith("#")) {
                tagNome = tagNome.substring(1);
            }

            String finalTagNome = tagNome;
            Hashtag hashtag = hashtagRepository.findByNome(tagNome)
                    .orElseGet(() -> {
                        Hashtag novaTag = new Hashtag();
                        novaTag.setNome(finalTagNome);
                        return hashtagRepository.save(novaTag);
                    });

            hashtagEntities.add(hashtag);
        }

        post.setHashtags(hashtagEntities);
    }

    private void processarHashtagsDaLegenda(Post post, String legenda) {
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(legenda);
        List<String> hashtags = new ArrayList<>();

        while (matcher.find()) {
            hashtags.add(matcher.group(1));
        }

        if (!hashtags.isEmpty()) {
            processarHashtags(post, hashtags);
        }
    }

    private void processarMarcacoesUsuarios(Post post, List<Long> usuariosIds) {
        List<Usuario> usuarios = new ArrayList<>();

        for (Long usuarioId : usuariosIds) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + usuarioId + " não encontrado"));
            usuarios.add(usuario);
        }

        post.setMarcacoes(usuarios);
    }

    private PostResponseDTO converterParaResponseDTO(Post post, Long usuarioLogadoId) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setLegenda(post.getLegenda());
        dto.setDataCriacao(post.getDataCriacao());
        dto.setLocalizacao(post.getLocalizacao());
        dto.setVisibilidade(post.getVisibilidade());
        dto.setArquivado(post.isArquivado());

        UsuarioSummaryDTO autorDTO = new UsuarioSummaryDTO();
        autorDTO.setId(post.getAutor().getId());
        autorDTO.setNome(post.getAutor().getNome());
        autorDTO.setUsername(post.getAutor().getUsername());
        autorDTO.setFotoPerfil((s3StorageService.getFileUrl(post.getAutor().getFotoPerfil())));
        dto.setAutor(autorDTO);

        List<String> hashtags = post.getHashtags().stream()
                .map(Hashtag::getNome)
                .collect(Collectors.toList());
        dto.setHashtags(hashtags);

        List<UsuarioSummaryDTO> usuariosMarcados = post.getMarcacoes().stream()
                .map(usuario -> {
                    UsuarioSummaryDTO userDto = new UsuarioSummaryDTO();
                    userDto.setId(usuario.getId());
                    userDto.setNome(usuario.getNome());
                    userDto.setUsername(usuario.getUsername());
                    return userDto;
                })
                .collect(Collectors.toList());
        dto.setUsuariosMarcados(usuariosMarcados);

        dto.setNumeroCurtidas(curtidaRepository.countByPostId(post.getId()));
        dto.setNumeroComentarios(comentarioRepository.countByPostId(post.getId()));

        if (usuarioLogadoId != null) {
            dto.setCurtidoPeloUsuario(curtidaRepository.existsByUsuarioIdAndPostId(usuarioLogadoId, post.getId()));
            dto.setSalvoPeloUsuario(salvosRepository.existsByUsuarioIdAndPostId(usuarioLogadoId, post.getId()));
        }

        return dto;
    }

    private PostSummaryDTO converterParaSummaryDTO(Post post, Long usuarioLogadoId) {
        PostSummaryDTO dto = new PostSummaryDTO();
        dto.setId(post.getId());
        dto.setImagem(s3StorageService.getFileUrl(post.getImagemUrl()));
        dto.setDataCriacao(post.getDataCriacao());
        UsuarioSummaryDTO autorDTO = new UsuarioSummaryDTO();
        autorDTO.setId(post.getAutor().getId());
        autorDTO.setNome(post.getAutor().getNome());
        autorDTO.setUsername(post.getAutor().getUsername());
        autorDTO.setFotoPerfil((s3StorageService.getFileUrl(post.getAutor().getFotoPerfil())));
        dto.setAutor(autorDTO);
        dto.setLegenda(post.getLegenda());
        dto.setLocalizacao(post.getLocalizacao());
        dto.setNumeroCurtidas(curtidaRepository.countByPostId(post.getId()));
        dto.setNumeroComentarios(comentarioRepository.countByPostId(post.getId()));
        if (usuarioLogadoId != null) {
            // Direto no repository: evita hop inter-service com TX aninhada.
            dto.setCurtidoPeloUsuario(curtidaRepository.existsByUsuarioIdAndPostId(usuarioLogadoId, post.getId()));
            dto.setSalvoPeloUsuario(salvosRepository.existsByUsuarioIdAndPostId(usuarioLogadoId, post.getId()));
        } else {
            dto.setCurtidoPeloUsuario(false);
            dto.setSalvoPeloUsuario(false);
        }

        return dto;
    }
}