package com.wposs.catalogo.modelo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RolConverter implements AttributeConverter<Rol, String> {
    @Override
    public String convertToDatabaseColumn(Rol rol) {
        return rol == null ? null : "ROLE_" + rol.name();
    }

    @Override
    public Rol convertToEntityAttribute(String valor) {
        if (valor == null) {
            return null;
        }
        String nombreRol = valor.startsWith("ROLE_") ? valor.substring(5) : valor;
        return Rol.valueOf(nombreRol);
    }
}
