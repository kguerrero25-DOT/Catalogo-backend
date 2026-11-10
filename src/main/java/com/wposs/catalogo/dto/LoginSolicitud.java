package com.wposs.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginSolicitud(
        @Schema(description = "Nombre de usuario registrado", example = "usuario-demo")
        @NotBlank(message = "El usuario es obligatorio") String usuario,
        @Schema(description = "Contrasena de la cuenta; ejemplo ficticio", example = "Ejemplo-no-usar-123")
        @NotBlank(message = "La contraseña es obligatoria") String contrasena) {
}
