package com.jorge.examenes.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interceptor global de excepciones para el microservicio de exámenes.
 * Captura los errores, formatea la salida en JSON y registra el problema.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Usamos el Logger nativo de Java
    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /**
     * Captura cualquier excepción genérica no controlada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        logError(ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno", "Ha ocurrido un error inesperado al procesar el examen.");
    }

    /**
     * Captura las excepciones de negocio generadas manualmente (ej: no hay suficientes preguntas).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(RuntimeException ex) {
        logError(ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Petición Inválida", ex.getMessage());
    }

    /**
     * Captura la excepción cuando se busca un examen o pregunta que no existe.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        logError(ex);
        return buildResponse(HttpStatus.NOT_FOUND, "No Encontrado", ex.getMessage());
    }

    /**
     * Construye el JSON de respuesta estandarizado.
     */
    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }

    /**
     * Registra el error extrayendo la ruta en la que ocurrió.
     */
    private void logError(Exception ex) {
        String endpoint = "Desconocido";
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            endpoint = attrs.getRequest().getRequestURI();
        }

        logger.log(Level.SEVERE, "Excepción capturada en {0}: {1}", new Object[]{endpoint, ex.getMessage()});

        // en una fase posterior, aquí haremos una llamada http (feignclient/resttemplate)
        // para enviar esta incidencia al incidencias-service.
    }
}