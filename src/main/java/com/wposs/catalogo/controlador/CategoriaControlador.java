package com.wposs.catalogo.controlador;

import com.wposs.catalogo.modelo.Categoria;
import com.wposs.catalogo.servicio.CategoriaServicio;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaControlador {
    private final CategoriaServicio servicio;
    public CategoriaControlador(CategoriaServicio servicio){this.servicio=servicio;}
    public record CategoriaDatos(String nombre) {}
    @GetMapping public List<Categoria> listar(){return servicio.listar();}
    @PostMapping public ResponseEntity<Categoria> crear(@RequestBody CategoriaDatos datos){Categoria c=servicio.crear(datos.nombre());URI uri=ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(c.getId()).toUri();return ResponseEntity.created(uri).body(c);}
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id){try{return servicio.eliminar(id)?ResponseEntity.noContent().build():ResponseEntity.notFound().build();}catch(DataIntegrityViolationException ex){return ResponseEntity.status(409).build();}}
    @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<Void> invalido(){return ResponseEntity.badRequest().build();}
}
