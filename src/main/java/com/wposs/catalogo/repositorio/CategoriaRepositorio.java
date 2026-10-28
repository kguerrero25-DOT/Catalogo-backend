package com.wposs.catalogo.repositorio;

import com.wposs.catalogo.modelo.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoriaRepositorio extends JpaRepository<Categoria,Long> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
