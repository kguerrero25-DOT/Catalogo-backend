package com.wposs.catalogo.mapper;

import com.wposs.catalogo.dto.ProductoDetalle;
import com.wposs.catalogo.dto.ProductoResumen;
import com.wposs.catalogo.modelo.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {
    public ProductoDetalle toDetalle(Producto producto) {
        return new ProductoDetalle(producto.getId(), producto.getTitulo(), producto.getPrecio(),
                producto.getExistencias(), producto.getCategoria().getNombre());
    }

    public ProductoResumen toResumen(Producto producto) {
        return new ProductoResumen(producto.getId(), producto.getTitulo(), producto.getPrecio());
    }
}
