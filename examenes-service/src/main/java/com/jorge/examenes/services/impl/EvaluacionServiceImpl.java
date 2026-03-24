package com.jorge.examenes.services.impl;

import com.jorge.examenes.dto.EstadisticasAlumnoDTO;
import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import com.jorge.examenes.dto.EvaluacionResultDTO;
import com.jorge.examenes.dto.ExamenSubmitDTO;
import com.jorge.examenes.entity.Evaluacion;
import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.exceptions.BadRequestException;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.mapping.EvaluacionMapper;
import com.jorge.examenes.repository.EvaluacionRepository;
import com.jorge.examenes.repository.ExamenRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvaluacionServiceImpl {

    private final EvaluacionRepository evaluacionRepository;
    private final ExamenRepository examenRepository;
    private final EvaluacionMapper evaluacionMapper;

    public EvaluacionServiceImpl(EvaluacionRepository evaluacionRepository, ExamenRepository examenRepository, EvaluacionMapper evaluacionMapper) {
        this.evaluacionRepository = evaluacionRepository;
        this.examenRepository = examenRepository;
        this.evaluacionMapper = evaluacionMapper;
    }

    public EvaluacionResultDTO corregirExamen(Long idExamen, ExamenSubmitDTO submitDTO) throws BadRequestException {
        Examen examen = examenRepository.findById(idExamen)
                .orElseThrow(() -> new NotFoundException("Examen no encontrado con ID: " + idExamen));

        int aciertos = 0;
        int fallos = 0;
        int enBlanco = 0;

        Map<Integer, String> respuestasAlumno = submitDTO.getRespuestas();
        if (respuestasAlumno == null) {
            respuestasAlumno = new HashMap<>(); // si es null, creamos un mapa vacío para que cuente como e blanco
        }
        int numeroPregunta = 1;

        for (Pregunta preguntaReal : examen.getPreguntas()) {
            String respuestaDada = respuestasAlumno.get(numeroPregunta);

            if (respuestaDada == null || respuestaDada.trim().isEmpty()) {
                enBlanco++;
            } else if (respuestaDada.equalsIgnoreCase(preguntaReal.getCorrecta())) {
                aciertos++;
            } else {
                fallos++;
            }
            numeroPregunta++;
        }

        int totalPreguntas = examen.getPreguntas().size();

        double notaFinal = totalPreguntas > 0 ? ((double) aciertos / totalPreguntas) * 10.0 : 0.0;
        notaFinal = Math.round(notaFinal * 100.0) / 100.0;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // comprobamos que no sea nulo antes de sacar el nombre y asi sonar no salta
        if (authentication == null) {
            throw new BadRequestException("El contexto de seguridad está vacío. No se puede identificar al usuario.");
        }

        // extraemos el correo
        String correoUsuarioLogueado = authentication.getName();
        int intentosPrevios = evaluacionRepository.countByIdExamenAndCorreoUsuario(idExamen, correoUsuarioLogueado);
        if (intentosPrevios >= 2) {
            throw new BadRequestException("Has alcanzado el número máximo de intentos (2) para este examen.");
        }
        // guardamos la evaluación
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setCorreoUsuario(correoUsuarioLogueado);
        evaluacion.setIdExamen(examen.getId());
        evaluacion.setNota(notaFinal);
        evaluacion.setFecha(LocalDateTime.now());

        evaluacionRepository.save(evaluacion);

        return new EvaluacionResultDTO(aciertos, fallos, enBlanco, notaFinal);
    }

    public List<EvaluacionHistorialDTO> obtenerMisNotas() throws BadRequestException {
        // extraemos el usuario del token (igual que al corregir)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new BadRequestException("No hay un usuario logueado en el sistema.");
        }

        String correoUsuario = authentication.getName();

        // buscamos sus evaluaciones en la base de datos
        List<Evaluacion> misEvaluaciones = evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(correoUsuario);

        return evaluacionMapper.toHistorialDTOList(misEvaluaciones);
    }

    public List<EvaluacionHistorialDTO> obtenerNotasDeAlumnoEnExamen(Long idExamen, String correoAlumno) {

        List<Evaluacion> evaluaciones = evaluacionRepository.findByIdExamenAndCorreoUsuarioOrderByFechaDesc(idExamen, correoAlumno);

        return evaluacionMapper.toHistorialDTOList(evaluaciones);
    }

    public EstadisticasAlumnoDTO obtenerEstadisticasAlumno(String correo) {

        List<Evaluacion> evaluaciones = evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(correo);

        // si el alumno no ha hecho ningún examen, devolvemos ceros
        if (evaluaciones == null || evaluaciones.isEmpty()) {
            throw new NotFoundException("El alumno no ha realizado ningún examen aún.");
        }

        int totalExamenes = evaluaciones.size();
        int aprobados = 0;
        double sumaNotas = 0.0;

        for (Evaluacion eval : evaluaciones) {
            sumaNotas += eval.getNota();
            if (eval.getNota() >= 5.0) {
                aprobados++;
            }
        }

        // calculamos la media y la redondeamos a 2 decimales
        double media = sumaNotas / totalExamenes;
        media = Math.round(media * 100.0) / 100.0;

        int suspensos = totalExamenes - aprobados;

        return new EstadisticasAlumnoDTO(
                correo,
                totalExamenes,
                media,
                aprobados,
                suspensos
        );
    }

    public List<EvaluacionHistorialDTO> obtenerHistorialAlumno(String correo, String sortBy, String sortDir) {
        String campoEntidad = traducirCampoSort(sortBy);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(campoEntidad).ascending()
                : Sort.by(campoEntidad).descending();

        // llamamos al nuevo métod del repositorio pasándole el sort
        List<Evaluacion> evaluaciones = evaluacionRepository.findByCorreoUsuario(correo, sort);
        return evaluacionMapper.toHistorialDTOList(evaluaciones);
    }

    public List<EvaluacionHistorialDTO> obtenerNotasExamen(Long idExamen, String sortBy, String sortDir) {
        String campoEntidad = traducirCampoSort(sortBy);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(campoEntidad).ascending()
                : Sort.by(campoEntidad).descending();

        List<Evaluacion> evaluaciones = evaluacionRepository.findByIdExamen(idExamen, sort);
        return evaluacionMapper.toHistorialDTOList(evaluaciones);
    }

    // traductor de columnas de la url a entidad
    private String traducirCampoSort(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "nota" -> "nota";
            case "fecha" -> "fecha";
            case "correo" -> "correoUsuario";
            default -> "id"; // ordenacion por id por defecto
        };
    }
}