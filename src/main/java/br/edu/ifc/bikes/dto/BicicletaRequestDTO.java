package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BicicletaRequestDTO(
        @NotBlank(message = "O modelo é obrigatório")
        @Size(min=2, max=100)
        String modelo,
        @NotBlank(message = "O código é obrigatório")
        @Size(min=3, max=20)
        String codigo
) { }
