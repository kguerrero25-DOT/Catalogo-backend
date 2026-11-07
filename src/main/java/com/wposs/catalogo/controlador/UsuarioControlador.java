package com.wposs.catalogo.controlador;

import com.wposs.catalogo.dto.PerfilRespuesta;
import com.wposs.catalogo.servicio.UsuarioServicio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {
    private final UsuarioServicio usuarios;

    public UsuarioControlador(UsuarioServicio usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/{usuario}")
    public PerfilRespuesta perfil(@PathVariable String usuario) {
        return usuarios.buscarPerfil(usuario);
    }
}
