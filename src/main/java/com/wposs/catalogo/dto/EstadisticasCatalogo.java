package com.wposs.catalogo.dto;

import java.math.BigDecimal;
import java.util.Map;

public record EstadisticasCatalogo(long totalProductos, BigDecimal valorTotalInventario,
                                   Map<String, Long> cantidadPorCategoria, String iva) { }
