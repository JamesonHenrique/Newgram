package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.services.PushNotificationService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/push")
@RequiredArgsConstructor
@Tag(name = "Push", description = "API para Web Push (VAPID)")
public class PushResource {

    private final PushNotificationService pushService;

    @GetMapping("/vapid-public-key")
    @Operation(summary = "Chave pública VAPID", description = "Para assinar no navegador")
    public ResponseEntity<Map<String, String>> chavePublica() {

        return ResponseEntity.ok(Map.of("publicKey", pushService.getVapidPublicKey()));
    }

    @PostMapping("/subscriptions")
    @Operation(summary = "Inscrever dispositivo", description = "Idempotente por endpoint")
    public ResponseEntity<Void> inscrever(
            @RequestBody @Valid InscricaoPushDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        pushService.inscrever(usuario.getId(), dto.getEndpoint(), dto.getP256dh(), dto.getAuth());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/subscriptions")
    @Operation(summary = "Desinscrever dispositivo", description = "Remove pelo endpoint")
    public ResponseEntity<Void> desinscrever(
            @RequestParam String endpoint,
            @AuthenticationPrincipal Usuario usuario) {

        pushService.desinscrever(usuario.getId(), endpoint);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class InscricaoPushDTO {
        @NotBlank
        private String endpoint;
        @NotBlank
        private String p256dh;
        @NotBlank
        private String auth;
    }
}
