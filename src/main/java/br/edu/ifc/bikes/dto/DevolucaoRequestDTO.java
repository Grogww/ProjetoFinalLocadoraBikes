package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotNull;

public record DevolucaoRequestDTO(
        @NotNull(message = "Informe se a bicicleta possui problema")
        Boolean possuiProblema
) { }
