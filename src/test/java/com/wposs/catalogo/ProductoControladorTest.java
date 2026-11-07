package com.wposs.catalogo;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductoControladorTest extends BasePruebasIntegracion {
    @Autowired
    private MockMvc mvc;

    @Test
    void listarDevuelve200() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(8))));
    }

    @Test
    void buscarInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/productos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").isNotEmpty())
                .andExpect(jsonPath("$.ruta").value("/api/productos/999999"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void crearValidoDevuelve201YLocation() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" 
                            {"titulo":"Nuevo libro","precio":20000,"categoriaId":1,"existencias":3}
                            """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/productos/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.categoria").value("libros"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void crearPrecioNegativoDevuelve400ConErrorDelCampo() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"titulo":"Incorrecto","precio":-5,"categoriaId":1,"existencias":1}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.precio").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void tituloEnBlancoDevuelve400ConErrorDelCampo() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"titulo":" ","precio":10.00,"categoriaId":1,"existencias":1}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void tituloDeDoscientosCaracteresDevuelve400() throws Exception {
        String titulo = "x".repeat(200);
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"titulo":"%s","precio":10.00,"categoriaId":1,"existencias":1}
                            """.formatted(titulo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void categoriaInexistenteDevuelve400() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"titulo":"Categoria ausente","precio":10.00,"categoriaId":999999,"existencias":1}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void tituloDuplicadoDevuelve409() throws Exception {
        String json = """
                {"titulo":"Producto duplicado de prueba","precio":10.00,"categoriaId":1,"existencias":1}
                """;
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void jsonMalformadoDevuelve400ConErrorEstructurado() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{roto"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isNotEmpty())
                .andExpect(jsonPath("$.ruta").value("/api/productos"));
    }

    @Test
    void detalleNoExponeIdDeCategoriaNiRelacionesAnidadas() throws Exception {
        MvcResult resultado = mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andReturn();
        Number idJson = com.jayway.jsonpath.JsonPath.read(resultado.getResponse().getContentAsString(), "$[0].id");
        long id = idJson.longValue();

        mvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoriaId").doesNotExist())
                .andExpect(jsonPath("$.categoria.id").doesNotExist())
                .andExpect(jsonPath("$.categoria").isString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarDosVecesDevuelve204Y404() throws Exception {
        String json = """
                {"titulo":"Temporal","precio":1500,"categoriaId":1,"existencias":1}
                """;
        String location = mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        org.junit.jupiter.api.Assertions.assertNotNull(location);
        mvc.perform(delete(location)).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(delete(location)).andExpect(status().isNotFound());
    }

    @Test
    void estadisticasDevuelve200() throws Exception {
        mvc.perform(get("/api/productos/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos").isNumber());
    }

    @Test
    void categoriaDesconocidaDevuelveListaVacia() throws Exception {
        mvc.perform(get("/api/productos?categoria=ninguna-categoria"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
