package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.busca.BuscarResponseDTO;
import com.jhcs.newgram.application.services.BuscarService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/buscar")
@RequiredArgsConstructor
@Tag(name = "Busca", description = "API para busca global")
public class BuscarResource {

    private final BuscarService buscarService;

    @GetMapping
    @Operation(summary = "Busca global", description = "Pessoas + posts (privacidade aplicada) + hashtags")
    public ResponseEntity<BuscarResponseDTO> buscar(
            @Parameter(description = "Termo de busca", required = true)
            @RequestParam @NotBlank String q,
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(description = "Parâmetros de paginação (page=0, size=10)")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        return ResponseEntity.ok(buscarService.buscar(q.trim(), usuario.getId(), pageable));
    }
}
