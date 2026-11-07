package com.wposs.catalogo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String usuario;

    @Column(nullable = false)
    private String contrasena;

    @Column(nullable = false, length = 254)
    private String correo;

    @Convert(converter = RolConverter.class)
    @Column(nullable = false, length = 32)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    protected Usuario() { }

    public Usuario(String usuario, String contrasena, String correo, Rol rol) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.correo = correo;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getContrasena() { return contrasena; }
    public String getCorreo() { return correo; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
