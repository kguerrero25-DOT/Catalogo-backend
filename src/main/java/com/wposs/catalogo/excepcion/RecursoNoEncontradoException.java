package com.wposs.catalogo.excepcion;

public class RecursoNoEncontradoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int estado;

    public RecursoNoEncontradoException(String mensaje) {
        this(mensaje, 404);
    }

    public RecursoNoEncontradoException(String mensaje, int estado) {
        super(mensaje);
        this.estado = estado;
    }

    public int getEstado() {
        return estado;
    }
}
