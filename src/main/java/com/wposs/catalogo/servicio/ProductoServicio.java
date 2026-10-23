package com.wposs.catalogo.servicio;

import com.wposs.catalogo.modelo.Estadisticas;
import com.wposs.catalogo.modelo.Producto;
import com.wposs.catalogo.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ProductoServicio {
    private final ProductoRepositorio repositorio;
    private final String iva;

    public ProductoServicio(ProductoRepositorio repositorio,
                            @Value("${catalogo.iva:0.19}") String iva) {
        this.repositorio = repositorio;
        this.iva = iva;
    }

    public List<Producto> buscarTodos() {
        return repositorio.buscarTodos();
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        return repositorio.buscarPorCategoria(categoria);
    }

    public Optional<Producto> buscarPorId(Long id) {
        return repositorio.buscarPorId(id);
    }

    public Producto crear(Producto producto) {
        validar(producto);
        return repositorio.guardar(new Producto(null, producto.titulo().trim(),
                producto.precio(), producto.categoria().trim(), producto.existencias()));
    }

    public Optional<Producto> actualizar(Long id, Producto producto) {
        validar(producto);
        if (repositorio.buscarPorId(id).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(repositorio.guardar(new Producto(id, producto.titulo().trim(),
                producto.precio(), producto.categoria().trim(), producto.existencias())));
    }

    public boolean eliminar(Long id) {
        return repositorio.eliminar(id);
    }

    public Estadisticas estadisticas() {
        List<Producto> lista = repositorio.buscarTodos();
        BigDecimal valor = lista.stream()
                .map(p -> p.precio().multiply(BigDecimal.valueOf(p.existencias())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> categorias = lista.stream().collect(Collectors.groupingBy(
                Producto::categoria, TreeMap::new, Collectors.counting()));
        return new Estadisticas(lista.size(), valor, categorias, iva);
    }

    private void validar(Producto producto) {
        if (producto == null || producto.titulo() == null || producto.titulo().isBlank()
                || producto.categoria() == null || producto.categoria().isBlank()
                || producto.precio() == null || producto.precio().compareTo(BigDecimal.ZERO) <= 0
                || producto.existencias() == null || producto.existencias() < 0) {
            throw new IllegalArgumentException("Datos del producto invalidos");
        }
    }
}
