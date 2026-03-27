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
        Long idUsuario = 0L;

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            jakarta.servlet.http.HttpServletRequest request = attrs.getRequest();
            endpoint = request.getRequestURI();
            idUsuario = extraerIdUsuarioDelToken(request);
        }

        logger.log(Level.SEVERE, "Excepción capturada en {0}: {1}", new Object[]{endpoint, ex.getMessage()});

        Map<String, Object> incidenciaJson = construirPayloadIncidencia(ex, endpoint, idUsuario);
        enviarIncidencia(incidenciaJson);
    }

    private Long extraerIdUsuarioDelToken(jakarta.servlet.http.HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // Early return: Si no hay cabecera válida, salimos directamente
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 0L;
        }

        try {
            String token = authHeader.substring(7);
            String[] partes = token.split("\\.");

            // Early return: Si el token está mal formado
            if (partes.length <= 1) {
                return 0L;
            }

            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(partes[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode nodo = mapper.readTree(payloadJson);

            return nodo.has("id_user") ? nodo.get("id_user").asLong() : 0L;

        } catch (Exception e) {
            logger.log(Level.WARNING, "No se pudo extraer el ID del token JWT: {0}", e.getMessage());
            return 0L;
        }
    }

    private Map<String, Object> construirPayloadIncidencia(Exception ex, String endpoint, Long idUsuario) {
        String clase = "Desconocida";
        String metodo = "Desconocido";

        if (ex.getStackTrace() != null && ex.getStackTrace().length > 0) {
            StackTraceElement elemento = ex.getStackTrace()[0];
            clase = limpiarNombreClase(elemento.getClassName());
            metodo = limpiarNombreMetodo(elemento.getMethodName());
        }

        Map<String, Object> incidenciaJson = new java.util.HashMap<>();
        incidenciaJson.put("endpoint", endpoint);
        incidenciaJson.put("tipo", ex.getClass().getSimpleName());
        incidenciaJson.put("clase", clase);
        incidenciaJson.put("metodo", metodo);
        incidenciaJson.put("traza", formatearTraza(ex));
        incidenciaJson.put("fecha", java.time.LocalDateTime.now().toString());
        incidenciaJson.put("id_usuario", idUsuario);

        return incidenciaJson;
    }

    private String limpiarNombreClase(String clase) {
        if (clase.contains(".")) clase = clase.substring(clase.lastIndexOf(".") + 1);
        if (clase.contains("$$")) clase = clase.substring(0, clase.indexOf("$$"));
        return clase;
    }

    private String limpiarNombreMetodo(String metodo) {
        return metodo.startsWith("lambda$") ? metodo.split("\\$")[1] : metodo;
    }

    private String formatearTraza(Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        String trazaCompleta = sw.toString();
        return trazaCompleta.length() > 2000 ? trazaCompleta.substring(0, 2000) : trazaCompleta;
    }

    private void enviarIncidencia(Map<String, Object> incidenciaJson) {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            restTemplate.postForObject("http://host.docker.internal:8082/incidencias", incidenciaJson, String.class);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "No se pudo comunicar con el servidor de incidencias: {0}", e.getMessage());
        }
    }
}
