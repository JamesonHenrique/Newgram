package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.seguidor.SeguidorResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.Arquivo;
import com.jhcs.newgram.core.domain.entities.Seguidor;
import com.jhcs.newgram.core.domain.enums.TipoNotificacao;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeguidorService {

    @Autowired
    private SeguidorRepository seguidorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacaoService notificacaoService;
    @Autowired
    private ArquivoService arquivoService;

    @Transactional(readOnly = true)
    public List<SeguidorResponseDTO> listarSeguidores(Long usuarioId, Pageable pageable) {
        List<Usuario> seguidores = seguidorRepository.findSeguidoresByUsuarioId(usuarioId, pageable);
        return seguidores.stream()
                .map(seguidor -> {
                    Optional<Seguidor> relacao = seguidorRepository.findBySeguidorIdAndSeguidoId(seguidor.getId(), usuarioId);
                    SeguidorResponseDTO dto = new SeguidorResponseDTO();
                    if (relacao.isPresent()) {
                        dto.setId(relacao.get().getId());
                        dto.setDataCriacao(relacao.get().getDataCriacao());
                        dto.setNotificacoesAtivadas(relacao.get().isNotificacoesAtivadas());
                    }
                    dto.setSeguidorId(seguidor.getId());
                    dto.setSeguidorUsername(seguidor.getUsername());
                    dto.setSeguidorNome(seguidor.getNome());

                    dto.setSeguidoId(usuarioId);
                    return dto;
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SeguidorResponseDTO> listarSeguidos(Long usuarioId, Pageable pageable) {
        List<Usuario> seguidos = seguidorRepository.findSeguidosByUsuarioId(usuarioId, pageable);
        return seguidos.stream()
                .map(seguido -> {
                    Optional<Seguidor> relacao = seguidorRepository.findBySeguidorIdAndSeguidoId(usuarioId, seguido.getId());
                    SeguidorResponseDTO dto = new SeguidorResponseDTO();
                    if (relacao.isPresent()) {
                        dto.setId(relacao.get().getId());
                        dto.setDataCriacao(relacao.get().getDataCriacao());
                        dto.setNotificacoesAtivadas(relacao.get().isNotificacoesAtivadas());
                    }
                    dto.setSeguidoId(seguido.getId());
                    dto.setSeguidoUsername(seguido.getUsername());
                    dto.setSeguidoNome(seguido.getNome());

                    dto.setSeguidorId(usuarioId);
                    return dto;
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long contarSeguidores(Long usuarioId) {
        return seguidorRepository.countSeguidoresByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Long contarSeguidos(Long usuarioId) {
        return seguidorRepository.countSeguidosByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public boolean verificarSeguimento(Long seguidorId, Long seguidoId) {
        return seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
    }

    @Transactional
    public SeguidorResponseDTO seguir(Long seguidorId, Long seguidoId) {
        if (seguidorId.equals(seguidoId)) {
            throw new RuntimeException("Não é possível seguir a si mesmo");
        }

        if (seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId)) {
            throw new RuntimeException("Já está seguindo este usuário");
        }

        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new RuntimeException("Seguidor não encontrado"));

        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new RuntimeException("Seguido não encontrado"));

        Seguidor relacao = new Seguidor();
        relacao.setSeguidor(seguidor);
        relacao.setSeguido(seguido);
        relacao.setDataCriacao(new Date());
        relacao.setNotificacoesAtivadas(true);

        relacao = seguidorRepository.save(relacao);

        // Enviar notificação para o seguido
        notificacaoService.criarNotificacao(
                seguidoId,
                seguidorId,
                TipoNotificacao.NOVO_SEGUIDOR,
                seguidor.getUsername() + " começou a seguir você"
        );

        return converterParaDTO(relacao);
    }

    @Transactional
    public void deixarDeSeguir(Long seguidorId, Long seguidoId) {
        if (!seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId)) {
            throw new RuntimeException("Não está seguindo este usuário");
        }

        seguidorRepository.deleteBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
    }

    @Transactional
    public void alterarNotificacoes(Long seguidorId, Long seguidoId, boolean ativar) {
        Optional<Seguidor> relacaoOpt = seguidorRepository.findBySeguidorIdAndSeguidoId(seguidorId, seguidoId);

        if (relacaoOpt.isEmpty()) {
            throw new RuntimeException("Não está seguindo este usuário");
        }

        Seguidor relacao = relacaoOpt.get();
        relacao.setNotificacoesAtivadas(ativar);
        seguidorRepository.save(relacao);
    }
    @Transactional(readOnly = true)
    public List<UsuarioSummaryDTO> buscarSeguidosAleatorios(Long usuarioId, int limite) {
        List<Usuario> seguidosAleatorios = seguidorRepository.findRandomSeguidosByUsuarioId(usuarioId, limite);

        return seguidosAleatorios.stream()
                .map(seguido -> {
                    UsuarioSummaryDTO dto = new UsuarioSummaryDTO();
                    dto.setId(seguido.getId());
                    dto.setNome(seguido.getNome());
                    dto.setUsername(seguido.getUsername());


                    // Buscar foto de perfil
                    List<ArquivoDTO> arquivos = arquivoService.buscarArquivosPorEntidade(
                            Arquivo.TipoEntidadeRelacionada.PERFIL,
                            seguido.getId()
                    );
                    if (!arquivos.isEmpty()) {
                        dto.setFotoPerfil(arquivos.get(0));
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<Usuario> sugerirUsuariosParaSeguir(Long usuarioId, int limite) {
        // Obter IDs de usuários já seguidos
        List<Long> idsJaSeguidos = seguidorRepository.findBySeguidorId(usuarioId)
                .stream()
                .map(s -> s.getSeguido().getId())
                .collect(Collectors.toList());

        // Adicionar ID do próprio usuário à lista de exclusão
        idsJaSeguidos.add(usuarioId);

        return usuarioRepository.findSugestoesUsuarios(usuarioId, idsJaSeguidos, limite);
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