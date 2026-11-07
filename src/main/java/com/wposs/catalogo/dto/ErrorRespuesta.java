package com.wposs.catalogo.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorRespuesta(LocalDateTime momento, int estado, String error, String mensaje,
                             String ruta, Map<String, String> campos) { }
