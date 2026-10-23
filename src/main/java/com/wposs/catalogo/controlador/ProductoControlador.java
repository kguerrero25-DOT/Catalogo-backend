package com.wposs.catalogo.controlador;

import com.wposs.catalogo.modelo.Estadisticas;
import com.wposs.catalogo.modelo.Producto;
import com.wposs.catalogo.servicio.ProductoServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoControlador {
    private final ProductoServicio servicio;

    public ProductoControlador(ProductoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listar(@RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(categoria == null
                ? servicio.buscarTodos() : servicio.buscarPorCategoria(categoria));
    }

    // Debe ir antes de /{id} para evitar que "estadisticas" se trate como identificador.
    @GetMapping("/estadisticas")
    public ResponseEntity<Estadisticas> estadisticas() {
        return ResponseEntity.ok(servicio.estadisticas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscar(@PathVariable Long id) {
        return servicio.buscarPorId(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        Producto creado = servicio.crear(producto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return servicio.actualizar(id, producto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return servicio.eliminar(id) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> datosInvalidos(IllegalArgumentException excepcion) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Void> jsonInvalido(org.springframework.http.converter.HttpMessageNotReadableException excepcion) {
        return ResponseEntity.badRequest().build();
    }
}
