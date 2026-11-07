package com.wposs.catalogo.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginSolicitud(
        @NotBlank(message = "El usuario es obligatorio") String usuario,
        @NotBlank(message = "La contraseña es obligatoria") String contrasena) { }
