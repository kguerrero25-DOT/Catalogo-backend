package com.wposs.catalogo.repositorio;

import com.wposs.catalogo.modelo.Producto;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

@Repository
public class ProductoRepositorio {
    private final Map<Long, Producto> productos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public ProductoRepositorio() {
        guardar(new Producto(null, "Java para principiantes", new BigDecimal("45000"), "libros", 12));
        guardar(new Producto(null, "Clean Code", new BigDecimal("95000"), "libros", 5));
        guardar(new Producto(null, "Spring Boot en practica", new BigDecimal("78000"), "libros", 7));
        guardar(new Producto(null, "Teclado mecanico", new BigDecimal("180000"), "tecnologia", 9));
        guardar(new Producto(null, "Mouse inalambrico", new BigDecimal("65000"), "tecnologia", 20));
        guardar(new Producto(null, "Monitor 24 pulgadas", new BigDecimal("650000"), "tecnologia", 4));
        guardar(new Producto(null, "Cuaderno universitario", new BigDecimal("12000"), "papeleria", 35));
        guardar(new Producto(null, "Boligrafo azul", new BigDecimal("2500"), "papeleria", 100));
    }

    public List<Producto> buscarTodos() {
        return productos.values().stream().sorted(Comparator.comparing(Producto::id)).toList();
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        return productos.values().stream()
                .filter(p -> p.categoria().equalsIgnoreCase(categoria))
                .sorted(Comparator.comparing(Producto::id)).toList();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return Optional.ofNullable(productos.get(id));
    }

    public Producto guardar(Producto producto) {
        Long id = producto.id() == null ? secuencia.incrementAndGet() : producto.id();
        Producto guardado = new Producto(id, producto.titulo(), producto.precio(),
                producto.categoria(), producto.existencias());
        productos.put(id, guardado);
        return guardado;
    }

    public boolean eliminar(Long id) {
        return productos.remove(id) != null;
    }
}
