package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioCreateDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioUpdateDTO;
import com.jhcs.newgram.core.domain.entities.Arquivo;
import com.jhcs.newgram.core.domain.entities.Seguidor;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SeguidorRepository seguidorRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ArquivoService arquivoService;

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> buscarUsuarios(String termo, Pageable pageable, Long usuarioLogadoId) {
        Page<Usuario> usuarios = usuarioRepository.buscarUsuarios(termo, pageable);
        return usuarios.map(usuario -> converterParaSummaryDTO(usuario, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id, Long usuarioLogadoId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return converterParaResponseDTO(usuario, usuarioLogadoId);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorUsername(String username, Long usuarioLogadoId) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return converterParaResponseDTO(usuario, usuarioLogadoId);
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioCreateDTO dto) {
        // Validações
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }

        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("Nome de usuário já em uso");
        }

        if (!dto.getSenha().equals(dto.getConfirmacaoSenha())) {
            throw new BusinessException("Senhas não conferem");
        }

        // Criar usuário
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setDataCriacao(new Date());
        usuario.setVerificado(false);

        usuario = usuarioRepository.save(usuario);

        return converterParaResponseDTO(usuario, usuario.getId());
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateDTO dto, Long usuarioLogadoId) {
        if (!id.equals(usuarioLogadoId)) {
            throw new UnauthorizedException("Você não tem permissão para atualizar este usuário");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        usuario.setNome(dto.getNome());
        usuario.setBio(dto.getBio());
        usuario.setWebsite(dto.getWebsite());
        usuario.setTelefone(dto.getTelefone());

        usuario = usuarioRepository.save(usuario);

        return converterParaResponseDTO(usuario, usuarioLogadoId);
    }

    @Transactional
    public UsuarioResponseDTO atualizarFotoPerfil(Long id, String fotoUrl, Long usuarioLogadoId) {
        if (!id.equals(usuarioLogadoId)) {
            throw new UnauthorizedException("Você não tem permissão para atualizar este usuário");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        List<ArquivoDTO> arquivos = arquivoService.buscarArquivosPorEntidade(
                Arquivo.TipoEntidadeRelacionada.PERFIL,
                usuario.getId()
        );
        usuario = usuarioRepository.save(usuario);

        return converterParaResponseDTO(usuario, usuarioLogadoId);
    }

    @Transactional
    public void alterarSenha(Long id, String senhaAtual, String novaSenha, String confirmacaoSenha, Long usuarioLogadoId) {
        if (!id.equals(usuarioLogadoId)) {
            throw new UnauthorizedException("Você não tem permissão para alterar a senha deste usuário");
        }

        if (!novaSenha.equals(confirmacaoSenha)) {
            throw new BusinessException("Nova senha e confirmação não conferem");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new BusinessException("Senha atual incorreta");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void seguirUsuario(Long seguidorId, Long seguidoId) {
        if (seguidorId.equals(seguidoId)) {
            throw new BusinessException("Você não pode seguir a si mesmo");
        }

        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário seguidor não encontrado"));

        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário a ser seguido não encontrado"));

        if (seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId)) {
            throw new BusinessException("Você já segue este usuário");
        }

        Seguidor relacao = new Seguidor();
        relacao.setSeguidor(seguidor);
        relacao.setSeguido(seguido);
        relacao.setDataCriacao(new Date());
        relacao.setNotificacoesAtivadas(true);

        seguidorRepository.save(relacao);

        // Criar notificação para o usuário seguido
        // notificacaoService.criarNotificacaoSeguidor(seguidor, seguido);
    }

    @Transactional
    public void deixarDeSeguirUsuario(Long seguidorId, Long seguidoId) {
        if (!seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId)) {
            throw new BusinessException("Você não segue este usuário");
        }

        seguidorRepository.deleteBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> listarSeguidores(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        Page<Usuario> seguidores = (Page<Usuario>) usuarioRepository.findSeguidoresByUsuarioId(usuarioId, pageable);
        return seguidores.map(seguidor -> converterParaSummaryDTO(seguidor, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioSummaryDTO> listarSeguindo(Long usuarioId, Pageable pageable, Long usuarioLogadoId) {
        Page<Usuario> seguindo = (Page<Usuario>) usuarioRepository.findSeguidosByUsuarioId(usuarioId, pageable);
        return seguindo.map(seguido -> converterParaSummaryDTO(seguido, usuarioLogadoId));
    }

    @Transactional(readOnly = true)
    public List<UsuarioSummaryDTO> sugerirUsuarios(Long usuarioId, int limite) {
        // Obter IDs de usuários que o usuário já segue
        List<Long> seguindoIds = seguidorRepository.findBySeguidorId(usuarioId)
                .stream()
                .map(seguidor -> seguidor.getSeguido().getId())
                .collect(Collectors.toList());

        // Adicionar o próprio ID do usuário para excluí-lo das sugestões
        seguindoIds.add(usuarioId);

        List<Usuario> sugestoes = usuarioRepository.findSugestoesUsuarios(usuarioId, seguindoIds, limite);

        return sugestoes.stream()
                .map(usuario -> converterParaSummaryDTO(usuario, usuarioId))
                .collect(Collectors.toList());
    }

    // Métodos auxiliares para converter entidades em DTOs

    private UsuarioResponseDTO converterParaResponseDTO(Usuario usuario, Long usuarioLogadoId) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setBio(usuario.getBio());
        dto.setDataCadastro(usuario.getDataCriacao());
        dto.setVerificado(usuario.isVerificado());
        dto.setWebsite(usuario.getWebsite());
        List<ArquivoDTO> arquivos = arquivoService.buscarArquivosPorEntidade(
                Arquivo.TipoEntidadeRelacionada.PERFIL,
                usuario.getId()
        );
        if (!arquivos.isEmpty()) {
            dto.setFotoPerfil(arquivos.get(0));
        }
        // Estatísticas
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId()));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId()));
        dto.setNumeroPosts(postRepository.countPostsByUsuarioId(usuario.getId()));

        // Verificar se o usuário logado está seguindo este usuário
        if (usuarioLogadoId != null && !usuarioLogadoId.equals(usuario.getId())) {
            dto.setSeguindoUsuario(seguidorRepository.existsBySeguidorIdAndSeguidoId(usuarioLogadoId, usuario.getId()));
        }

        return dto;
    }

    private UsuarioSummaryDTO converterParaSummaryDTO(Usuario usuario, Long usuarioLogadoId) {
        UsuarioSummaryDTO dto = new UsuarioSummaryDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsername());
        List<ArquivoDTO> arquivos = arquivoService.buscarArquivosPorEntidade(
                Arquivo.TipoEntidadeRelacionada.PERFIL,
                usuario.getId()
        );
        if (!arquivos.isEmpty()) {
            dto.setFotoPerfil(arquivos.get(0));
        }
        dto.setVerificado(usuario.isVerificado());

        // Verificar se o usuário logado está seguindo este usuário
        if (usuarioLogadoId != null && !usuarioLogadoId.equals(usuario.getId())) {
            dto.setSeguindoUsuario(seguidorRepository.existsBySeguidorIdAndSeguidoId(usuarioLogadoId, usuario.getId()));
        }

        return dto;
    }
}