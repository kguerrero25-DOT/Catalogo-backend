package com.wposs.catalogo.excepcion;

import com.wposs.catalogo.dto.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {
    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarNoEncontrado(
            RecursoNoEncontradoException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.valueOf(excepcion.getEstado()), excepcion.getMessage(), null, solicitud);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorRespuesta> manejarDuplicado(
            RecursoDuplicadoException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.CONFLICT, excepcion.getMessage(), null, solicitud);
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<ErrorRespuesta> manejarReglaDeNegocio(
            ReglaDeNegocioException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.CONFLICT, excepcion.getMessage(), null, solicitud);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(
            MethodArgumentNotValidException excepcion, HttpServletRequest solicitud) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : excepcion.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return respuesta(HttpStatus.BAD_REQUEST, "Hay campos con valores inválidos", campos, solicitud);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarRestricciones(
            ConstraintViolationException excepcion, HttpServletRequest solicitud) {
        Map<String, String> campos = excepcion.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violacion -> nombreCampo(violacion),
                        ConstraintViolation::getMessage,
                        (mensajeExistente, mensajeNuevo) -> mensajeExistente,
                        LinkedHashMap::new));
        return respuesta(HttpStatus.BAD_REQUEST, "Hay campos con valores inválidos", campos, solicitud);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarJsonInvalido(
            HttpMessageNotReadableException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es válido", null, solicitud);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoInvalido(
            MethodArgumentTypeMismatchException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.BAD_REQUEST, "Un parámetro de la solicitud tiene un formato inválido", null, solicitud);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridad(
            DataIntegrityViolationException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.CONFLICT, "La operación entra en conflicto con los datos existentes", null, solicitud);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorRespuesta> manejarCredencialesInvalidas(
            BadCredentialsException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.UNAUTHORIZED, "usuario o contraseña incorrectos", null, solicitud);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarErrorInesperado(
            Exception excepcion, HttpServletRequest solicitud) {
        log.error("Error interno al procesar la solicitud {}", solicitud.getRequestURI(), excepcion);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Intente nuevamente más tarde", null, solicitud);
    }

    private ResponseEntity<ErrorRespuesta> respuesta(HttpStatus estado, String mensaje,
                                                      Map<String, String> campos,
                                                      HttpServletRequest solicitud) {
        ErrorRespuesta cuerpo = new ErrorRespuesta(LocalDateTime.now(), estado.value(),
                estado.getReasonPhrase(), mensaje, solicitud.getRequestURI(), campos);
        return ResponseEntity.status(estado).body(cuerpo);
    }

    private String nombreCampo(ConstraintViolation<?> violacion) {
        String ruta = violacion.getPropertyPath().toString();
        int separador = ruta.lastIndexOf('.');
        return separador >= 0 ? ruta.substring(separador + 1) : ruta;
    }
}
