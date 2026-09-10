package com.jhcs.newgram.application.services;

import com.jhcs.newgram.core.domain.entities.PushSubscription;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.repositories.PushSubscriptionRepository;
import com.jhcs.newgram.core.domain.repositories.UsuarioRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import com.jhcs.newgram.infrastructure.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Web Push (VAPID). Chaves via env; sem elas, par efêmero só para
 * desenvolvimento (inscrições quebram no restart — nunca em prod).
 * Envio assíncrono; inscrição morta (404/410) é podada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushSubscriptionRepository subscriptionRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${push.vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${push.vapid.private-key:}")
    private String vapidPrivateKey;

    @Value("${push.vapid.subject:mailto:contato@newgram.example.com}")
    private String vapidSubject;

    private PushService pushService;
    private String publicKey;

    @PostConstruct
    public void init() throws Exception {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        if (vapidPublicKey == null || vapidPublicKey.isBlank()
                || vapidPrivateKey == null || vapidPrivateKey.isBlank()) {
            KeyPair par = gerarParEc();
            this.publicKey = base64Url(chavePublicaSemCompressao(par));
            vapidPrivateKey = base64Url(privada32Bytes(par));
            log.warn("PUSH_VAPID_* nao configurado: usando par efemero (apenas desenvolvimento)");
        } else {
            this.publicKey = vapidPublicKey.trim();
            vapidPrivateKey = vapidPrivateKey.trim();
        }
        this.pushService =
                new PushService(this.publicKey, vapidPrivateKey, vapidSubject);
    }

    public String getVapidPublicKey() {
        return publicKey;
    }

    @Transactional
    public void inscrever(Long usuarioId, String endpoint, String p256dh, String auth) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (endpoint == null || endpoint.isBlank() || p256dh == null || auth == null) {
            throw new BusinessException("Inscrição push inválida");
        }
        var existente = subscriptionRepository.findByEndpoint(endpoint.trim());
        if (existente.isPresent()) {
            return;
        }
        PushSubscription inscricao = new PushSubscription();
        inscricao.setUsuario(usuario);
        inscricao.setEndpoint(endpoint.trim());
        inscricao.setP256dh(p256dh);
        inscricao.setAuth(auth);
        subscriptionRepository.save(inscricao);
    }

    @Transactional
    public void desinscrever(Long usuarioId, String endpoint) {
        subscriptionRepository.deleteByEndpointAndUsuario(endpoint, usuarioId);
    }

    /** Dispara para todas as inscrições do usuário, sem bloquear o caller. */
    @Async
    public void enviarParaUsuario(Long usuarioId, String titulo, String corpo) {
        List<PushSubscription> inscricoes = subscriptionRepository.findByUsuarioId(usuarioId);
        String payload = "{\"title\":" + json(titulo) + ",\"body\":" + json(corpo) + "}";
        for (PushSubscription inscricao : inscricoes) {
            try {
                Notification notificacao = new Notification(
                        inscricao.getEndpoint(), inscricao.getP256dh(), inscricao.getAuth(), payload);
                int status = pushService.send(notificacao).getStatusLine().getStatusCode();
                if (status == 404 || status == 410) {
                    subscriptionRepository.delete(inscricao);
                } else if (status >= 400) {
                    log.warn("Push para {} devolveu {}", usuarioId, status);
                }
            } catch (Exception e) {
                log.warn("Falha ao enviar push para {}", usuarioId, e);
            }
        }
    }

    private static KeyPair gerarParEc() throws Exception {
        KeyPairGenerator gerador = KeyPairGenerator.getInstance("EC");
        gerador.initialize(new ECGenParameterSpec("secp256r1"), new java.security.SecureRandom());
        return gerador.generateKeyPair();
    }

    private static byte[] chavePublicaSemCompressao(KeyPair par) {
        ECPublicKey publica = (ECPublicKey) par.getPublic();
        byte[] x = ajusta32(publica.getW().getAffineX().toByteArray());
        byte[] y = ajusta32(publica.getW().getAffineY().toByteArray());
        byte[] saida = new byte[65];
        saida[0] = 0x04;
        System.arraycopy(x, 0, saida, 1, 32);
        System.arraycopy(y, 0, saida, 33, 32);
        return saida;
    }

    private static byte[] privada32Bytes(KeyPair par) {
        return ajusta32(((ECPrivateKey) par.getPrivate()).getS().toByteArray());
    }

    private static byte[] ajusta32(byte[] valor) {
        if (valor.length == 32) {
            return valor;
        }
        byte[] saida = new byte[32];
        if (valor.length > 32) {
            System.arraycopy(valor, valor.length - 32, saida, 0, 32);
        } else {
            System.arraycopy(valor, 0, saida, 32 - valor.length, valor.length);
        }
        return saida;
    }

    private static String base64Url(byte[] valor) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(valor);
    }

    private static String json(String valor) {
        return "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
