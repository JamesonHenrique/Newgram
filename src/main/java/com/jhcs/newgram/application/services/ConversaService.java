package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.conversa.ConversaCreateDTO;
import com.jhcs.newgram.application.dtos.conversa.ConversaResponseDTO;
import com.jhcs.newgram.application.dtos.conversa.ConversaSummaryDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemCreateDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.entities.Arquivo;
import com.jhcs.newgram.core.domain.entities.Conversa;
import com.jhcs.newgram.core.domain.entities.Mensagem;
import com.jhcs.newgram.core.domain.enums.TipoMensagem;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.ConversaRepository;
import com.jhcs.newgram.core.domain.repositories.MensagemRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversaService {

    @Autowired
    private ConversaRepository conversaRepository;

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Transactional
    public ConversaResponseDTO criarConversa(ConversaCreateDTO dto, Long usuarioId) {
        Usuario criador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        List<Usuario> participantes = new ArrayList<>();
        participantes.add(criador);

        // Adicionar outros participantes
        if (dto.getParticipantesIds() != null && !dto.getParticipantesIds().isEmpty()) {
            for (Long participanteId : dto.getParticipantesIds()) {
                if (!participanteId.equals(usuarioId)) {
                    Usuario participante = usuarioRepository.findById(participanteId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuário participante não encontrado"));
                    participantes.add(participante);
                }
            }
        }

        // Verificar se é um chat privado entre 2 pessoas
        if (!dto.isGrupo() && participantes.size() == 2) {
            Optional<Conversa> conversaExistente = conversaRepository.findConversaPrivada(
                    participantes.get(0), participantes.get(1));

            if (conversaExistente.isPresent()) {
                return converterParaResponseDTO(conversaExistente.get(), usuarioId);
            }
        }

        Conversa conversa = new Conversa();

        conversa.setGrupo(dto.isGrupo());

        conversa.setParticipantes(participantes);
        conversa.setDataCriacao(new Date());
        conversa.setUltimaInteracao(new Date());

        conversa = conversaRepository.save(conversa);

        return converterParaResponseDTO(conversa, usuarioId);
    }

    @Transactional
    public ConversaResponseDTO adicionarParticipante(Long conversaId, Long participanteId, Long usuarioId) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));

        // Verificar se o usuário é participante da conversa
        if (!isParticipante(conversa, usuarioId)) {
            throw new UnauthorizedException("Você não é participante desta conversa");
        }

        // Verificar se é um grupo
        if (!conversa.isGrupo()) {
            throw new BusinessException("Não é possível adicionar participantes a uma conversa privada");
        }

        // Verificar se o participante já existe na conversa
        if (conversa.getParticipantes().stream().anyMatch(p -> p.getId().equals(participanteId))) {
            throw new BusinessException("Usuário já é participante desta conversa");
        }

        Usuario participante = usuarioRepository.findById(participanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário participante não encontrado"));

        conversa.getParticipantes().add(participante);
        conversa = conversaRepository.save(conversa);

        // Adicionar mensagem do sistema sobre o novo participante
        Mensagem mensagemSistema = new Mensagem();
        mensagemSistema.setConteudo(participante.getNome() + " foi adicionado à conversa");
        mensagemSistema.setTipo(TipoMensagem.SISTEMA);
        mensagemSistema.setRemetente(null);
        mensagemSistema.setConversa(conversa);
        mensagemSistema.setDataEnvio(new Date());
        mensagemSistema.setVisualizada(true);
        mensagemRepository.save(mensagemSistema);

        return converterParaResponseDTO(conversa, usuarioId);
    }

    @Transactional
    public ConversaResponseDTO removerParticipante(Long conversaId, Long participanteId, Long usuarioId) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));

        // Verificar se o usuário é participante da conversa
        if (!isParticipante(conversa, usuarioId)) {
            throw new UnauthorizedException("Você não é participante desta conversa");
        }

        // Verificar se é um grupo
        if (!conversa.isGrupo()) {
            throw new BusinessException("Não é possível remover participantes de uma conversa privada");
        }

        // Verificar se o participante existe na conversa
        Usuario participante = conversa.getParticipantes().stream()
                .filter(p -> p.getId().equals(participanteId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Usuário não é participante desta conversa"));

        conversa.getParticipantes().remove(participante);
        conversa = conversaRepository.save(conversa);

        // Adicionar mensagem do sistema sobre a remoção do participante
        Mensagem mensagemSistema = new Mensagem();
        mensagemSistema.setConteudo(participante.getNome() + " foi removido da conversa");
        mensagemSistema.setTipo(TipoMensagem.SISTEMA);
        mensagemSistema.setRemetente(null);
        mensagemSistema.setConversa(conversa);
        mensagemSistema.setDataEnvio(new Date());
        mensagemSistema.setVisualizada(true);
        mensagemRepository.save(mensagemSistema);

        return converterParaResponseDTO(conversa, usuarioId);
    }

    @Transactional
    public void sairDaConversa(Long conversaId, Long usuarioId) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));

        // Verificar se o usuário é participante da conversa
        if (!isParticipante(conversa, usuarioId)) {
            throw new UnauthorizedException("Você não é participante desta conversa");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (conversa.isGrupo()) {
            // Se for grupo, apenas remove o participante
            conversa.getParticipantes().remove(usuario);

            // Se não restarem participantes, excluir a conversa
            if (conversa.getParticipantes().isEmpty()) {
                conversaRepository.delete(conversa);
            } else {
                conversaRepository.save(conversa);

                // Adicionar mensagem do sistema informando que o usuário saiu
                Mensagem mensagemSistema = new Mensagem();
                mensagemSistema.setConteudo(usuario.getNome() + " saiu da conversa");
                mensagemSistema.setTipo(TipoMensagem.SISTEMA);
                mensagemSistema.setRemetente(null);
                mensagemSistema.setConversa(conversa);
                mensagemSistema.setDataEnvio(new Date());
                mensagemSistema.setVisualizada(true);
                mensagemRepository.save(mensagemSistema);
            }
        } else {
            // Se for conversa privada e um dos participantes sair, excluir a conversa
            conversaRepository.delete(conversa);
        }
    }

    @Transactional(readOnly = true)
    public ConversaResponseDTO buscarPorId(Long conversaId, Long usuarioId) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));

        // Verificar se o usuário é participante da conversa
        if (!isParticipante(conversa, usuarioId)) {
            throw new UnauthorizedException("Você não é participante desta conversa");
        }

        return converterParaResponseDTO(conversa, usuarioId);
    }

    @Transactional(readOnly = true)
    public Page<ConversaSummaryDTO> listarConversas(Long usuarioId, Pageable pageable) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Page<Conversa> conversas = conversaRepository.findByParticipante(usuario, pageable);

        return conversas.map(conversa -> converterParaSummaryDTO(conversa, usuarioId));
    }

    @Transactional
    public MensagemResponseDTO enviarMensagem(Long conversaId, MensagemCreateDTO dto, Long usuarioId) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));

        // Verificar se o usuário é participante da conversa
        if (!isParticipante(conversa, usuarioId)) {
            throw new UnauthorizedException("Você não é participante desta conversa");
        }

        Usuario remetente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Mensagem mensagem = new Mensagem();
        mensagem.setConteudo(dto.getConteudo());
        mensagem.setTipo(dto.getTipo());
        mensagem.setRemetente(remetente);
        mensagem.setConversa(conversa);
        mensagem.setDataEnvio(new Date());
        mensagem.setVisualizada(false);
        mensagem.setEntregue(true);

        mensagem = mensagemRepository.save(mensagem);



        // Atualizar última interação da conversa
        conversa.setUltimaInteracao(new Date());
        conversaRepository.save(conversa);

        return converterParaMensagemResponseDTO(mensagem, usuarioId);
    }

    @Transactional(readOnly = true)
    public Page<MensagemResponseDTO> listarMensagens(Long conversaId, Long usuarioId, Pageable pageable) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));

        // Verificar se o usuário é participante da conversa
        if (!isParticipante(conversa, usuarioId)) {
            throw new UnauthorizedException("Você não é participante desta conversa");
        }

        Page<Mensagem> mensagens = mensagemRepository.findByConversaIdOrderByDataEnvioDesc(conversaId, pageable);

        return mensagens.map(mensagem -> converterParaMensagemResponseDTO(mensagem, usuarioId));
    }

    @Transactional
    public void marcarMensagensComoLidas(Long conversaId, Long usuarioId) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));

        // Verificar se o usuário é participante da conversa
        if (!isParticipante(conversa, usuarioId)) {
            throw new UnauthorizedException("Você não é participante desta conversa");
        }

        mensagemRepository.marcarMensagensComoVisualizadas(conversaId, usuarioId);
    }

    @Transactional
    public void excluirMensagem(Long mensagemId, Long usuarioId, boolean paraTodasPessoas) {
        Mensagem mensagem = mensagemRepository.findById(mensagemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensagem não encontrada"));

        // Verificar se o usuário é o remetente da mensagem
        if (mensagem.getRemetente() == null || !mensagem.getRemetente().getId().equals(usuarioId)) {
            throw new UnauthorizedException("Você não pode excluir esta mensagem");
        }

        if (paraTodasPessoas) {
            // Excluir para todos
            mensagemRepository.delete(mensagem);
        } else {
            // Excluir apenas para o remetente
            mensagem.setDeletadaPeloRemetente(true);
            mensagemRepository.save(mensagem);
        }
    }

    // Métodos auxiliares

    private boolean isParticipante(Conversa conversa, Long usuarioId) {
        return conversa.getParticipantes().stream()
                .anyMatch(participante -> participante.getId().equals(usuarioId));
    }

    private ConversaResponseDTO converterParaResponseDTO(Conversa conversa, Long usuarioId) {
        ConversaResponseDTO dto = new ConversaResponseDTO();
        dto.setId(conversa.getId());
        dto.setNome(conversa.getNomeGrupo());
        dto.setGrupo(conversa.isGrupo());
        dto.setDataCriacao(conversa.getDataCriacao());
        dto.setUltimaInteracao(conversa.getUltimaInteracao());

        // Converter participantes
        List<UsuarioSummaryDTO> participantesDTO = conversa.getParticipantes().stream()
                .map(participante -> {
                    UsuarioSummaryDTO usuarioDTO = new UsuarioSummaryDTO();
                    usuarioDTO.setId(participante.getId());
                    usuarioDTO.setNome(participante.getNome());
                    usuarioDTO.setUsername(participante.getUsername());


                    return usuarioDTO;
                })
                .collect(Collectors.toList());

        dto.setParticipantes(participantesDTO);

        // Buscar imagem do grupo
        if (conversa.isGrupo()) {

        }

        // Contagem de mensagens não lidas
        dto.setMensagensNaoLidas(mensagemRepository.countMensagensNaoVisualizadas(conversa.getId(), usuarioId));

        // Verificar se o usuário é participante
        dto.setParticipante(isParticipante(conversa, usuarioId));

        // Buscar última mensagem
        Page<Mensagem> ultimasMensagens = mensagemRepository.findByConversaIdOrderByDataEnvioDesc(
                conversa.getId(), Pageable.ofSize(1));

        if (!ultimasMensagens.isEmpty()) {
            dto.setUltimaMensagem(converterParaMensagemResponseDTO(ultimasMensagens.getContent().get(0), usuarioId));
        }

        return dto;
    }

    private ConversaSummaryDTO converterParaSummaryDTO(Conversa conversa, Long usuarioId) {
        ConversaSummaryDTO dto = new ConversaSummaryDTO();
        dto.setId(conversa.getId());
        dto.setUltimaInteracao(conversa.getUltimaInteracao());
        dto.setGrupo(conversa.isGrupo());

        if (conversa.isGrupo()) {
            dto.setNome(conversa.getNomeGrupo());

        } else {
            // Para conversas individuais, mostrar informações do outro participante
            Usuario outroParticipante = conversa.getParticipantes().stream()
                    .filter(participante -> !participante.getId().equals(usuarioId))
                    .findFirst()
                    .orElse(null);

            if (outroParticipante != null) {
                dto.setNome(outroParticipante.getNome());

                UsuarioSummaryDTO outroParticipanteDTO = new UsuarioSummaryDTO();
                outroParticipanteDTO.setId(outroParticipante.getId());
                outroParticipanteDTO.setNome(outroParticipante.getNome());
                outroParticipanteDTO.setUsername(outroParticipante.getUsername());



                dto.setOutroParticipante(outroParticipanteDTO);
            }
        }

        // Contagem de mensagens não lidas
        dto.setMensagensNaoLidas(mensagemRepository.countMensagensNaoVisualizadas(conversa.getId(), usuarioId));

        // Buscar última mensagem (só o texto para preview)
        Page<Mensagem> ultimasMensagens = mensagemRepository.findByConversaIdOrderByDataEnvioDesc(
                conversa.getId(), Pageable.ofSize(1));

        if (!ultimasMensagens.isEmpty()) {
            Mensagem ultimaMensagem = ultimasMensagens.getContent().get(0);

            if (ultimaMensagem.getTipo() == TipoMensagem.TEXTO) {
                dto.setUltimaMensagemTexto(ultimaMensagem.getConteudo());
            } else if (ultimaMensagem.getTipo() == TipoMensagem.IMAGEM) {
                dto.setUltimaMensagemTexto("📷 Imagem");
            } else if (ultimaMensagem.getTipo() == TipoMensagem.VIDEO) {
                dto.setUltimaMensagemTexto("🎬 Vídeo");
            } else if (ultimaMensagem.getTipo() == TipoMensagem.AUDIO) {
                dto.setUltimaMensagemTexto("🎵 Áudio");
            } else if (ultimaMensagem.getTipo() == TipoMensagem.ARQUIVO) {
                dto.setUltimaMensagemTexto("📎 Arquivo");
            } else if (ultimaMensagem.getTipo() == TipoMensagem.SISTEMA) {
                dto.setUltimaMensagemTexto(ultimaMensagem.getConteudo());
            }
        }

        return dto;
    }

    private MensagemResponseDTO converterParaMensagemResponseDTO(Mensagem mensagem, Long usuarioId) {
        MensagemResponseDTO dto = new MensagemResponseDTO();
        dto.setId(mensagem.getId());
        dto.setConteudo(mensagem.getConteudo());
        dto.setTipo(mensagem.getTipo());
        dto.setDataEnvio(mensagem.getDataEnvio());
        dto.setVisualizada(mensagem.isVisualizada());
        dto.setEntregue(mensagem.isEntregue());

        // Verificar se a mensagem foi deletada pelo remetente
        dto.setDeletada(mensagem.isDeletadaPeloRemetente() &&
                (mensagem.getRemetente() != null && mensagem.getRemetente().getId().equals(usuarioId)));

        // Informações do remetente se não for mensagem do sistema
        if (mensagem.getRemetente() != null) {
            UsuarioSummaryDTO remetenteDTO = new UsuarioSummaryDTO();
            remetenteDTO.setId(mensagem.getRemetente().getId());
            remetenteDTO.setNome(mensagem.getRemetente().getNome());
            remetenteDTO.setUsername(mensagem.getRemetente().getUsername());




            dto.setRemetente(remetenteDTO);
        }


        return dto;
    }
}