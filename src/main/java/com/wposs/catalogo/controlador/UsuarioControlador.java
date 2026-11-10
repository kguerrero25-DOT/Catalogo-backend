package com.wposs.catalogo.controlador;

import com.wposs.catalogo.dto.PerfilRespuesta;
import com.wposs.catalogo.servicio.UsuarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Consulta administrativa de perfiles de usuario")
public class UsuarioControlador {
    private final UsuarioServicio usuarios;

    public UsuarioControlador(UsuarioServicio usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/{usuario}")
    @Operation(summary = "Consultar perfil de usuario", description = "Devuelve un perfil publico. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticacion"),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public PerfilRespuesta perfil(@PathVariable String usuario) {
        return usuarios.buscarPerfil(usuario);
    }
}
