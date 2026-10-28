package com.wposs.catalogo.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="categorias")
public class Categoria {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false,unique=true)
    private String nombre;
    @JsonIgnore
    @OneToMany(mappedBy="categoria")
    private List<Producto> productos = new ArrayList<>();
    protected Categoria() {}
    public Categoria(String nombre) { this.nombre=nombre; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre=nombre; }
    public List<Producto> getProductos() { return productos; }
}
