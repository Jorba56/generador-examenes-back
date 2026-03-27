package com.jorge.incidencias.exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorge.incidencias.entity.Incidencia;
import com.jorge.incidencias.services.IncidenciasService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Interceptor global de excepciones (Spring Advice) que captura cualquier error no controlado
 * lanzado desde los controladores o servicios. Formatea la salida de error en un JSON estándar
 * y registra la incidencia automáticamente en la base de datos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final IncidenciasService incidenciaService;

    // Solo inyectamos IncidenciasService (aquí no existe UserRepository)
    public GlobalExceptionHandler(IncidenciasService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    /**
     * Captura cualquier excepción genérica (Exception) no controlada por otros manejadores.
     * Registra el error en la base de datos y devuelve una respuesta HTTP 500 (Internal Server Error).
     *
     * @param ex La excepción capturada.
     * @return Respuesta estandarizada en formato JSON.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        registrarIncidencia(ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno", "Ha ocurrido un error inesperado.");
    }

    /**
     * Maneja las excepciones personalizadas de tipo NotFoundException.
     * Registra el suceso y devuelve una respuesta HTTP 404 (Not Found).
     *
     * @param ex La excepción NotFoundException capturada.
     * @return Respuesta estandarizada detallando el recurso no encontrado.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        registrarIncidencia(ex);
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }

    /**
     * Extrae de forma reflexiva los metadatos de la excepción (endpoint, clase, método y stacktrace).
     * Limpia los nombres de clases generadas dinámicamente o lambdas para una mayor legibilidad,
     * trunca la traza a 2000 caracteres para evitar desbordamientos y persiste la incidencia.
     *
     * @param ex La excepción de la cual se extraerá el contexto.
     */
    private void registrarIncidencia(Exception ex) {
        String endpoint = "Desconocido";
        Long idUsuario = 0L;

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            endpoint = request.getRequestURI();
            idUsuario = extraerIdDelToken(request);
        }

        String tipo = ex.getClass().getSimpleName();
        String clase = "Desconocida";
        String metodo = "Desconocido";

        if (ex.getStackTrace() != null && ex.getStackTrace().length > 0) {
            StackTraceElement elemento = ex.getStackTrace()[0];
            clase = limpiarNombreClase(elemento.getClassName()); // 🚀 Lógica delegada
            metodo = limpiarNombreMetodo(elemento.getMethodName()); // 🚀 Lógica delegada
        }

        String traza = obtenerTrazaRecortada(ex); // 🚀 Lógica delegada

        Incidencia incidencia = new Incidencia();
        incidencia.setEndpoint(endpoint);
        incidencia.setTipo(tipo);
        incidencia.setClase(clase);
        incidencia.setMetodo(metodo);
        incidencia.setTraza(traza);
        incidencia.setIdUsuario(idUsuario);
        incidenciaService.guardar(incidencia);
    }

    private Long extraerIdDelToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // Retorno temprano si no hay cabecera válida
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 0L;
        }

        try {
            String token = authHeader.substring(7);
            String[] partes = token.split("\\.");

            // Retorno temprano si el token no tiene el formato correcto
            if (partes.length <= 1) {
                return 0L;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(partes[1]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode nodo = mapper.readTree(payloadJson);

            return nodo.has("id_user") ? nodo.get("id_user").asLong() : 0L;

        } catch (Exception e) {
            return 0L; // Si el token es inválido o falla el parseo, se queda en 0L
        }
    }

    private String limpiarNombreClase(String claseOriginal) {
        String claseLimpia = claseOriginal;
        if (claseLimpia.contains(".")) {
            claseLimpia = claseLimpia.substring(claseLimpia.lastIndexOf(".") + 1);
        }
        if (claseLimpia.contains("$$")) {
            claseLimpia = claseLimpia.substring(0, claseLimpia.indexOf("$$"));
        }
        return claseLimpia;
    }

    private String limpiarNombreMetodo(String metodoOriginal) {
        if (metodoOriginal.startsWith("lambda$")) {
            return metodoOriginal.split("\\$")[1];
        }
        return metodoOriginal;
    }

    private String obtenerTrazaRecortada(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        String trazaCompleta = sw.toString();
        return trazaCompleta.length() > 2000 ? trazaCompleta.substring(0, 2000) : trazaCompleta;
    }
}