package com.wposs.catalogo.modelo;

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
    @Column(length=500)
    private String descripcion;
    @OneToMany(mappedBy="categoria")
    private List<Producto> productos = new ArrayList<>();
    protected Categoria() {}
    public Categoria(String nombre) { this(nombre, null); }
    public Categoria(String nombre, String descripcion) { this.nombre=nombre; this.descripcion=descripcion; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre=nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion=descripcion; }
    public List<Producto> getProductos() { return productos; }
}
