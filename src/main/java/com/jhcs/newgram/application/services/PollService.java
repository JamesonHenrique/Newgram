package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.enquete.PollCreateDTO;
import com.jhcs.newgram.application.dtos.enquete.PollOpcaoResponseDTO;
import com.jhcs.newgram.application.dtos.enquete.PollResponseDTO;
import com.jhcs.newgram.application.dtos.enquete.PollVotoDTO;
import com.jhcs.newgram.core.domain.entities.Poll;
import com.jhcs.newgram.core.domain.entities.PollOpcao;
import com.jhcs.newgram.core.domain.entities.PollVoto;
import com.jhcs.newgram.core.domain.entities.Post;
import com.jhcs.newgram.core.domain.entities.Storie;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.PollOpcaoRepository;
import com.jhcs.newgram.core.domain.repositories.PollRepository;
import com.jhcs.newgram.core.domain.repositories.PollVotoRepository;
import com.jhcs.newgram.core.domain.repositories.PostRepository;
import com.jhcs.newgram.core.domain.repositories.StorieRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enquetes de escolha única em posts ou stories, com apuração percentual.
 */
@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollOpcaoRepository opcaoRepository;
    private final PollVotoRepository votoRepository;
    private final PostRepository postRepository;
    private final StorieRepository storieRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PollResponseDTO criar(PollCreateDTO dto, Long autorId) {
        if ((dto.getPostId() == null) == (dto.getStorieId() == null)) {
            throw new BusinessException("Informe postId ou storieId (apenas um)");
        }
        if (dto.getOpcoes() == null || dto.getOpcoes().size() < 2 || dto.getOpcoes().size() > 4) {
            throw new BusinessException("Enquete precisa de 2 a 4 opções");
        }
        if (dto.getEncerraEm() != null && !dto.getEncerraEm().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Encerramento precisa ser no futuro");
        }

        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Poll enquete = new Poll();
        enquete.setPergunta(dto.getPergunta().trim());
        enquete.setAutor(autor);
        enquete.setEncerraEm(dto.getEncerraEm());

        if (dto.getPostId() != null) {
            Post post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado"));
            Support.requireOwner(post.getAutor().getId(), autorId,
                    "Só o autor do post cria enquete nele");
            if (pollRepository.findByPostId(post.getId()).isPresent()) {
                throw new BusinessException("Post já tem enquete");
            }
            enquete.setPost(post);
        } else {
            Storie storie = storieRepository.findById(dto.getStorieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Storie não encontrado"));
            Support.requireOwner(storie.getAutor().getId(), autorId,
                    "Só o autor do storie cria enquete nele");
            if (pollRepository.findByStorieId(storie.getId()).isPresent()) {
                throw new BusinessException("Storie já tem enquete");
            }
            enquete.setStorie(storie);
        }

        enquete = pollRepository.save(enquete);
        List<PollOpcao> opcoes = new ArrayList<>();
        int ordem = 0;
        for (String texto : dto.getOpcoes()) {
            PollOpcao opcao = new PollOpcao();
            opcao.setEnquete(enquete);
            opcao.setTexto(texto.trim());
            opcao.setOrdem(ordem++);
            opcoes.add(opcaoRepository.save(opcao));
        }
        enquete.setOpcoes(opcoes);
        return converter(enquete, autorId);
    }

    @Transactional(readOnly = true)
    public PollResponseDTO buscarPorId(Long id, Long viewerId) {
        Poll enquete = pollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquete não encontrada"));
        return converter(enquete, viewerId);
    }

    @Transactional
    public PollResponseDTO votar(Long enqueteId, PollVotoDTO dto, Long usuarioId) {
        Poll enquete = pollRepository.findById(enqueteId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquete não encontrada"));
        if (enquete.encerrada(LocalDateTime.now())) {
            throw new BusinessException("Enquete encerrada");
        }
        PollOpcao opcao = opcaoRepository.findById(dto.getOpcaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Opção não encontrada"));
        if (!opcao.getEnquete().getId().equals(enqueteId)) {
            throw new BusinessException("Opção não pertence a esta enquete");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        PollVoto voto = new PollVoto();
        voto.setEnquete(enquete);
        voto.setOpcao(opcao);
        voto.setUsuario(usuario);
        voto.setDataCriacao(LocalDateTime.now());
        try {
            votoRepository.saveAndFlush(voto);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Você já votou nesta enquete", e);
        }
        return converter(enquete, usuarioId);
    }

    @Transactional
    public void excluir(Long enqueteId, Long usuarioId) {
        Poll enquete = pollRepository.findById(enqueteId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquete não encontrada"));
        Support.requireOwner(enquete.getAutor().getId(), usuarioId,
                "Só o autor exclui a enquete");
        pollRepository.delete(enquete);
    }

    /** Apuração com percentual; null quando ninguém votou ainda. */
    public PollResponseDTO converter(Poll enquete, Long viewerId) {
        PollResponseDTO dto = new PollResponseDTO();
        dto.setId(enquete.getId());
        dto.setPergunta(enquete.getPergunta());
        dto.setEncerraEm(enquete.getEncerraEm());
        dto.setEncerrada(enquete.encerrada(LocalDateTime.now()));

        List<PollOpcao> opcoes = opcaoRepository.findByEnqueteIdOrderByOrdemAsc(enquete.getId());
        long total = votoRepository.countByEnqueteId(enquete.getId());
        dto.setTotalVotos(total);

        List<PollOpcaoResponseDTO> opcoesDto = new ArrayList<>();
        for (PollOpcao opcao : opcoes) {
            long votos = votoRepository.countByOpcaoId(opcao.getId());
            PollOpcaoResponseDTO opcaoDto = new PollOpcaoResponseDTO();
            opcaoDto.setId(opcao.getId());
            opcaoDto.setTexto(opcao.getTexto());
            opcaoDto.setVotos(votos);
            opcaoDto.setPercentual(total == 0 ? 0 : (votos * 100.0 / total));
            opcoesDto.add(opcaoDto);
        }
        dto.setOpcoes(opcoesDto);

        if (viewerId != null) {
            Optional<PollVoto> meuVoto = votoRepository.findByEnqueteIdAndUsuarioId(enquete.getId(), viewerId);
            dto.setMinhaOpcaoId(meuVoto.map(voto -> voto.getOpcao().getId()).orElse(null));
        }
        return dto;
    }
}
