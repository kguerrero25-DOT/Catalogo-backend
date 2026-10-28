package com.wposs.catalogo.controlador;

import com.wposs.catalogo.modelo.*;
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
    public ProductoControlador(ProductoServicio servicio){this.servicio=servicio;}
    @GetMapping
    public ResponseEntity<List<Producto>> listar(@RequestParam(required=false) String categoria){return ResponseEntity.ok(categoria==null?servicio.buscarTodos():servicio.buscarPorCategoria(categoria));}



    @GetMapping("/estadisticas")
    public ResponseEntity<Estadisticas> estadisticas(){return ResponseEntity.ok(servicio.estadisticas());}
    @GetMapping("/sin-stock")
    public ResponseEntity<List<Producto>> sinStock(@RequestParam(defaultValue="5") int limite){return ResponseEntity.ok(servicio.sinStock(limite));}
    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscar(@PathVariable Long id){return servicio.buscarPorId(id).map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());}
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody ProductoDatos datos){Producto p=servicio.crear(datos);URI uri=ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(p.getId()).toUri();return ResponseEntity.created(uri).body(p);}
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id,@RequestBody ProductoDatos datos){return servicio.actualizar(id,datos).map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){return servicio.eliminar(id)?ResponseEntity.noContent().build():ResponseEntity.notFound().build();}
    @ExceptionHandler({IllegalArgumentException.class,org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<Void> invalido(Exception ex){return ResponseEntity.badRequest().build();}
}
