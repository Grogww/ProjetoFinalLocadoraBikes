package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.DevolucaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoResponseDTO;
import br.edu.ifc.bikes.jwt.JwtUserDetails;
import br.edu.ifc.bikes.service.JasperService;
import br.edu.ifc.bikes.service.LocacaoService;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Locações", description = "Contém todas as operações relativas aos recursos para locação de uma bicicleta.")
@RestController
@RequestMapping("api/v1/locacoes")
@RequiredArgsConstructor
public class LocacaoController {

    private final LocacaoService locacaoService;
    private final JasperService jasperService;

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

    @Operation(summary = "Devolver uma bicicleta", description = "Recurso exclusivo do perfil CLIENTE. Finaliza a locação e calcula o valor a debitar no cartão",
        responses = {
                @ApiResponse(responseCode = "200", description = "Devolução registrada com sucesso",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocacaoResponseDTO.class))),
                @ApiResponse(responseCode = "403", description = "Recurso não permitido ao perfil ADMIN",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "404", description = "Locação não encontrada para o cliente autenticado",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "409", description = "Locação já finalizada",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada inválidos",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    //o cliente só devolve as próprias locações, identificadas pelo usuário do token
    @PatchMapping("/{id}/devolucao")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<LocacaoResponseDTO> devolver(@PathVariable Long id,
                                                       @Valid @RequestBody DevolucaoRequestDTO devolucao,
                                                       @AuthenticationPrincipal JwtUserDetails userDetails) {
        LocacaoResponseDTO locacaoFinalizada = locacaoService.devolver(id, devolucao, userDetails.getId());
        return ResponseEntity.ok(locacaoFinalizada);
    }

    @Operation(summary = "Gerar o relatório de locações e devoluções", description = "Recurso exclusivo do perfil ADMIN. Com apenasAtivas=true lista só as locações em andamento",
        responses = {
                @ApiResponse(responseCode = "200", description = "Relatório gerado em PDF",
                        content = @Content(mediaType = "application/pdf")),
                @ApiResponse(responseCode = "403", description = "Recurso não permitido ao perfil CLIENTE",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
        })
    //o filtro vai como parâmetro para a consulta do relatório; sem ele, lista todas as locações
    @GetMapping("/relatorio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> gerarRelatorio(@RequestParam(defaultValue = "false") Boolean apenasAtivas,
                                               HttpServletResponse response) {
        jasperService.addParameter("APENAS_ATIVAS", apenasAtivas);
        byte[] relatorioBytes = jasperService.generatePdf();
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition", "inline; filename="+System.currentTimeMillis()+".pdf");
        try {
            response.getOutputStream().write(relatorioBytes);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar relatório", e);
        }
        return ResponseEntity.ok().build();
    }
}
