package br.edu.ifc.bikes.dto;

public record BicicletaResponseDTO(
        Long id,
        String modelo,
        String codigo,
        String status
) {}
