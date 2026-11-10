package com.wposs.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoActualizar(
        @Schema(description = "Nombre actualizado del producto", example = "Teclado mecanico")
        @NotBlank(message = "El título es obligatorio y no puede estar en blanco")
        @Size(max = 120, message = "El título no puede superar los 120 caracteres")
        String titulo,
        @Schema(description = "Precio unitario actualizado", example = "175000.00")
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser como mínimo 0.01")
        @Digits(integer = 10, fraction = 2, message = "El precio debe tener hasta 10 dígitos enteros y 2 decimales")
        BigDecimal precio,
        @Schema(description = "Unidades disponibles actualizadas", example = "12")
        @NotNull(message = "Las existencias son obligatorias")
        @PositiveOrZero(message = "Las existencias no pueden ser negativas")
        Integer existencias,
        @Schema(description = "Identificador de la categoria existente", example = "2")
        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId) {
}
