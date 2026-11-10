package com.wposs.catalogo.config;

import com.wposs.catalogo.modelo.Categoria;
import com.wposs.catalogo.modelo.Producto;
import com.wposs.catalogo.repositorio.CategoriaRepositorio;
import com.wposs.catalogo.repositorio.ProductoRepositorio;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DatosIniciales {
    @Bean
    CommandLineRunner cargarProductosDePrueba(CategoriaRepositorio categorias,
                                              ProductoRepositorio productos) {
        return argumentos -> {
            if (productos.count() > 0) {
                return;
            }

            List<ProductoSemilla> semillas = List.of(
                    new ProductoSemilla("Java para principiantes", "45000", "libros", 12),
                    new ProductoSemilla("Clean Code", "95000", "libros", 5),
                    new ProductoSemilla("Spring Boot en practica", "78000", "libros", 7),
                    new ProductoSemilla("Patrones de diseño", "99000", "libros", 8),
                    new ProductoSemilla("Arquitectura limpia", "88000", "libros", 10),
                    new ProductoSemilla("Teclado mecanico", "180000", "tecnologia", 9),
                    new ProductoSemilla("Mouse inalambrico", "65000", "tecnologia", 20),
                    new ProductoSemilla("Monitor 24 pulgadas", "650000", "tecnologia", 4),
                    new ProductoSemilla("Memoria USB", "35000", "tecnologia", 15),
                    new ProductoSemilla("Auriculares", "90000", "tecnologia", 6),
                    new ProductoSemilla("Cuaderno universitario", "12000", "papeleria", 35),
                    new ProductoSemilla("Boligrafo azul", "2500", "papeleria", 100),
                    new ProductoSemilla("Agenda", "18000", "papeleria", 12),
                    new ProductoSemilla("Lapiz", "1000", "papeleria", 80),
                    new ProductoSemilla("Resaltador", "4500", "papeleria", 45),
                    new ProductoSemilla("Lampara", "55000", "hogar", 9),
                    new ProductoSemilla("Organizador", "25000", "hogar", 13),
                    new ProductoSemilla("Reloj", "85000", "hogar", 4),
                    new ProductoSemilla("Almohada", "32000", "hogar", 17),
                    new ProductoSemilla("Taza", "16000", "hogar", 24));

            List<Producto> datos = semillas.stream()
                    .map(semilla -> new Producto(semilla.titulo(), new BigDecimal(semilla.precio()),
                            categorias.findByNombreIgnoreCase(semilla.categoria()).orElseThrow(),
                            semilla.existencias()))
                    .toList();
            productos.saveAll(datos);
        };
    }

    private record ProductoSemilla(String titulo, String precio, String categoria, int existencias) {
    }
}
