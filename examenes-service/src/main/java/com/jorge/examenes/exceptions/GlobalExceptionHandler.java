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
        Long idUsuario = 0L; // Por defecto

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            jakarta.servlet.http.HttpServletRequest request = attrs.getRequest(); // Usa javax.servlet si estás en Spring Boot 2
            endpoint = request.getRequestURI();

            // 🚀 EXTRAEMOS EL ID DEL TOKEN A MANO
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String[] partes = token.split("\\.");
                    if (partes.length > 1) {
                        // Decodificamos el payload (la parte del medio del JWT)
                        String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(partes[1]));

                        // Leemos el JSON usando Jackson (nativo de Spring)
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        com.fasterxml.jackson.databind.JsonNode nodo = mapper.readTree(payloadJson);

                        // Buscamos tu clave id_user
                        if (nodo.has("id_user")) {
                            idUsuario = nodo.get("id_user").asLong();
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "No se pudo extraer el ID del token JWT: {0}", e.getMessage());
                }
            }
        }

        logger.log(Level.SEVERE, "Excepción capturada en {0}: {1}", new Object[]{endpoint, ex.getMessage()});

        // 1. Extraemos los detalles exactos de la excepción
        String tipo = ex.getClass().getSimpleName();
        String clase = "Desconocida";
        String metodo = "Desconocido";

        if (ex.getStackTrace() != null && ex.getStackTrace().length > 0) {
            StackTraceElement elemento = ex.getStackTrace()[0];
            clase = elemento.getClassName();
            metodo = elemento.getMethodName();
        }

        if (clase.contains(".")) clase = clase.substring(clase.lastIndexOf(".") + 1);
        if (clase.contains("$$")) clase = clase.substring(0, clase.indexOf("$$"));
        if (metodo.startsWith("lambda$")) metodo = metodo.split("\\$")[1];

        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        ex.printStackTrace(pw);
        String trazaCompleta = sw.toString();
        String traza = trazaCompleta.length() > 2000 ? trazaCompleta.substring(0, 2000) : trazaCompleta;

        // 2. Construimos el JSON para enviarlo al microservicio de Incidencias
        Map<String, Object> incidenciaJson = new java.util.HashMap<>();
        incidenciaJson.put("endpoint", endpoint);
        incidenciaJson.put("tipo", tipo);
        incidenciaJson.put("clase", clase);
        incidenciaJson.put("metodo", metodo);
        incidenciaJson.put("traza", traza);
        incidenciaJson.put("fecha", java.time.LocalDateTime.now().toString());

        // 🔥 AQUÍ INYECTAMOS EL ID REAL QUE HEMOS CAPTURADO
        incidenciaJson.put("id_usuario", idUsuario);

        // 3. Enviamos la petición con RestTemplate
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            // Cuidado con la URL, asegúrate de que es correcta en tu entorno Docker
            restTemplate.postForObject("http://host.docker.internal:8082/incidencias", incidenciaJson, String.class);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "No se pudo comunicar con el servidor de incidencias: {0}", e.getMessage());
        }
    }
}