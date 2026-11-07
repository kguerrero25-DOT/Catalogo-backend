package com.wposs.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wposs.catalogo.modelo.Categoria;
import com.wposs.catalogo.modelo.Producto;
import com.wposs.catalogo.repositorio.CategoriaRepositorio;
import com.wposs.catalogo.repositorio.ProductoRepositorio;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Import(TransaccionesDemostracionTest.Configuracion.class)
class TransaccionesDemostracionTest extends BasePruebasIntegracion {
    @Autowired
    private ProductoRepositorio productos;

    @Autowired
    private CategoriaRepositorio categorias;

    @Autowired
    private DemostracionTransaccional demostracion;

    @Test
    void excepcionCheckedConfirmaYRuntimeRevierteLaTransaccion() throws Exception {
        Categoria categoria = categorias.findByNombreIgnoreCase("libros").orElseThrow();
        Producto producto = productos.saveAndFlush(
                new Producto("Producto transaccional de prueba", new BigDecimal("10.00"), categoria, 1));
        BigDecimal precioChecked = new BigDecimal("20.00");
        BigDecimal precioRuntime = new BigDecimal("30.00");

        try {
            assertThrows(FalloChecked.class,
                    () -> demostracion.modificarYFallarConChecked(producto.getId(), precioChecked));
            assertEquals(precioChecked, productos.findById(producto.getId()).orElseThrow().getPrecio());

            assertThrows(IllegalStateException.class,
                    () -> demostracion.modificarYFallarConRuntime(producto.getId(), precioRuntime));
            assertEquals(precioChecked, productos.findById(producto.getId()).orElseThrow().getPrecio());
        } finally {
            productos.deleteById(producto.getId());
        }
    }

    @TestConfiguration
    static class Configuracion {
        @Bean
        DemostracionTransaccional demostracionTransaccional(ProductoRepositorio productos) {
            return new DemostracionTransaccional(productos);
        }
    }

    static class DemostracionTransaccional {
        private final ProductoRepositorio productos;

        DemostracionTransaccional(ProductoRepositorio productos) {
            this.productos = productos;
        }

        @Transactional
        public void modificarYFallarConChecked(Long id, BigDecimal precio) throws FalloChecked {
            Producto producto = productos.findById(id).orElseThrow();
            producto.setPrecio(precio);
            throw new FalloChecked();
        }

        @Transactional
        public void modificarYFallarConRuntime(Long id, BigDecimal precio) {
            Producto producto = productos.findById(id).orElseThrow();
            producto.setPrecio(precio);
            throw new IllegalStateException("Fallo de demostración");
        }
    }

    static class FalloChecked extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
