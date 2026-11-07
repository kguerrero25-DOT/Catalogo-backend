package com.wposs.catalogo.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wposs.catalogo.dto.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class RespuestaErrorSeguridad {
    private final ObjectMapper objectMapper;

    public RespuestaErrorSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void escribir(HttpServletRequest solicitud, HttpServletResponse respuesta,
                         HttpStatus estado, String mensaje) throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        ErrorRespuesta cuerpo = new ErrorRespuesta(LocalDateTime.now(), estado.value(),
                estado.getReasonPhrase(), mensaje, solicitud.getRequestURI(), Map.of());
        objectMapper.writeValue(respuesta.getOutputStream(), cuerpo);
    }
}
