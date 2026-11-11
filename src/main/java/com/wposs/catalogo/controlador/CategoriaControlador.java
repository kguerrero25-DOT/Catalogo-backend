package com.wposs.catalogo.controlador;

import com.wposs.catalogo.dto.CategoriaDetalle;
import com.wposs.catalogo.dto.CategoriaNueva;
import com.wposs.catalogo.servicio.CategoriaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Categorias", description = "Consulta y administracion de categorias")
public class CategoriaControlador {
    private final CategoriaServicio servicio;

    public CategoriaControlador(CategoriaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Listar categorias",
            description = "Lista las categorias disponibles. Acceso publico; devuelve el listado de categorias.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Listado obtenido")})
    public ResponseEntity<List<CategoriaDetalle>> listar() {
        return ResponseEntity.ok(servicio.listarDetalles());
    }

    @PostMapping
    @Operation(summary = "Crear categoria", description = "Crea una categoria. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticacion"),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "La categoria ya existe")
    })
    public ResponseEntity<CategoriaDetalle> crear(@Valid @RequestBody CategoriaNueva datos) {
        CategoriaDetalle categoria = servicio.crear(datos);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoria.id())
                .toUri();
        return ResponseEntity.created(uri).body(categoria);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoria", description = "Elimina una categoria sin productos asociados. Requiere rol ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria eliminada"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticacion"),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Categoria no encontrada"),
            @ApiResponse(responseCode = "409", description = "La categoria tiene productos asociados")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminarRequerido(id);
        return ResponseEntity.noContent().build();
    }
}
