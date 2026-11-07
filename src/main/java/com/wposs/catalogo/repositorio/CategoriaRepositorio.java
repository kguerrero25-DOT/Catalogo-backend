package com.wposs.catalogo.repositorio;

import com.wposs.catalogo.modelo.Categoria;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepositorio extends JpaRepository<Categoria,Long> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
    @Query("select distinct c from Categoria c left join fetch c.productos")
    List<Categoria> findAllWithProductos();
}
