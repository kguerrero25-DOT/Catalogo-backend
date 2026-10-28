package com.wposs.catalogo.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="productos")
public class Producto {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String titulo;
    @Column(nullable=false,precision=12,scale=2)
    private BigDecimal precio;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="categoria_id",nullable=false)
    @JsonIgnore
    private Categoria categoria;
    @Column(nullable=false)
    private Integer existencias;
    protected Producto() {}
    public Producto(String titulo, BigDecimal precio, Categoria categoria, Integer existencias) {
        this.titulo=titulo;this.precio=precio;this.categoria=categoria;this.existencias=existencias;
    }
    public Long getId(){return id;}
    public String getTitulo(){return titulo;}
    public void setTitulo(String titulo){this.titulo=titulo;}
    public BigDecimal getPrecio(){return precio;}
    public void setPrecio(BigDecimal precio){this.precio=precio;}
    public Categoria getCategoria(){return categoria;}
    public void setCategoria(Categoria categoria){this.categoria=categoria;}
    @JsonProperty("categoria")
    public String getCategoriaNombre(){return categoria==null?null:categoria.getNombre();}
    @JsonProperty("categoria_id")
    public Long getCategoriaId(){return categoria==null?null:categoria.getId();}
    public Integer getExistencias(){return existencias;}
    public void setExistencias(Integer existencias){this.existencias=existencias;}
}
