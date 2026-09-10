package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.statususuario.StatusNotaDTO;
import com.jhcs.newgram.application.services.StatusUsuarioService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/status")
@RequiredArgsConstructor
@Tag(name = "Status", description = "API para notas e presença")
public class StatusResource {

    private final StatusUsuarioService statusUsuarioService;

    @GetMapping("/notas")
    @Operation(summary = "Notas de seguidos", description = "Notas (24h) de seguidos aceitos + própria")
    public ResponseEntity<List<StatusNotaDTO>> listarNotas(@AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(statusUsuarioService.listarNotas(usuario.getId()));
    }

    @PutMapping("/nota")
    @Operation(summary = "Definir nota", description = "Nota curta (máx. 60 caracteres); vazia apaga")
    public ResponseEntity<StatusNotaDTO> definirNota(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(statusUsuarioService.definirNota(usuario.getId(), body.get("nota")));
    }
}
