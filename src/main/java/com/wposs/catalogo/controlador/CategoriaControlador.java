package com.wposs.catalogo.controlador;

import com.wposs.catalogo.dto.CategoriaDetalle;
import com.wposs.catalogo.dto.CategoriaNueva;
import com.wposs.catalogo.servicio.CategoriaServicio;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaControlador {
    private final CategoriaServicio servicio;

    public CategoriaControlador(CategoriaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDetalle>> listar() {
        return ResponseEntity.ok(servicio.listarDetalles());
    }

    @PostMapping
    public ResponseEntity<CategoriaDetalle> crear(@Valid @RequestBody CategoriaNueva datos) {
        CategoriaDetalle categoria = servicio.crear(datos);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoria.id())
                .toUri();
        return ResponseEntity.created(uri).body(categoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminarRequerido(id);
        return ResponseEntity.noContent().build();
    }
}
