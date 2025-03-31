package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.post.PostCreateDTO;
import com.jhcs.newgram.application.dtos.post.PostResponseDTO;
import com.jhcs.newgram.application.dtos.post.PostSummaryDTO;
import com.jhcs.newgram.application.dtos.post.PostUpdateDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.*;
import com.jhcs.newgram.core.domain.repositories.ComentarioRepository;
import com.jhcs.newgram.core.domain.repositories.CurtidaRepository;
import com.jhcs.newgram.core.domain.repositories.HashtagRepository;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.core.domain.repositories.SalvosRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
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
    private UsuarioService usuarioService;

    @Autowired
    private ArquivoService arquivoService;

    @Transactional
    public PostResponseDTO criarPost(PostCreateDTO dto, Long usuarioId) {
        Usuario autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Post post = new Post();
        post.setLegenda(dto.getLegenda());
        post.setLocalizacao(dto.getLocalizacao());
        post.setVisibilidade(dto.getVisibilidade());
        post.setAutor(autor);
        post.setDataCriacao(new Date());
        post.setArquivado(false);

        // Processar hashtags
        if (dto.getHashtags() != null && !dto.getHashtags().isEmpty()) {
            processarHashtags(post, dto.getHashtags());
        } else if (dto.getLegenda() != null) {
            processarHashtagsDaLegenda(post, dto.getLegenda());
        }

        // Processar marcações
        if (dto.getUsuariosMarcados() != null && !dto.getUsuariosMarcados().isEmpty()) {
            processarMarcacoesUsuarios(post, dto.getUsuariosMarcados());
        }

        post = postRepository.save(post);
        if (dto.getArquivos() != null && !dto.getArquivos().isEmpty()) {
            for (MultipartFile arquivo : dto.getArquivos()) {
                arquivoService.uploadArquivo(
                        arquivo,
                        Arquivo.TipoEntidadeRelacionada.POST,
                        post.getId()
                );
            }
        }
        return converterParaResponseDTO(post, usuarioId);
    }

    @Transactional
    public PostResponseDTO atualizarPost(Long postId, PostUpdateDTO dto, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        // Verificar se o usuário é o autor do post
        if (!post.getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para editar este post");
        }

        if (dto.getLegenda() != null) {
            post.setLegenda(dto.getLegenda());
        }

        if (dto.getLocalizacao() != null) {
            post.setLocalizacao(dto.getLocalizacao());
        }

        if (dto.getVisibilidade() != null) {
            post.setVisibilidade(dto.getVisibilidade());
        }

        // Atualizar hashtags
        if (dto.getHashtags() != null) {
            post.getHashtags().clear();
            processarHashtags(post, dto.getHashtags());
        }

        // Atualizar marcações
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

        // Verificar se o usuário é o autor do post
        if (!post.getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para excluir este post");
        }

        postRepository.delete(post);
    }

    @Transactional
    public PostResponseDTO arquivarPost(Long postId, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        // Verificar se o usuário é o autor do post
        if (!post.getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para arquivar este post");
        }

        post.setArquivado(true);
        post = postRepository.save(post);

        return converterParaResponseDTO(post, usuarioId);
    }

    @Transactional
    public PostResponseDTO desarquivarPost(Long postId, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        // Verificar se o usuário é o autor do post
        if (!post.getAutor().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não tem permissão para desarquivar este post");
        }

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
    public Page<PostSummaryDTO> listarPostsDoUsuario(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        Page<Post> posts = postRepository.findByAutorId(usuarioId, pageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarFeedDoUsuario(Long usuarioId, Pageable pageable) {
        Page<Post> posts = postRepository.findFeedByUsuarioId(usuarioId, pageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsPopulares(Pageable pageable, Long usuarioId) {
        Page<Post> posts = postRepository.findPostsPopulares(pageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsPorHashtag(String hashtag, Pageable pageable, Long usuarioId) {
        Page<Post> posts = postRepository.findByHashtag(hashtag, pageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsPorLocalizacao(String localizacao, Pageable pageable, Long usuarioId) {
        Page<Post> posts = postRepository.findByLocalizacao(localizacao, pageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsSalvos(Long usuarioId, Pageable pageable) {
        Page<Post> posts = postRepository.findSalvosByUsuarioId(usuarioId, pageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> listarPostsSalvosPorColecao(Long usuarioId, String colecao, Pageable pageable) {
        Page<Post> posts = postRepository.findSalvosByUsuarioIdAndColecao(usuarioId, colecao, pageable);
        return posts.map(post -> converterParaSummaryDTO(post, usuarioId));
    }

    @Transactional
    public PostResponseDTO curtirPost(Long postId, Long usuarioId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (curtidaRepository.existsByUsuarioIdAndPostId(usuarioId, postId)) {
            throw new BusinessException("Você já curtiu este post");
        }

        Curtida curtida = new Curtida();
        curtida.setUsuario(usuario);
        curtida.setPost(post);
        curtida.setDataCriacao(new Date());

        curtidaRepository.save(curtida);

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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (salvosRepository.existsByUsuarioIdAndPostId(usuarioId, postId)) {
            throw new BusinessException("Você já salvou este post");
        }

        Salvos salvo = new Salvos();
        salvo.setUsuario(usuario);
        salvo.setPost(post);
        salvo.setDataSalvo(new Date());
        salvo.setColecao(colecao);

        salvosRepository.save(salvo);
    }

    @Transactional
    public void removerPostSalvo(Long postId, Long usuarioId) {
        if (!salvosRepository.existsByUsuarioIdAndPostId(usuarioId, postId)) {
            throw new BusinessException("Você não salvou este post");
        }

        salvosRepository.deleteByUsuarioIdAndPostId(usuarioId, postId);
    }

    // Métodos auxiliares

    private void processarHashtags(Post post, List<String> hashtags) {
        List<Hashtag> hashtagEntities = new ArrayList<>();

        for (String tagNome : hashtags) {
            // Remover # do início se presente
            if (tagNome.startsWith("#")) {
                tagNome = tagNome.substring(1);
            }

            // Buscar hashtag existente ou criar nova
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

        // Autor
        UsuarioSummaryDTO autorDTO = new UsuarioSummaryDTO();
        autorDTO.setId(post.getAutor().getId());
        autorDTO.setNome(post.getAutor().getNome());
        autorDTO.setUsername(post.getAutor().getUsername());
        autorDTO.setVerificado(post.getAutor().isVerificado());
        dto.setAutor(autorDTO);

        // Hashtags
        List<String> hashtags = post.getHashtags().stream()
                .map(Hashtag::getNome)
                .collect(Collectors.toList());
        dto.setHashtags(hashtags);

        // Usuários marcados
        List<UsuarioSummaryDTO> usuariosMarcados = post.getMarcacoes().stream()
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

        // Estatísticas
        dto.setNumeroCurtidas(curtidaRepository.countByPostId(post.getId()));
        dto.setNumeroComentarios(comentarioRepository.countByPostId(post.getId()));
        // Upload das imagens usando o ArquivoService
        List<ArquivoDTO> arquivos = arquivoService.buscarArquivosPorEntidade(
                Arquivo.TipoEntidadeRelacionada.POST,
                post.getId()
        );
        dto.setArquivos(arquivos);
        // Verificar se o usuário curtiu ou salvou o post
        if (usuarioLogadoId != null) {
            dto.setCurtidoPeloUsuario(curtidaRepository.existsByUsuarioIdAndPostId(usuarioLogadoId, post.getId()));
            dto.setSalvoPeloUsuario(salvosRepository.existsByUsuarioIdAndPostId(usuarioLogadoId, post.getId()));
        }

        return dto;
    }

    private PostSummaryDTO converterParaSummaryDTO(Post post, Long usuarioLogadoId) {
        PostSummaryDTO dto = new PostSummaryDTO();
        dto.setId(post.getId());
        dto.setDataCriacao(post.getDataCriacao());

        // Autor
        UsuarioSummaryDTO autorDTO = new UsuarioSummaryDTO();
        autorDTO.setId(post.getAutor().getId());
        autorDTO.setNome(post.getAutor().getNome());
        autorDTO.setUsername(post.getAutor().getUsername());
        autorDTO.setVerificado(post.getAutor().isVerificado());
        dto.setAutor(autorDTO);
        List<ArquivoDTO> arquivos = arquivoService.buscarArquivosPorEntidade(
                Arquivo.TipoEntidadeRelacionada.POST,
                post.getId()
        );
        dto.setImagemPrincipal(arquivos.isEmpty() ? null : arquivos.get(0));
        // Estatísticas
        dto.setNumeroCurtidas(curtidaRepository.countByPostId(post.getId()));
        dto.setNumeroComentarios(comentarioRepository.countByPostId(post.getId()));

        return dto;
    }
}