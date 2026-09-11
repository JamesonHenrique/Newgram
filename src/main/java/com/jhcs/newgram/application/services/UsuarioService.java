package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.usuario.UsuarioComumDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioUpdateDTO;
import com.jhcs.newgram.core.domain.entities.Denuncia;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.AlvoDenuncia;
import com.jhcs.newgram.core.domain.enums.StatusDenuncia;
import com.jhcs.newgram.core.domain.enums.StatusSeguimento;
import com.jhcs.newgram.core.domain.enums.TipoArquivo;
import com.jhcs.newgram.core.domain.enums.TipoConta;
import com.jhcs.newgram.core.domain.repositories.DenunciaRepository;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.core.domain.repositories.VisualizacaoPostRepository;
import com.jhcs.newgram.infrastructure.aws.S3StorageService;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SeguidorRepository seguidorRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private S3StorageService s3StorageService;

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Autowired
    private VisualizacaoPostRepository visualizacaoPostRepository;


    @Transactional(readOnly = true)
    public UsuarioSummaryDTO buscarUsuarioPorId(Long id, Long usuarioLogadoId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        UsuarioSummaryDTO dto = converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId);
        dto.setSeguindoUsuario(segue(usuarioLogadoId, id));
        return dto;
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorUsername(String username, Long usuarioLogadoId) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        UsuarioResponseDTO dto = converterParaUsuarioResponseDTO(usuario, usuarioLogadoId);
        dto.setSeguindoUsuario(segue(usuarioLogadoId, usuario.getId()));
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarUsuarios(String termo, Pageable pageable, Long usuarioLogadoId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        return usuarioRepository.buscarUsuarios(termo, safePageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }

        if (dto.getBio() != null) {
            usuario.setBio(dto.getBio());
        }
        if (dto.getFotoPerfil() != null && !dto.getFotoPerfil().isEmpty()) {
            salvarFotoDePerfil(usuario.getId(), dto.getFotoPerfil());
        }

        usuario = usuarioRepository.save(usuario);
        return converterParaUsuarioResponseDTO(usuario, usuario.getId());
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSeguidores(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        return usuarioRepository.findSeguidoresByUsuarioId(usuarioId, StatusSeguimento.ACEITO, safePageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSeguidos(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        return usuarioRepository.findSeguidosByUsuarioId(usuarioId, StatusSeguimento.ACEITO, safePageable)
                .map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarSugestoesUsuarios(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Usuario> sugestoes =
                usuarioRepository.findSugestoesUsuarios(usuarioId, StatusSeguimento.ACEITO, safePageable);
        return sugestoes.map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarUsuariosMaisFamosos(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Usuario> famosos =
                usuarioRepository.findUsuariosMaisFamosos(safePageable, usuarioId, StatusSeguimento.ACEITO);
        return famosos.map(usuario -> converterParaUsuarioSummaryDTO(usuario, usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioComumDTO> buscarUsuariosPorAmigosEmComum(Long usuarioId, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataCriacao");
        Pageable safePageable = Support.safePage(pageable, sort);
        Page<Object[]> results = usuarioRepository.findUsuariosPorAmigosEmComum(
                usuarioId, StatusSeguimento.ACEITO, safePageable);
        return results.map(result -> {
            Usuario usuario = (Usuario) result[0];
            Long commonFollowers = (Long) result[1];
            return converterParaUsuarioComumDTO(usuario, commonFollowers.intValue(), usuarioId);
        });
    }

    public void salvarFotoDePerfil(Long id, MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum usuario encontrado com o ID: " + id));

        String fotoAntiga = usuario.getFotoPerfil();
        var fotoPerfil = arquivoService.saveFile(file, usuario.getUsuarioName(), TipoArquivo.FOTO_PERFIL);
        usuario.setFotoPerfil(fotoPerfil);
        usuarioRepository.save(usuario);
        // Foto antiga vira órfã só após a nova persistir.
        arquivoService.deleteFile(fotoAntiga);
    }

    @Transactional
    public UsuarioResponseDTO atualizarPrivado(Long id, boolean privado, Long usuarioLogadoId) {
        Support.requireOwner(id, usuarioLogadoId, "Você só pode alterar a própria conta");
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        usuario.setPrivado(privado);
        return converterParaUsuarioResponseDTO(usuarioRepository.save(usuario), id);
    }

    /** Solicita o selo de verificação (entra na fila de moderação). */
    @Transactional
    public void solicitarVerificacao(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (usuario.isVerificado()) {
            throw new BusinessException("Conta já verificada");
        }
        if (denunciaRepository
                .findByDenuncianteIdAndTipoAlvoAndAlvoId(usuarioId, AlvoDenuncia.USUARIO, usuarioId)
                .isPresent()) {
            throw new BusinessException("Solicitação já enviada");
        }
        Denuncia denuncia = new Denuncia();
        denuncia.setDenunciante(usuario);
        denuncia.setTipoAlvo(AlvoDenuncia.USUARIO);
        denuncia.setAlvoId(usuarioId);
        denuncia.setMotivo("VERIFICACAO");
        denuncia.setDescricao("Solicitação de selo de verificação");
        denuncia.setStatus(StatusDenuncia.ABERTA);
        denuncia.setDataCriacao(java.time.LocalDateTime.now());
        denunciaRepository.save(denuncia);
    }

    /** LGPD: exporta os dados da conta em estrutura serializável. */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> exportarDados(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        java.util.Map<String, Object> dados = new java.util.LinkedHashMap<>();
        dados.put("id", usuario.getId());
        dados.put("nome", usuario.getNome());
        dados.put("username", usuario.getUsername());
        dados.put("email", usuario.getEmail());
        dados.put("bio", usuario.getBio());
        dados.put("privado", usuario.isPrivado());
        dados.put("verificado", usuario.isVerificado());
        dados.put("emailVerificado", usuario.isEmailVerificado());
        dados.put("tipoConta", usuario.getTipoConta());
        dados.put("dataCriacao", usuario.getDataCriacao());
        dados.put("numeroSeguidores",
                seguidorRepository.countSeguidoresByUsuarioId(usuarioId, StatusSeguimento.ACEITO));
        dados.put("numeroSeguindo",
                seguidorRepository.countSeguidosByUsuarioId(usuarioId, StatusSeguimento.ACEITO));
        dados.put("posts", postRepository.findByAutorId(usuarioId, org.springframework.data.domain.Pageable.unpaged())
                .map(post -> java.util.Map.of(
                        "id", post.getId(),
                        "legenda", String.valueOf(post.getLegenda()),
                        "dataCriacao", String.valueOf(post.getDataCriacao())))
                .getContent());
        return dados;
    }

    /** LGPD: anonimiza a conta (conteúdo coletivo preservado sem PII). */
    @Transactional
    public void excluirConta(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        usuario.setNome("Usuário removido");
        usuario.setUsername("removido_" + usuarioId);
        usuario.setEmail("removido_" + usuarioId + "@deleted.local");
        usuario.setSenha("REMOVED_" + java.util.UUID.randomUUID());
        usuario.setBio(null);
        usuario.setFotoPerfil(null);
        usuario.setPrivado(true);
        usuario.setVerificado(false);
        usuario.setTwoFactorEnabled(false);
        usuario.setTotpSecret(null);
        usuario.setChavePix(null);
        arquivoService.deleteFile(usuario.getFotoPerfil());
        usuario.setFotoPerfil(null);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizarChavePix(Long usuarioId, String chavePix) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        String chave = chavePix == null ? null : chavePix.trim();
        if (chave != null && (chave.isEmpty() || chave.length() > 100)) {
            chave = chave.isEmpty() ? null : chave;
            if (chave != null && chave.length() > 100) {
                throw new BusinessException("Chave Pix deve ter no máximo 100 caracteres");
            }
        }
        usuario.setChavePix(chave);
        return converterParaUsuarioResponseDTO(usuarioRepository.save(usuario), usuarioId);
    }

    @Transactional
    public UsuarioResponseDTO atualizarTipoConta(Long usuarioId, TipoConta tipoConta) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (tipoConta == null) {
            throw new BusinessException("Tipo de conta é obrigatório");
        }
        usuario.setTipoConta(tipoConta);
        return converterParaUsuarioResponseDTO(usuarioRepository.save(usuario), usuarioId);
    }

    @Transactional(readOnly = true)
    public com.jhcs.newgram.application.dtos.usuario.AnalyticsDTO analytics(Long usuarioId) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        java.time.LocalDate ha7 = java.time.LocalDate.now().minusDays(7);
        java.time.LocalDate ha30 = java.time.LocalDate.now().minusDays(30);
        com.jhcs.newgram.application.dtos.usuario.AnalyticsDTO dto =
                new com.jhcs.newgram.application.dtos.usuario.AnalyticsDTO();
        dto.setViews7d(visualizacaoPostRepository.countViewsDoAutorDesde(usuarioId, ha7));
        dto.setViews30d(visualizacaoPostRepository.countViewsDoAutorDesde(usuarioId, ha30));
        dto.setAlcance7d(visualizacaoPostRepository.countAlcanceDoAutorDesde(usuarioId, ha7));
        dto.setSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuarioId, StatusSeguimento.ACEITO));
        dto.setPosts(postRepository.countPostsByUsuarioId(usuarioId));
        return dto;
    }

    private boolean segue(Long usuarioLogadoId, Long alvoId) {
        return usuarioLogadoId != null && seguidorRepository.existsBySeguidorIdAndSeguidoIdAndStatus(
                usuarioLogadoId, alvoId, StatusSeguimento.ACEITO);
    }

    private UsuarioComumDTO converterParaUsuarioComumDTO(Usuario usuario, int commonFollowers, Long usuarioLogadoId) {
        UsuarioComumDTO dto = new UsuarioComumDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsuarioName());
        dto.setCommonFollowers(commonFollowers);
        dto.setFotoPerfil(s3StorageService.getFileUrl(usuario.getFotoPerfil()));
        dto.setSeguindoUsuario(segue(usuarioLogadoId, usuario.getId()));
        return dto;
    }

    private UsuarioResponseDTO converterParaUsuarioResponseDTO(Usuario usuario, Long usuarioLogadoId) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsuarioName());
        dto.setEmail(usuario.getEmail());
        dto.setBio(usuario.getBio());

        dto.setDataCadastro(usuario.getDataCriacao());

        dto.setFotoPerfil(s3StorageService.getFileUrl(usuario.getFotoPerfil()));
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId(), StatusSeguimento.ACEITO));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId(), StatusSeguimento.ACEITO));
        dto.setNumeroPosts(postRepository.countPostsByUsuarioId(usuario.getId()));
        dto.setSeguindoUsuario(segue(usuarioLogadoId, usuario.getId()));
        dto.setPrivado(usuario.isPrivado());
        dto.setVerificado(usuario.isVerificado());
        dto.setEmailVerificado(usuario.isEmailVerificado());
        dto.setChavePix(usuario.getChavePix());
        dto.setTipoConta(usuario.getTipoConta());
        return dto;
    }

    private UsuarioSummaryDTO converterParaUsuarioSummaryDTO(Usuario usuario, Long usuarioLogadoId) {
        UsuarioSummaryDTO dto = new UsuarioSummaryDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsuarioName());
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId(), StatusSeguimento.ACEITO));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId(), StatusSeguimento.ACEITO));
        dto.setNumeroPosts(postRepository.countPostsByUsuarioId(usuario.getId()));

        dto.setFotoPerfil(s3StorageService.getFileUrl(usuario.getFotoPerfil()));
        dto.setSeguindoUsuario(segue(usuarioLogadoId, usuario.getId()));
        dto.setPrivado(usuario.isPrivado());
        dto.setVerificado(usuario.isVerificado());
        dto.setPapel(usuario.getPapel());

        return dto;
    }
}
