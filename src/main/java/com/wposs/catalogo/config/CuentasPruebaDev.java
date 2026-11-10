package com.wposs.catalogo.config;

import com.wposs.catalogo.modelo.Rol;
import com.wposs.catalogo.modelo.Usuario;
import com.wposs.catalogo.repositorio.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class CuentasPruebaDev implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CuentasPruebaDev.class);

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsuario;
    private final String adminContrasena;
    private final String editorUsuario;
    private final String editorContrasena;

    public CuentasPruebaDev(
            UsuarioRepositorio usuarios,
            PasswordEncoder passwordEncoder,
            @Value("${TEST_ADMIN_USERNAME}") String adminUsuario,
            @Value("${TEST_ADMIN_PASSWORD}") String adminContrasena,
            @Value("${TEST_EDITOR_USERNAME}") String editorUsuario,
            @Value("${TEST_EDITOR_PASSWORD}") String editorContrasena) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.adminUsuario = adminUsuario;
        this.adminContrasena = adminContrasena;
        this.editorUsuario = editorUsuario;
        this.editorContrasena = editorContrasena;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validarCredenciales(adminUsuario, adminContrasena);
        validarCredenciales(editorUsuario, editorContrasena);
        if (adminUsuario.equals(editorUsuario)) {
            throw new IllegalStateException("Las cuentas ADMIN y EDITOR deben tener usuarios distintos");
        }

        crearSiNoExiste(adminUsuario, adminContrasena, Rol.ADMIN);
        crearSiNoExiste(editorUsuario, editorContrasena, Rol.EDITOR);
    }

    private void validarCredenciales(String usuario, String contrasena) {
        if (usuario == null || usuario.isBlank() || usuario.length() > 80) {
            throw new IllegalStateException("Los usuarios de prueba deben tener entre 1 y 80 caracteres");
        }
        if (contrasena == null || contrasena.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 16
                || contrasena.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Las contrasenas de prueba deben tener entre 16 y 72 bytes");
        }
        if (usuario.equals(contrasena)) {
            throw new IllegalStateException("La contrasena de prueba no puede ser igual al usuario");
        }
    }

    private void crearSiNoExiste(String nombre, String contrasena, Rol rol) {
        usuarios.findByUsuario(nombre).ifPresentOrElse(
                existente -> {
                    if (existente.getRol() != rol || !existente.isActivo()) {
                        throw new IllegalStateException(
                                "El usuario de prueba existente no tiene el rol o estado esperado: " + nombre);
                    }
                    log.info("Cuenta de prueba existente verificada para rol {}", rol);
                },
                () -> {
                    usuarios.save(new Usuario(nombre, passwordEncoder.encode(contrasena),
                            nombre + "@example.test", rol));
                    log.info("Cuenta de prueba creada para rol {}", rol);
                });
    }
}
