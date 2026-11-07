package com.wposs.catalogo.mapper;

import com.wposs.catalogo.dto.CategoriaDetalle;
import com.wposs.catalogo.modelo.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {
    public CategoriaDetalle toDetalle(Categoria categoria) {
        return new CategoriaDetalle(categoria.getId(), categoria.getNombre(), categoria.getDescripcion(),
                (long) categoria.getProductos().size());
    }
}
