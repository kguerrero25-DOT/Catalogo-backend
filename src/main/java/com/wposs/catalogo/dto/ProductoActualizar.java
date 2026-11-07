package com.wposs.catalogo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoActualizar(
        @NotBlank(message = "El título es obligatorio y no puede estar en blanco")
        @Size(max = 120, message = "El título no puede superar los 120 caracteres")
        String titulo,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser como mínimo 0.01")
        @Digits(integer = 10, fraction = 2, message = "El precio debe tener hasta 10 dígitos enteros y 2 decimales")
        BigDecimal precio,
        @NotNull(message = "Las existencias son obligatorias")
        @PositiveOrZero(message = "Las existencias no pueden ser negativas")
        Integer existencias,
        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId) { }
