package com.wposs.catalogo.servicio;

import com.wposs.catalogo.dto.PerfilRespuesta;
import com.wposs.catalogo.dto.RegistroSolicitud;
import com.wposs.catalogo.excepcion.RecursoDuplicadoException;
import com.wposs.catalogo.modelo.Rol;
import com.wposs.catalogo.modelo.Usuario;
import com.wposs.catalogo.repositorio.UsuarioRepositorio;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServicio {
    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicio(UsuarioRepositorio usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(RegistroSolicitud solicitud) {
        if (usuarios.existsByUsuario(solicitud.usuario())) {
            throw new RecursoDuplicadoException("El nombre de usuario ya está registrado");
        }
        Usuario usuario = new Usuario(solicitud.usuario(),
                passwordEncoder.encode(solicitud.contrasena()), solicitud.correo(), Rol.USER);
        try {
            return usuarios.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException excepcion) {
            throw new RecursoDuplicadoException("El nombre de usuario ya está registrado");
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("#usuarioSolicitado == authentication.name or hasRole('ADMIN')")
    public PerfilRespuesta buscarPerfil(String usuarioSolicitado) {
        Usuario usuario = usuarios.findByUsuario(usuarioSolicitado)
                .orElseThrow(() -> new com.wposs.catalogo.excepcion.RecursoNoEncontradoException(
                        "No se encontró el usuario solicitado"));
        return new PerfilRespuesta(usuario.getId(), usuario.getUsuario(), usuario.getCorreo(),
                usuario.getRol().name(), usuario.isActivo());
    }
}
