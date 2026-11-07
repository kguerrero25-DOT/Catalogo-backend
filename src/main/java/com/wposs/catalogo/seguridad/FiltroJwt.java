package com.wposs.catalogo.seguridad;

import com.wposs.catalogo.servicio.ServicioJwt;
import com.wposs.catalogo.servicio.UsuarioDetallesServicio;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

public class FiltroJwt extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(FiltroJwt.class);

    private final ServicioJwt servicioJwt;
    private final UsuarioDetallesServicio usuarios;

    public FiltroJwt(ServicioJwt servicioJwt, UsuarioDetallesServicio usuarios) {
        this.servicioJwt = servicioJwt;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        String encabezado = solicitud.getHeader("Authorization");
        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            cadena.doFilter(solicitud, respuesta);
            return;
        }

        String token = encabezado.substring(7).trim();
        if (token.isEmpty()) {
            log.debug("Se recibió un encabezado Bearer sin token");
            cadena.doFilter(solicitud, respuesta);
            return;
        }

        try {
            String nombreUsuario = servicioJwt.extraerUsuario(token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails usuario = usuarios.loadUserByUsername(nombreUsuario);
                if (servicioJwt.esValido(token, usuario)) {
                    UsernamePasswordAuthenticationToken autenticacion =
                            UsernamePasswordAuthenticationToken.authenticated(
                                    usuario, null, usuario.getAuthorities());
                    autenticacion.setDetails(solicitud.getRemoteAddr());
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException excepcion) {
            log.debug("Token JWT rechazado: {}", excepcion.getClass().getSimpleName());
        }
        cadena.doFilter(solicitud, respuesta);
    }
}
