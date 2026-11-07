package com.wposs.catalogo.excepcion;

public class ReglaDeNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
