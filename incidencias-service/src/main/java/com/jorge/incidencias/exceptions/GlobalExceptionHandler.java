package com.jorge.incidencias.exceptions;

import com.jorge.incidencias.entity.Incidencia;
import com.jorge.incidencias.services.IncidenciasService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Se utiliza el logger nativo de Java para evitar dependencias externas como slf4j o log4j
    private static final Logger log = Logger.getLogger(GlobalExceptionHandler.class.getName());

    private final IncidenciasService incidenciaService;

    // Solo inyectamos IncidenciasService (aquí no existe UserRepository)
    public GlobalExceptionHandler(IncidenciasService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        registrarIncidencia(ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno", "Ha ocurrido un error inesperado.");
    }

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

    private void registrarIncidencia(Exception ex) {
        String endpoint = "Desconocido";

        // Como aquí no tenemos acceso a la base de datos de usuarios, asignamos 0L temporalmente.
        Long idUsuario = 0L;

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            endpoint = attrs.getRequest().getRequestURI();
        }

        String tipo = ex.getClass().getSimpleName();
        String clase = "Desconocida";
        String metodo = "Desconocido";

        if (ex.getStackTrace() != null && ex.getStackTrace().length > 0) {
            StackTraceElement elemento = ex.getStackTrace()[0];
            clase = elemento.getClassName();
            metodo = elemento.getMethodName();
        }

        if (clase.contains(".")) {
            clase = clase.substring(clase.lastIndexOf(".") + 1);
        }

        if (clase.contains("$$")) {
            clase = clase.substring(0, clase.indexOf("$$"));
        }

        if (metodo.startsWith("lambda$")) {
            metodo = metodo.split("\\$")[1];
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String trazaCompleta = sw.toString();
        String traza = trazaCompleta.length() > 2000 ? trazaCompleta.substring(0, 2000) : trazaCompleta;

        // Aquí SÍ guardamos usando el objeto Incidencia y el servicio directamente
        Incidencia incidencia = new Incidencia();
        incidencia.setEndpoint(endpoint);
        incidencia.setTipo(tipo);
        incidencia.setClase(clase);
        incidencia.setMetodo(metodo);
        incidencia.setTraza(traza);
        incidencia.setFecha(LocalDateTime.now());
        incidencia.setIdUsuario(idUsuario);

        incidenciaService.guardar(incidencia);
    }
}