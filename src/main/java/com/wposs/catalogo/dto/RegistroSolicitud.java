package com.wposs.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroSolicitud(
        @Schema(description = "Nombre de usuario nuevo", example = "usuario-demo")
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 80, message = "El usuario no puede superar 80 caracteres")
        String usuario,
        @Schema(description = "Contrasena de al menos ocho caracteres; ejemplo ficticio", example = "Ejemplo-no-usar-123")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String contrasena,
        @Schema(description = "Correo electronico de la cuenta", example = "usuario@example.test")
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe ser válido")
        @Size(max = 254, message = "El correo no puede superar 254 caracteres")
        String correo) {
}
