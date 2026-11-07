package com.wposs.catalogo.dto;

import java.math.BigDecimal;

public record ProductoDetalle(Long id, String titulo, BigDecimal precio, Integer existencias, String categoria) { }
