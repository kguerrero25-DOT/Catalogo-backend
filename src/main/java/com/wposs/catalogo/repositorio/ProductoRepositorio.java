package com.wposs.catalogo.repositorio;

import com.wposs.catalogo.modelo.Producto;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductoRepositorio extends JpaRepository<Producto,Long> {
    List<Producto> findByCategoria_NombreIgnoreCase(String nombre);
    List<Producto> findByPrecioBetween(BigDecimal min, BigDecimal max);
    Optional<Producto> findByTituloIgnoreCase(String titulo);
    @EntityGraph(attributePaths = {"categoria"})
    List<Producto> findByExistenciasLessThan(Integer limite);
    @EntityGraph(attributePaths = {"categoria"})
    Optional<Producto> findConCategoriaById(Long id);
    @Query("select p from Producto p join fetch p.categoria order by p.id")
    List<Producto> buscarConCategoria();
    interface ResumenCategoria { String getNombre(); Long getCantidad(); }
    @Query("select c.nombre as nombre, count(p) as cantidad from Producto p join p.categoria c group by c.nombre")
    List<ResumenCategoria> contarPorCategoria();
}
