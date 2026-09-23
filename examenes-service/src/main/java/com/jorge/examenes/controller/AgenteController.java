package com.jorge.examenes.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorge.examenes.dto.EstadisticasAlumnoDTO;
import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import com.jorge.examenes.services.EvaluacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/agente")
public class AgenteController {

    private final EvaluacionService evaluacionService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    // Regex para extraer email y números del mensaje
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern ID_PATTERN =
            Pattern.compile("(?i)(?:examen|id|examen id|número|numero)[^0-9]*([0-9]+)");

    private static final String SYSTEM_PROMPT = """
            Eres un asistente virtual inteligente para profesores y administradores de una plataforma de exámenes.
            Tu objetivo es analizar el rendimiento, notas y estadísticas de los alumnos de forma clara y profesional.
            Cuando te proporcionen datos en JSON, analízalos y responde en lenguaje natural claro y estructurado.
            NUNCA te inventes datos. Si no hay datos, indícalo claramente.
            Si ves una tendencia en las notas, menciónala.
            Responde siempre en español.
            """;

    public AgenteController(EvaluacionService evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping("/chat")
    public String chatearConAgente(
            @RequestParam String mensaje,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) Long idExamen) {

        // 1. Intentamos extraer correo e id del propio mensaje si no vienen como parámetros
        String correoFinal = correo;
        Long idExamenFinal = idExamen;

        if (correoFinal == null || correoFinal.isBlank()) {
            Matcher emailMatcher = EMAIL_PATTERN.matcher(mensaje);
            if (emailMatcher.find()) {
                correoFinal = emailMatcher.group();
                System.out.println("[AGENTE-DEBUG] Correo extraído del mensaje: " + correoFinal);
            }
        }

        if (idExamenFinal == null) {
            Matcher idMatcher = ID_PATTERN.matcher(mensaje);
            if (idMatcher.find()) {
                idExamenFinal = Long.parseLong(idMatcher.group(1));
                System.out.println("[AGENTE-DEBUG] ID examen extraído del mensaje: " + idExamenFinal);
            }
        }

        // 2. Recopilamos datos reales de la BD con lo que tenemos
        StringBuilder contexto = new StringBuilder();
        contexto.append(SYSTEM_PROMPT).append("\n\n");
        contexto.append("Pregunta del usuario: ").append(mensaje).append("\n\n");

        boolean hayDatos = false;

        if (correoFinal != null && !correoFinal.isBlank()) {
            try {
                EstadisticasAlumnoDTO stats = evaluacionService
                        .obtenerEstadisticasAlumno(correoFinal);
                contexto.append("=== ESTADÍSTICAS DEL ALUMNO: ").append(correoFinal).append(" ===\n");
                contexto.append(objectMapper.writeValueAsString(stats)).append("\n\n");

                List<EvaluacionHistorialDTO> historial = evaluacionService
                        .obtenerHistorialAlumno(correoFinal, "fecha", "desc");
                contexto.append("=== HISTORIAL DE EXÁMENES ===\n");
                contexto.append(objectMapper.writeValueAsString(historial)).append("\n\n");
                hayDatos = true;
            } catch (Exception e) {
                contexto.append("No se encontraron datos para el alumno: ")
                        .append(correoFinal).append("\n\n");
            }
        }

        if (idExamenFinal != null) {
            try {
                List<EvaluacionHistorialDTO> notas = evaluacionService
                        .obtenerNotasExamen(idExamenFinal, "nota", "desc");
                contexto.append("=== NOTAS DEL EXAMEN ID: ").append(idExamenFinal).append(" ===\n");
                contexto.append(objectMapper.writeValueAsString(notas)).append("\n\n");
                hayDatos = true;
            } catch (Exception e) {
                contexto.append("No se encontraron datos para el examen ID: ")
                        .append(idExamenFinal).append("\n\n");
            }
        }

        if (!hayDatos) {
            contexto.append("""
                No se han proporcionado datos específicos de la BD.
                Responde la pregunta del usuario de forma general basándote en tu conocimiento
                sobre educación y análisis de rendimiento académico.
                Si el usuario pregunta por un alumno o examen concreto, indícale que debe
                proporcionar el correo del alumno o el ID del examen.
                """);
        }

        // 3. Llamamos a Gemini con todo el contexto
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", contexto.toString())
                            ))
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    GEMINI_URL + geminiApiKey,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            System.out.println("[AGENTE-DEBUG] Respuesta Gemini: " + response.getBody());

            JsonNode json = objectMapper.readTree(response.getBody());
            return json
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText("Sin respuesta del modelo.");

        } catch (Exception e) {
            return "Error al conectar con Gemini: " + e.getMessage();
        }
    }
}