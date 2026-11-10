package com.wposs.catalogo.controlador;

import com.wposs.catalogo.dto.EstadisticasCatalogo;
import com.wposs.catalogo.dto.ProductoActualizar;
import com.wposs.catalogo.dto.ProductoDetalle;
import com.wposs.catalogo.dto.ProductoNuevo;
import com.wposs.catalogo.dto.ProductoResumen;
import com.wposs.catalogo.servicio.ProductoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Productos", description = "Consulta y administracion del catalogo de productos")
public class ProductoControlador {
    private final ProductoServicio servicio;

    public ProductoControlador(ProductoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Lista productos y permite filtrar por nombre de categoria.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Listado obtenido")})
    public ResponseEntity<List<ProductoResumen>> listar(@RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(categoria == null
                ? servicio.buscarTodosResumen()
                : servicio.buscarPorCategoriaResumen(categoria));
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Consultar estadisticas", description = "Devuelve totales de productos e inventario agrupados por categoria.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Estadisticas obtenidas")})
    public ResponseEntity<EstadisticasCatalogo> estadisticas() {
        return ResponseEntity.ok(servicio.estadisticasCatalogo());
    }

    @GetMapping("/sin-stock")
    @Operation(summary = "Consultar productos con pocas existencias", description = "Lista productos por debajo del limite indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "El limite debe ser mayor que cero")
    })
    public ResponseEntity<List<ProductoDetalle>> sinStock(
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "El límite debe ser como mínimo 1") int limite) {
        return ResponseEntity.ok(servicio.sinStockDetalle(limite));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar producto", description = "Busca el detalle de un producto por identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoDetalle> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.buscarDetallePorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear producto", description = "Crea un producto. Requiere rol ADMIN o EDITOR.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o categoria inexistente"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticacion"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "409", description = "Producto duplicado")
    })
    public ResponseEntity<ProductoDetalle> crear(@Valid @RequestBody ProductoNuevo datos) {
        ProductoDetalle producto = servicio.crear(datos);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(producto.id())
                .toUri();
        return ResponseEntity.created(uri).body(producto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza los datos del producto. Requiere rol ADMIN o EDITOR.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o categoria inexistente"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticacion"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Producto duplicado")
    })
    public ResponseEntity<ProductoDetalle> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody ProductoActualizar datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticacion"),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminarRequerido(id);
        return ResponseEntity.noContent().build();
    }
}
