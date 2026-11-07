package com.wposs.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaNueva(
        @NotBlank(message = "El nombre de la categoría es obligatorio y no puede estar en blanco")
        @Size(min = 3, max = 60, message = "El nombre de la categoría debe tener entre 3 y 60 caracteres")
        String nombre,
        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion) { }
