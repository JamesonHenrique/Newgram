package com.jhcs.newgram.presentation.resources;

import com.jhcs.newgram.application.dtos.enquete.PollCreateDTO;
import com.jhcs.newgram.application.dtos.enquete.PollResponseDTO;
import com.jhcs.newgram.application.dtos.enquete.PollVotoDTO;
import com.jhcs.newgram.application.services.PollService;
import com.jhcs.newgram.core.domain.entities.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enquetes")
@RequiredArgsConstructor
@Tag(name = "Enquetes", description = "API para enquetes em posts e stories")
public class PollResource {

    private final PollService pollService;

    @PostMapping
    @Operation(summary = "Criar enquete", description = "Anexa enquete de 2-4 opções a um post ou storie do autor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Enquete criada",
                    content = @Content(schema = @Schema(implementation = PollResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou alvo com enquete",
                    content = @Content)
    })
    public ResponseEntity<PollResponseDTO> criar(
            @RequestBody @Valid PollCreateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pollService.criar(dto, usuario.getId()));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Apuração da enquete", description = "Pergunta, opções e percentuais")
    public ResponseEntity<PollResponseDTO> buscar(
            @Parameter(description = "ID da enquete", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.ok(pollService.buscarPorId(id, usuario.getId()));
    }

    @PostMapping("/{id:\\d+}/votar")
    @Operation(summary = "Votar", description = "Um voto por usuário (idempotente via 409)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Voto registrado",
                    content = @Content(schema = @Schema(implementation = PollResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Enquete encerrada ou voto duplicado",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Enquete ou opção não encontrada",
                    content = @Content)
    })
    public ResponseEntity<PollResponseDTO> votar(
            @Parameter(description = "ID da enquete", required = true)
            @PathVariable Long id,
            @RequestBody @Valid PollVotoDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pollService.votar(id, dto, usuario.getId()));
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Excluir enquete", description = "Só o autor exclui")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Enquete não encontrada",
                    content = @Content)
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da enquete", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        pollService.excluir(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
