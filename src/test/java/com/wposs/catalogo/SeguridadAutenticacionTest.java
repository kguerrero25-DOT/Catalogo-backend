package com.wposs.catalogo;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wposs.catalogo.modelo.Categoria;
import com.wposs.catalogo.modelo.Producto;
import com.wposs.catalogo.modelo.Rol;
import com.wposs.catalogo.modelo.Usuario;
import com.wposs.catalogo.repositorio.UsuarioRepositorio;
import com.wposs.catalogo.repositorio.CategoriaRepositorio;
import com.wposs.catalogo.repositorio.ProductoRepositorio;
import com.wposs.catalogo.servicio.ServicioJwt;
import com.wposs.catalogo.servicio.UsuarioDetallesServicio;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadAutenticacionTest extends BasePruebasIntegracion {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepositorio usuarios;
    @Autowired private CategoriaRepositorio categorias;
    @Autowired private ProductoRepositorio productos;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ServicioJwt servicioJwt;
    @Autowired private UsuarioDetallesServicio usuarioDetalles;

    @Test
    void registroValidoDevuelve201TokenYContrasenaBcrypt() throws Exception {
        String nombre = nombreUnico();
        MvcResult resultado = registrar(nombre, "Password-segura-1", "user@example.test", "USER")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario").value(nombre))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andReturn();
        Usuario guardado = usuarios.findByUsuario(nombre).orElseThrow();
        assertTrue(guardado.getContrasena().startsWith("$2a$")
                || guardado.getContrasena().startsWith("$2b$"));
        assertEquals(Rol.USER, guardado.getRol());
        validarClaims(tokenDe(resultado));
    }

    @Test
    void usuarioRepetidoDevuelve409() throws Exception {
        String nombre = nombreUnico();
        registrar(nombre, "Password-segura-1", "dup@example.test", null).andExpect(status().isCreated());
        registrar(nombre, "Password-segura-2", "dup2@example.test", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409));
    }

    @Test
    void nombreDeUsuarioTieneRestriccionUnicaEnBaseDeDatos() {
        String nombre = nombreUnico();
        usuarios.saveAndFlush(new Usuario(nombre, passwordEncoder.encode("Password-segura-1"),
                "unique1@example.test", Rol.USER));
        assertThrows(DataIntegrityViolationException.class,
                () -> usuarios.saveAndFlush(new Usuario(nombre, passwordEncoder.encode("Password-segura-2"),
                        "unique2@example.test", Rol.USER)));
    }

    @Test
    void contrasenaCortaDevuelve400() throws Exception {
        registrar(nombreUnico(), "1234", "short@example.test", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.contrasena").isNotEmpty());
    }

    @Test
    void registroIgnoraRolAdminEnviadoPorCliente() throws Exception {
        String nombre = nombreUnico();
        registrar(nombre, "Password-segura-1", "role@example.test", "ADMIN")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USER"));
        assertEquals(Rol.USER, usuarios.findByUsuario(nombre).orElseThrow().getRol());
    }

    @Test
    void loginCorrectoDevuelveTokenJwtValidoDeUnaHora() throws Exception {
        String nombre = nombreUnico();
        registrar(nombre, "Password-segura-1", "login@example.test", null).andExpect(status().isCreated());
        MvcResult resultado = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(nombre, "Password-segura-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        String token = tokenDe(resultado);
        assertTrue(servicioJwt.esValido(token, usuarioDetalles.loadUserByUsername(nombre)));
        validarClaims(token);
    }

    @Test
    void loginIncorrectoDevuelve401ConMensajeGenerico() throws Exception {
        String nombre = nombreUnico();
        registrar(nombre, "Password-segura-1", "wrong@example.test", null).andExpect(status().isCreated());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(nombre, "incorrecta")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("usuario o contraseña incorrectos"))
                .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    void listarProductosEsPublico() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isOk());
    }

    @Test
    void tokenInvalidoNoInterrumpeRutaPublica() throws Exception {
        mvc.perform(get("/api/productos").header("Authorization", "Bearer invalido"))
                .andExpect(status().isOk());
    }

    @Test
    void corsPermitePreflightDelOrigenConfigurado() throws Exception {
        mvc.perform(options("/api/productos")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void eliminarSinTokenDevuelve401Json() throws Exception {
        mvc.perform(delete("/api/productos/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.momento").isNotEmpty())
                .andExpect(jsonPath("$.estado").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje").value("usuario o contraseña incorrectos"))
                .andExpect(jsonPath("$.ruta").value("/api/productos/1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void usuarioNoPuedeEliminarProductos() throws Exception {
        mvc.perform(delete("/api/productos/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estado").value(403))
                .andExpect(jsonPath("$.mensaje", containsString("permisos")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeEliminarProducto() throws Exception {
        Categoria categoria = categorias.findByNombreIgnoreCase("libros").orElseThrow();
        Producto producto = productos.saveAndFlush(new Producto("Producto a eliminar " + UUID.randomUUID(),
                java.math.BigDecimal.ONE, categoria, 1));
        mvc.perform(delete("/api/productos/{id}", producto.getId())).andExpect(status().isNoContent());
    }

    @Test
    void tokenManipuladoDevuelve401YNo500() throws Exception {
        String nombre = guardarUsuarioActivo();
        String token = servicioJwt.generar(usuarioDetalles.loadUserByUsername(nombre));
        String[] partes = token.split("\\.");
        partes[1] = (partes[1].charAt(0) == 'e' ? "f" : "e") + partes[1].substring(1);
        mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer " + String.join(".", partes)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void tokenVencidoDevuelve401YNo500() throws Exception {
        String token = crearToken("usuario-vencido", Date.from(Instant.now().minusSeconds(7200)),
                Date.from(Instant.now().minusSeconds(3600)));
        mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void authorizationSinBearerDevuelve401YNo500() throws Exception {
        mvc.perform(get("/api/auth/yo").header("Authorization", "token-sin-prefijo"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void usuarioDesactivadoNoPuedeAutenticarseNiUsarJwt() throws Exception {
        String nombre = nombreUnico();
        Usuario usuario = usuarios.saveAndFlush(new Usuario(nombre,
                passwordEncoder.encode("Password-segura-1"), "disabled@example.test", Rol.USER));
        usuario.setActivo(false);
        usuarios.saveAndFlush(usuario);
        String token = servicioJwt.generar(usuarioDetalles.loadUserByUsername(nombre));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(nombre, "Password-segura-1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("usuario o contraseña incorrectos"));
        mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioAutenticadoPuedeConsultarSuPerfil() throws Exception {
        String nombre = nombreUnico();
        MvcResult registro = registrar(nombre, "Password-segura-1", "profile@example.test", null)
                .andExpect(status().isCreated()).andReturn();
        mvc.perform(get("/api/auth/yo").header("Authorization", "Bearer " + tokenDe(registro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value(nombre))
                .andExpect(jsonPath("$.correo").value("profile@example.test"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorPuedeConsultarPerfilDeOtroUsuario() throws Exception {
        String nombre = nombreUnico();
        usuarios.saveAndFlush(new Usuario(nombre, passwordEncoder.encode("Password-segura-1"),
                "admin-profile@example.test", Rol.USER));
        mvc.perform(get("/api/usuarios/{usuario}", nombre))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value(nombre));
    }

    private org.springframework.test.web.servlet.ResultActions registrar(
            String nombre, String contrasena, String correo, String rol) throws Exception {
        String cuerpo = "{\"usuario\":\"%s\",\"contrasena\":\"%s\",\"correo\":\"%s\"%s}"
                .formatted(nombre, contrasena, correo, rol == null ? "" : ",\"rol\":\"" + rol + "\"");
        return mvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private String loginJson(String nombre, String contrasena) {
        return "{\"usuario\":\"%s\",\"contrasena\":\"%s\"}".formatted(nombre, contrasena);
    }

    private String tokenDe(MvcResult resultado) throws Exception {
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }

    private String guardarUsuarioActivo() {
        String nombre = nombreUnico();
        usuarios.saveAndFlush(new Usuario(nombre, passwordEncoder.encode("Password-segura-1"),
                "active@example.test", Rol.USER));
        return nombre;
    }

    private String crearToken(String usuario, Date emitido, Date vencimiento) {
        return Jwts.builder().subject(usuario).claim("roles", Set.of("ROLE_USER"))
                .issuedAt(emitido).expiration(vencimiento)
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8))).compact();
    }

    private void validarClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();
        assertEquals(Set.of("sub", "roles", "iat", "exp"), claims.keySet());
        assertFalse(claims.containsKey("correo"));
        assertEquals(Duration.ofHours(1).toMillis(),
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    private String nombreUnico() {
        return "usuario-" + UUID.randomUUID();
    }
}
