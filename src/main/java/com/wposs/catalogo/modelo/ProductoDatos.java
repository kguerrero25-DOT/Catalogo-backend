package com.wposs.catalogo.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record ProductoDatos(String titulo,BigDecimal precio,String categoria,
                            @JsonProperty("categoria_id") Long categoriaId,Integer existencias) {}
