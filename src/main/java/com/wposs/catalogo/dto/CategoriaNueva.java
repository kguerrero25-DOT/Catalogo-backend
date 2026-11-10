package com.wposs.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaNueva(
        @Schema(description = "Nombre unico de la categoria", example = "tecnologia")
        @NotBlank(message = "El nombre de la categoría es obligatorio y no puede estar en blanco")
        @Size(min = 3, max = 60, message = "El nombre de la categoría debe tener entre 3 y 60 caracteres")
        String nombre,
        @Schema(description = "Descripcion opcional de la categoria", example = "Dispositivos y accesorios")
        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion) {
}
