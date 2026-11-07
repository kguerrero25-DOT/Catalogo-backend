package com.wposs.catalogo.controlador;

import com.wposs.catalogo.dto.EstadisticasCatalogo;
import com.wposs.catalogo.dto.ProductoActualizar;
import com.wposs.catalogo.dto.ProductoDetalle;
import com.wposs.catalogo.dto.ProductoNuevo;
import com.wposs.catalogo.dto.ProductoResumen;
import com.wposs.catalogo.servicio.ProductoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController
@RequestMapping("/api/productos")
public class ProductoControlador {
    private final ProductoServicio servicio;

    public ProductoControlador(ProductoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResumen>> listar(@RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(categoria == null
                ? servicio.buscarTodosResumen()
                : servicio.buscarPorCategoriaResumen(categoria));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasCatalogo> estadisticas() {
        return ResponseEntity.ok(servicio.estadisticasCatalogo());
    }

    @GetMapping("/sin-stock")
    public ResponseEntity<List<ProductoDetalle>> sinStock(
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "El límite debe ser como mínimo 1") int limite) {
        return ResponseEntity.ok(servicio.sinStockDetalle(limite));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDetalle> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.buscarDetallePorId(id));
    }

    @PostMapping
    public ResponseEntity<ProductoDetalle> crear(@Valid @RequestBody ProductoNuevo datos) {
        ProductoDetalle producto = servicio.crear(datos);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(producto.id())
                .toUri();
        return ResponseEntity.created(uri).body(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDetalle> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody ProductoActualizar datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminarRequerido(id);
        return ResponseEntity.noContent().build();
    }
}
