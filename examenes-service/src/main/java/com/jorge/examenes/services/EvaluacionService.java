package com.jorge.examenes.services;

import com.jorge.examenes.dto.EstadisticasAlumnoDTO;
import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import com.jorge.examenes.dto.EvaluacionResultDTO;
import com.jorge.examenes.dto.ExamenSubmitDTO;
import com.jorge.examenes.exceptions.BadRequestException;

import java.util.List;

/**
 * Interfaz que define el contrato para el servicio de Evaluaciones.
 * <p>
 * Establece las operaciones permitidas para la corrección automática de exámenes,
 * la consulta de historiales de calificaciones y el cálculo de estadísticas de los alumnos.
 * </p>
 */
public interface EvaluacionService {

    /**
     * Procesa y corrige la entrega de un examen comparando las respuestas del alumno.
     *
     * @param idExamen Identificador único del examen a corregir.
     * @param submitDTO Objeto {@link ExamenSubmitDTO} que contiene el mapa de respuestas enviadas.
     * @return {@link EvaluacionResultDTO} con el desglose de la corrección (aciertos, fallos, en blanco y nota final).
     * @throws BadRequestException Si el usuario ha superado el límite de intentos o hay problemas con su autenticación.
     */
    EvaluacionResultDTO corregirExamen(Long idExamen, ExamenSubmitDTO submitDTO) throws BadRequestException;

    /**
     * Recupera el historial completo de calificaciones del usuario actualmente autenticado.
     *
     * @return Lista de {@link EvaluacionHistorialDTO} con las evaluaciones del alumno logueado.
     * @throws BadRequestException Si no existe un contexto de seguridad válido (usuario no logueado).
     */
    List<EvaluacionHistorialDTO> obtenerMisNotas() throws BadRequestException;

    /**
     * Obtiene los intentos y calificaciones de un alumno específico para un examen concreto.
     *
     * @param idExamen Identificador del examen a consultar.
     * @param correoAlumno Correo electrónico del alumno cuyas notas se desean buscar.
     * @return Lista de {@link EvaluacionHistorialDTO} con el historial de ese examen.
     */
    List<EvaluacionHistorialDTO> obtenerNotasDeAlumnoEnExamen(Long idExamen, String correoAlumno);

    /**
     * Calcula las métricas de rendimiento de un alumno en base a todo su historial.
     *
     * @param correo Correo electrónico del alumno a analizar.
     * @return {@link EstadisticasAlumnoDTO} que contiene la nota media, total de exámenes, aprobados y suspensos.
     */
    EstadisticasAlumnoDTO obtenerEstadisticasAlumno(String correo);

    /**
     * Recupera el historial de evaluaciones de un alumno permitiendo ordenación dinámica.
     *
     * @param correo Correo electrónico del alumno.
     * @param sortBy Campo por el cual ordenar los resultados (ej: "fecha", "nota").
     * @param sortDir Dirección de la ordenación ("asc" o "desc").
     * @return Lista de {@link EvaluacionHistorialDTO} ordenada según los parámetros.
     */
    List<EvaluacionHistorialDTO> obtenerHistorialAlumno(String correo, String sortBy, String sortDir);

    /**
     * Obtiene el listado de todas las notas obtenidas por los alumnos en un examen concreto (Ranking).
     *
     * @param idExamen Identificador del examen.
     * @param sortBy Campo por el cual ordenar los resultados (ej: "nota" para hacer un ranking).
     * @param sortDir Dirección de la ordenación ("asc" o "desc").
     * @return Lista de {@link EvaluacionHistorialDTO} con las calificaciones ordenadas.
     */
    List<EvaluacionHistorialDTO> obtenerNotasExamen(Long idExamen, String sortBy, String sortDir);

    /**
     * Método auxiliar que traduce los parámetros de ordenación recibidos de la URL
     * a los nombres reales de las columnas en la base de datos (Entidad).
     *
     * @param sortBy Valor del parámetro de ordenación recibido en la petición HTTP.
     * @return Nombre exacto del atributo correspondiente en la entidad Evaluacion.
     */
    default String traducirCampoSort(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "nota" -> "nota";
            case "fecha" -> "fecha";
            case "correo" -> "correoUsuario";
            default -> "id"; // ordenacion por id por defecto
        };
    }
}