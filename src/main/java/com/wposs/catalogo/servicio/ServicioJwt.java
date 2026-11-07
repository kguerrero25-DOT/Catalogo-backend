package com.wposs.catalogo.servicio;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class ServicioJwt {
    private final SecretKey clave;
    private final long duracionMs;

    public ServicioJwt(@Value("${app.jwt.secreto}") String secreto,
                       @Value("${app.jwt.duracion-ms}") long duracionMs) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRETO debe contener al menos 32 bytes");
        }
        if (duracionMs != 3_600_000L) {
            throw new IllegalStateException("La duración de los JWT debe ser exactamente una hora");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.duracionMs = duracionMs;
    }

    public String generar(UserDetails usuario) {
        Date ahora = new Date();
        List<String> roles = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("roles", roles)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave)
                .compact();
    }

    public String extraerUsuario(String token) {
        return analizar(token).getSubject();
    }

    public boolean esValido(String token, UserDetails usuario) {
        try {
            Claims claims = analizar(token);
            return claims.getSubject().equals(usuario.getUsername()) && usuario.isEnabled();
        } catch (JwtException | IllegalArgumentException excepcion) {
            return false;
        }
    }

    private Claims analizar(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
