package com.wposs.catalogo.modelo;

import java.math.BigDecimal;

public record Producto(Long id, String titulo, BigDecimal precio,
                       String categoria, Integer existencias) { }
