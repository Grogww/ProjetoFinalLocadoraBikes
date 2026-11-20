package br.edu.ifc.bikes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LocacaoResponseDTO(
        Long id,
        ClienteResponseDTO cliente,
        BicicletaResponseDTO bicicleta,
        LocalDateTime dataRetirada,
        LocalDateTime dataPrevistaDevolucao,
        LocalDateTime dataDevolucao,
        BigDecimal valor,
        String finalCartao,
        String status
) {}
