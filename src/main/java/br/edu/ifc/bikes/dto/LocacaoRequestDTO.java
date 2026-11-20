package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.CreditCardNumber;

//@CreditCardNumber confere o dígito verificador do cartão (algoritmo de Luhn)
public record LocacaoRequestDTO(
        @NotBlank(message = "O número do cartão é obrigatório")
        @Pattern(regexp = "\\d{16}", message = "O número do cartão deve ter 16 dígitos")
        @CreditCardNumber(message = "O número do cartão deve ser válido")
        String numeroCartao,
        @NotBlank(message = "A validade do cartão é obrigatória")
        @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "A validade do cartão deve estar no formato MM/AA")
        String validadeCartao,
        @NotBlank(message = "O cvv é obrigatório")
        @Pattern(regexp = "\\d{3}", message = "O cvv deve ter 3 dígitos")
        String cvv,
        @NotNull(message = "A quantidade de horas é obrigatória")
        @Min(value = 1, message = "A locação deve ser de no mínimo 1 hora")
        @Max(value = 24, message = "A locação deve ser de no máximo 24 horas")
        Integer horas
) { }
