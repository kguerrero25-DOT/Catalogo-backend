package com.wposs.catalogo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControladorTest {
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
                .andExpect(status().isNotFound());
    }

    @Test
    void crearValidoDevuelve201YLocation() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" 
                            {"titulo":"Nuevo libro","precio":20000,"categoria":"libros","existencias":3}
                            """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/productos/")))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void crearPrecioNegativoDevuelve400() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"titulo":"Incorrecto","precio":-5,"categoria":"libros","existencias":1}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarDosVecesDevuelve204Y404() throws Exception {
        String json = """
                {"titulo":"Temporal","precio":1500,"categoria":"papeleria","existencias":1}
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
