package com.jorge.examenes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * Objeto de Transferencia de Datos (DTO) utilizado para recibir desde el cliente
 * las respuestas seleccionadas por un alumno al finalizar un examen.
 * Actúa como envoltorio para el mapa de respuestas.
 */
@Schema(description = "Objeto que envía el alumno al terminar el examen con todas sus respuestas.")
public class ExamenSubmitDTO {

    @Schema(description = "Mapa de respuestas. La clave (izquierda) es el ID real de la pregunta, y el valor (derecha) es la letra de la opción marcada.",
            example = "{\"1\": \"A\", \"2\": \"C\", \"3\": \"B\", \"4\": \"A\"}")
    private Map<Integer, String> respuestas;

    public ExamenSubmitDTO() { /*this constructor is empty */ }

    public Map<Integer, String> getRespuestas() { return respuestas; }
    public void setRespuestas(Map<Integer, String> respuestas) { this.respuestas = respuestas; }
}