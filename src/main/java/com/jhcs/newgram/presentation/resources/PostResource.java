package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.post.PostCreateDTO;
import com.jhcs.newgram.application.dtos.post.PostResponseDTO;
import com.jhcs.newgram.application.dtos.post.PostSummaryDTO;
import com.jhcs.newgram.application.dtos.post.PostUpdateDTO;
import com.jhcs.newgram.application.services.PostService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Operações para gerenciamento de posts")
public class PostResource {

    @Autowired
    private PostService postService;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    @Operation(summary = "Criar novo post", description = "Cria um novo post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<PostResponseDTO> criarPost(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Dados do post a ser criado", required = true)
            @RequestBody @Valid PostCreateDTO postDTO
          ) {

        PostResponseDTO post = postService.criarPost(postDTO, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping(path = "/{postId:\\d+}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Adicionar imagem ao post", description = "Adiciona ou atualiza a imagem de um post existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagem adicionada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> uploadImagemPost(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long postId,
            @Parameter(description = "Arquivo de imagem", required = true)
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        postService.salvarImagemDoPost(postId, file, usuario.getId());

        return ResponseEntity.ok()
                .build();

    }
    @GetMapping("/usuario/{usuarioId:\\d+}")
    @Operation(summary = "Listar posts de um usuário", description = "Retorna os posts publicados por um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> listarPostsDoUsuario(
            @Parameter(description = "ID do usuário", required = true)
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10, sort=dataCriacao,desc)")
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostSummaryDTO> posts = postService.listarPostsDoUsuario(usuarioId, pageable, usuario.getId());
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/feed")
    @Operation(summary = "Listar feed do usuário", description = "Retorna o feed personalizado do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feed listado com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> listarFeed(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10, sort=dataCriacao,desc)")
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<PostSummaryDTO> feed = postService.listarFeedDoUsuario(usuario.getId(), pageable);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/populares")
    @Operation(summary = "Listar posts populares", description = "Retorna os posts mais populares da plataforma")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts populares listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> listarPostsPopulares(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10, sort=dataCriacao,desc)")
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostSummaryDTO> posts = postService.listarPostsPopulares(pageable, usuario.getId());
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/recomendados")
    @Operation(summary = "Listar posts recomendados",
            description = "Retorna posts personalizados baseados nos interesses e interações do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts recomendados listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> listarPostsRecomendados(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10)")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<PostSummaryDTO> posts = postService.listarPostsRecomendados(usuario.getId(), pageable);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/buscar")
    @Operation(summary = "Buscar posts por legenda", description = "Retorna posts que contêm o termo buscado na legenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts encontrados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> buscarPostsPorLegenda(
            @Parameter(description = "Termo a ser buscado na legenda", required = true)
            @RequestParam String termo,
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10, sort=dataCriacao,desc)")
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostSummaryDTO> posts = postService.listarPostsPorLegenda(termo, pageable, usuario.getId());
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/tendencias")
    @Operation(summary = "Listar posts em tendência", description = "Retorna os posts que estão em tendência na plataforma baseado em engajamento recente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts em tendência listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> listarPostsTendencias(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10, sort=dataCriacao,desc)")
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostSummaryDTO> posts = postService.listarPostsTendencias(pageable, usuario.getId());
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/seguidos-populares")
    @Operation(summary = "Listar posts populares de seguidos",
            description = "Retorna os posts mais populares dos usuários que o usuário autenticado segue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts populares dos seguidos listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> listarPostsPopularesSeguidores(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10, sort=dataCriacao,desc)")
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostSummaryDTO> posts = postService.listarPostsPopularesSeguidores(usuario.getId(), pageable);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/hashtag/{nome}")
    @Operation(summary = "Listar posts por hashtag", description = "Retorna posts que contêm uma hashtag específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> listarPostsPorHashtag(
            @Parameter(description = "Nome da hashtag", required = true)
            @PathVariable String nome,
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10, sort=dataCriacao,desc)")
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostSummaryDTO> posts = postService.listarPostsPorHashtag(nome, pageable, usuario.getId());
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/localizacao")
    @Operation(summary = "Listar posts por localização", description = "Retorna posts associados a uma localização específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> listarPostsPorLocalizacao(
            @Parameter(description = "Nome da localização", required = true)
            @RequestParam String local,
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10, sort=dataCriacao,desc)")
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostSummaryDTO> posts = postService.listarPostsPorLocalizacao(local, pageable, usuario.getId());
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/salvos")
    @Operation(summary = "Listar posts salvos", description = "Retorna os posts salvos pelo usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts salvos listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> listarPostsSalvos(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10, sort=dataCriacao,desc)")
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostSummaryDTO> posts = postService.listarPostsSalvos(usuario.getId(), pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/salvos/colecao/{colecao}")
    @Operation(summary = "Listar posts salvos por coleção", description = "Retorna posts salvos pelo usuário em uma coleção específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts da coleção listados com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content)
    })
    public ResponseEntity<Page<PostSummaryDTO>> listarPostsSalvosPorColecao(
            @Parameter(description = "Nome da coleção", required = true)
            @PathVariable String colecao,
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10, sort=dataCriacao,desc)")
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostSummaryDTO> posts = postService.listarPostsSalvosPorColecao(usuario.getId(), colecao, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Buscar post por ID", description = "Retorna os detalhes de um post específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = PostResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<PostResponseDTO> buscarPorId(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        PostResponseDTO post = postService.buscarPorId(id, usuario.getId());
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Atualizar post", description = "Atualiza um post existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = PostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Permissão negada",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<PostResponseDTO> atualizarPost(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long id,
            @Parameter(description = "Dados para atualização do post", required = true)
            @RequestBody @Valid PostUpdateDTO postDTO,
            @AuthenticationPrincipal Usuario usuario) {

        PostResponseDTO post = postService.atualizarPost(id, postDTO, usuario.getId());
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Excluir post", description = "Remove permanentemente um post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Permissão negada",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> excluirPost(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        postService.excluirPost(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id:\\d+}/curtir")
    @Operation(summary = "Curtir post", description = "Adiciona uma curtida ao post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post curtido com sucesso",
                    content = @Content(schema = @Schema(implementation = PostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Post já curtido pelo usuário",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<PostResponseDTO> curtirPost(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        PostResponseDTO post = postService.curtirPost(id, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @DeleteMapping("/{id:\\d+}/descurtir")
    @Operation(summary = "Remover curtida", description = "Remove a curtida do usuário em um post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Curtida removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Post não foi curtido pelo usuário",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> descurtirPost(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        postService.descurtirPost(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id:\\d+}/salvar")
    @Operation(summary = "Salvar post", description = "Salva um post, opcionalmente em uma coleção específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post salvo com sucesso"),
            @ApiResponse(responseCode = "400", description = "Post já salvo pelo usuário",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> salvarPost(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nome da coleção para salvar o post")
            @RequestParam(required = false) String colecao,
            @AuthenticationPrincipal Usuario usuario) {

        postService.salvarPost(id, usuario.getId(), colecao);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id:\\d+}/remover-salvo")
    @Operation(summary = "Remover post salvo", description = "Remove um post da lista de salvos do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post removido dos salvos com sucesso"),
            @ApiResponse(responseCode = "400", description = "Post não está salvo pelo usuário",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> removerPostSalvo(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        postService.removerPostSalvo(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id:\\d+}/arquivar")
    @Operation(summary = "Arquivar post", description = "Arquiva um post sem removê-lo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post arquivado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Permissão negada",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> arquivarPost(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        postService.arquivarPost(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id:\\d+}/desarquivar")
    @Operation(summary = "Desarquivar post", description = "Desarquiva um post anteriormente arquivado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post desarquivado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Permissão negada",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    public ResponseEntity<Void> desarquivarPost(
            @Parameter(description = "ID do post", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        postService.desarquivarPost(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}