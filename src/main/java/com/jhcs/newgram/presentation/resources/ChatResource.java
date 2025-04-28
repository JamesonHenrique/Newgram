package com.jhcs.newgram.presentation.resources;


import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.application.services.ConversaService;
import com.jhcs.newgram.application.services.MensagemService;
import com.jhcs.newgram.application.services.UsuarioService;
import com.jhcs.newgram.core.domain.entities.Conversa;
import com.jhcs.newgram.core.domain.entities.Mensagem;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.core.domain.utils.ArquivoUtils;
import com.jhcs.newgram.infrastructure.aws.S3StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class ChatResource {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MensagemService mensagemService;

    @Autowired
    private ConversaService conversaService;

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private SeguidorRepository seguidorRepository;
    @Autowired
    private S3StorageService s3StorageService;

    @MessageMapping("/chat.enviar-mensagem")
    public void enviarMensagem(@Payload MensagemDTO mensagemDTO, Principal principal) {
        UsuarioResponseDTO remetenteDTO = usuarioService.buscarUsuarioPorUsername(principal.getName(), null);
        UsuarioSummaryDTO destinatarioDTO = usuarioService.buscarUsuarioPorId(mensagemDTO.getDestinatarioId(), null);

        Usuario remetente = converterParaUsuario(remetenteDTO);
        Usuario destinatario = converterParaUsuario(destinatarioDTO);
        Conversa conversa = conversaService.buscarConversaEntreUsuarios(remetente.getId(), destinatario.getId())
                .orElseGet(() -> conversaService.criarConversa(remetente, destinatario));

        Mensagem mensagem = mensagemService.enviarMensagem(
                conversa,
                remetente,
                destinatario,
                mensagemDTO.getConteudo(),
                mensagemDTO.getTipo()
        );

        MensagemDTO mensagemSalva = mensagemService.convertToDTO(mensagem);

        messagingTemplate.convertAndSendToUser(
                destinatario.getId().toString(),
                "/queue/mensagens",
                mensagemSalva
        );

        messagingTemplate.convertAndSendToUser(
                remetente.getId().toString(),
                "/queue/confirmacoes",
                mensagemSalva
        );
    }

    @MessageMapping("/chat.mensagem-entregue")
    @SendToUser("/queue/confirmacoes")
    public void confirmarEntrega(@Payload Long mensagemId) {
        mensagemService.marcarComoEntregue(mensagemId);
    }

    @MessageMapping("/chat.mensagem-visualizada")
    @SendToUser("/queue/confirmacoes")
    public void confirmarVisualizacao(@Payload Long mensagemId) {
        mensagemService.marcarComoVisualizada(mensagemId);
    }

    private UsuarioResponseDTO converterParaUsuarioResponseDTO(Usuario usuario, ArquivoDTO fotoPerfil) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsuarioName());
        dto.setEmail(usuario.getEmail());
        dto.setBio(usuario.getBio());

        dto.setDataCadastro(usuario.getDataCriacao());

        dto.setFotoPerfil(s3StorageService.getFileUrl(usuario.getFotoPerfil()));
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId()));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId()));
        dto.setNumeroPosts((long) usuario.getPosts().size());

        return dto;
    }

    private UsuarioSummaryDTO converterParaUsuarioSummaryDTO(Usuario usuario, Long usuarioLogadoId) {
        UsuarioSummaryDTO dto = new UsuarioSummaryDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsuarioName());
        dto.setNumeroSeguidores(seguidorRepository.countSeguidoresByUsuarioId(usuario.getId()));
        dto.setNumeroSeguindo(seguidorRepository.countSeguidosByUsuarioId(usuario.getId()));
        dto.setNumeroPosts((long) usuario.getPosts().size());

        dto.setFotoPerfil(s3StorageService.getFileUrl(usuario.getFotoPerfil()));
        boolean seguindoUsuario = false;
        if (usuarioLogadoId != null) {
            seguindoUsuario = seguidorRepository.existsBySeguidorIdAndSeguidoId(usuarioLogadoId, usuario.getId());
        }
        dto.setSeguindoUsuario(seguindoUsuario);



        return dto;
    }
    private Usuario converterParaUsuario(UsuarioResponseDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNome(dto.getNome());
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setBio(dto.getBio());
        usuario.setDataCriacao(dto.getDataCadastro());
        return usuario;
    }

    private Usuario converterParaUsuario(UsuarioSummaryDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setNome(dto.getNome());
        usuario.setUsername(dto.getUsername());

        return usuario;
    }
}