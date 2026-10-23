package com.wposs.catalogo.modelo;

import java.math.BigDecimal;
import java.util.Map;

public record Estadisticas(long totalProductos, BigDecimal valorTotalInventario,
                           Map<String, Long> cantidadPorCategoria, String iva) { }
