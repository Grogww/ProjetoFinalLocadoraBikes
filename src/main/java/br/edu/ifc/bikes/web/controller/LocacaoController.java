package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.LocacaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoResponseDTO;
import br.edu.ifc.bikes.jwt.JwtUserDetails;
import br.edu.ifc.bikes.service.LocacaoService;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Locações", description = "Contém todas as operações relativas aos recursos para locação de uma bicicleta.")
@RestController
@RequestMapping("api/v1/locacoes")
@RequiredArgsConstructor
public class LocacaoController {

    private final LocacaoService locacaoService;

    @Operation(summary = "Alugar uma bicicleta", description = "Recurso exclusivo do perfil CLIENTE. Aluga a primeira bicicleta disponível",
        responses = {
                @ApiResponse(responseCode = "201", description = "Locação criada com sucesso",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocacaoResponseDTO.class))),
                @ApiResponse(responseCode = "403", description = "Recurso não permitido ao perfil ADMIN",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "404", description = "Usuário autenticado sem cadastro de cliente",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "409", description = "Não há bicicleta disponível no momento",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada inválidos ou cartão vencido",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    //a locação é feita para o cliente do usuário autenticado pelo token
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<LocacaoResponseDTO> alugar(@Valid @RequestBody LocacaoRequestDTO locacao,
                                                     @AuthenticationPrincipal JwtUserDetails userDetails) {
        LocacaoResponseDTO locacaoCriada = locacaoService.alugar(locacao, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(locacaoCriada);
    }
}
