package com.jorge.examenes.services;

import com.jorge.examenes.dto.EstadisticasAlumnoDTO;
import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import com.jorge.examenes.dto.EvaluacionResultDTO;
import com.jorge.examenes.dto.ExamenSubmitDTO;
import com.jorge.examenes.exceptions.BadRequestException;

import java.util.List;

public interface EvaluacionService {
    EvaluacionResultDTO corregirExamen(Long idExamen, ExamenSubmitDTO submitDTO) throws BadRequestException;

    List<EvaluacionHistorialDTO> obtenerMisNotas() throws BadRequestException;

    List<EvaluacionHistorialDTO> obtenerNotasDeAlumnoEnExamen(Long idExamen, String correoAlumno);

    EstadisticasAlumnoDTO obtenerEstadisticasAlumno(String correo);

    List<EvaluacionHistorialDTO> obtenerHistorialAlumno(String correo, String sortBy, String sortDir);

    List<EvaluacionHistorialDTO> obtenerNotasExamen(Long idExamen, String sortBy, String sortDir);

    /**
     * Método auxiliar privado que traduce los parámetros de ordenación de la URL
     * a los nombres reales de las columnas en la base de datos (Entidad).
     *
     * @param sortBy Valor del parámetro de ordenación recibido en la petición.
     * @return Nombre del atributo correspondiente en la entidad Evaluacion.
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
