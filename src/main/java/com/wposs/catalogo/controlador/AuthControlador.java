package com.wposs.catalogo.controlador;

import com.wposs.catalogo.dto.AuthRespuesta;
import com.wposs.catalogo.dto.LoginSolicitud;
import com.wposs.catalogo.dto.PerfilRespuesta;
import com.wposs.catalogo.dto.RegistroSolicitud;
import com.wposs.catalogo.servicio.ServicioJwt;
import com.wposs.catalogo.servicio.UsuarioDetallesServicio;
import com.wposs.catalogo.servicio.UsuarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Registro, inicio de sesion y perfil autenticado")
public class AuthControlador {
    private final AuthenticationManager authenticationManager;
    private final UsuarioServicio usuarios;
    private final UsuarioDetallesServicio usuarioDetalles;
    private final ServicioJwt servicioJwt;

    public AuthControlador(AuthenticationManager authenticationManager, UsuarioServicio usuarios,
                           UsuarioDetallesServicio usuarioDetalles, ServicioJwt servicioJwt) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.usuarioDetalles = usuarioDetalles;
        this.servicioJwt = servicioJwt;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar usuario", description = "Crea una cuenta con rol USER y devuelve un JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe")
    })
    public ResponseEntity<AuthRespuesta> registrar(@Valid @RequestBody RegistroSolicitud solicitud) {
        var usuario = usuarios.registrar(solicitud);
        UserDetails detalles = usuarioDetalles.loadUserByUsername(usuario.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta(detalles));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Valida credenciales y devuelve un JWT Bearer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticacion exitosa"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas")
    })
    public AuthRespuesta iniciarSesion(@Valid @RequestBody LoginSolicitud solicitud) {
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                    solicitud.usuario(), solicitud.contrasena()));
        } catch (AuthenticationException excepcion) {
            throw new BadCredentialsException("usuario o contraseña incorrectos");
        }
        return respuesta(usuarioDetalles.loadUserByUsername(solicitud.usuario()));
    }

    @GetMapping("/yo")
    @Operation(summary = "Consultar perfil propio", description = "Devuelve el perfil asociado al JWT.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil consultado"),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT valido")
    })
    public PerfilRespuesta perfil(org.springframework.security.core.Authentication autenticacion) {
        return usuarios.buscarPerfil(autenticacion.getName());
    }

    private AuthRespuesta respuesta(UserDetails usuario) {
        String rol = usuario.getAuthorities().iterator().next().getAuthority().substring("ROLE_".length());
        return new AuthRespuesta(servicioJwt.generar(usuario), "Bearer", usuario.getUsername(), rol);
    }
}
