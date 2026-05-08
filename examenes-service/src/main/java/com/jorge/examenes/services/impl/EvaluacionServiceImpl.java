package com.jorge.examenes.services.impl;

import com.jorge.examenes.dto.EstadisticasAlumnoDTO;
import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import com.jorge.examenes.dto.EvaluacionResultDTO;
import com.jorge.examenes.dto.ExamenSubmitDTO;
import com.jorge.examenes.entity.Evaluacion;
import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.entity.RespuestaUsuario;
import com.jorge.examenes.exceptions.BadRequestException;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.mapping.EvaluacionMapper;
import com.jorge.examenes.repository.EvaluacionRepository;
import com.jorge.examenes.repository.ExamenRepository;
import com.jorge.examenes.repository.RespuestaUsuarioRepository;
import com.jorge.examenes.services.EvaluacionService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de evaluaciones.
 */
@Service
public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final ExamenRepository examenRepository;
    private final EvaluacionMapper evaluacionMapper;
    // 1. INYECTAMOS EL REPOSITORIO DE RESPUESTAS
    private final RespuestaUsuarioRepository respuestaUsuarioRepository;

    public EvaluacionServiceImpl(EvaluacionRepository evaluacionRepository,
                                 ExamenRepository examenRepository,
                                 EvaluacionMapper evaluacionMapper,
                                 RespuestaUsuarioRepository respuestaUsuarioRepository) {
        this.evaluacionRepository = evaluacionRepository;
        this.examenRepository = examenRepository;
        this.evaluacionMapper = evaluacionMapper;
        this.respuestaUsuarioRepository = respuestaUsuarioRepository;
    }

    @Override
    @Transactional // AÑADIMOS TRANSACTIONAL PARA ASEGURAR QUE SE GUARDA TODO JUNTO
    public EvaluacionResultDTO corregirExamen(Long idExamen, ExamenSubmitDTO submitDTO) throws BadRequestException {
        Examen examen = examenRepository.findById(idExamen)
                .orElseThrow(() -> new NotFoundException("Examen no encontrado con ID: " + idExamen));

        int aciertos = 0;
        int fallos = 0;
        int enBlanco = 0;

        Map<Integer, String> respuestasAlumno = submitDTO.getRespuestas();
        if (respuestasAlumno == null) {
            respuestasAlumno = new HashMap<>();
        }

        // 1. Bucle para calcular aciertos y fallos
        for (Pregunta preguntaReal : examen.getPreguntas()) {
            // ¡CORRECCIÓN!: Usamos el ID real de la pregunta en la BBDD, no 1, 2, 3...
            String respuestaDada = respuestasAlumno.get(preguntaReal.getId().intValue());

            if (respuestaDada == null || respuestaDada.trim().isEmpty()) {
                enBlanco++;
            } else if (respuestaDada.equalsIgnoreCase(preguntaReal.getCorrecta())) {
                aciertos++;
            } else {
                fallos++;
            }
        }

        int totalPreguntas = examen.getPreguntas().size();
        double notaFinal = totalPreguntas > 0 ? ((double) aciertos / totalPreguntas) * 10.0 : 0.0;
        notaFinal = Math.round(notaFinal * 100.0) / 100.0;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new BadRequestException("El contexto de seguridad está vacío. No se puede identificar al usuario.");
        }

        String correoUsuarioLogueado = authentication.getName();
        int intentosPrevios = evaluacionRepository.countByIdExamenAndCorreoUsuario(idExamen, correoUsuarioLogueado);
        if (intentosPrevios >= 2) {
            throw new BadRequestException("Has alcanzado el número máximo de intentos (2) para este examen.");
        }

        // 2. GUARDAMOS LA EVALUACIÓN
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setCorreoUsuario(correoUsuarioLogueado);
        evaluacion.setIdExamen(examen.getId());
        evaluacion.setNota(notaFinal);
        evaluacion.setFecha(LocalDateTime.now());

        // Reasignamos para capturar el ID de la base de datos
        evaluacion = evaluacionRepository.save(evaluacion);

        // 3. GUARDAMOS CADA RESPUESTA DEL ALUMNO EN LA NUEVA TABLA
        for (Pregunta preguntaReal : examen.getPreguntas()) {
            // ¡CORRECCIÓN!: Usamos el ID real aquí también
            String respuestaDada = respuestasAlumno.get(preguntaReal.getId().intValue());

            RespuestaUsuario ru = new RespuestaUsuario();
            ru.setIdEvaluacion(evaluacion.getId());
            ru.setIdPregunta(preguntaReal.getId());

            if (respuestaDada == null || respuestaDada.trim().isEmpty()) {
                ru.setRespuestaMarcada("-");
            } else {
                ru.setRespuestaMarcada(respuestaDada.toUpperCase());
            }

            respuestaUsuarioRepository.save(ru);
        }

        return new EvaluacionResultDTO(aciertos, fallos, enBlanco, notaFinal);
    }

    @Override
    public List<EvaluacionHistorialDTO> obtenerMisNotas() throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new BadRequestException("No hay un usuario logueado en el sistema.");
        }

        String correoUsuario = authentication.getName();

        List<Evaluacion> misEvaluaciones = evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(correoUsuario);
        if (misEvaluaciones.isEmpty()){
            throw new NotFoundException("No has realizado ningún examen aún.");
        }
        return evaluacionMapper.toHistorialDTOList(misEvaluaciones);
    }

    @Override
    public List<EvaluacionHistorialDTO> obtenerNotasDeAlumnoEnExamen(Long idExamen, String correoAlumno) {
        List<Evaluacion> evaluaciones = evaluacionRepository.findByIdExamenAndCorreoUsuarioOrderByFechaDesc(idExamen, correoAlumno);
        if (evaluaciones.isEmpty()){
            throw new NotFoundException("El usuario seleccionado no ha hecho este examen.");
        }
        return evaluacionMapper.toHistorialDTOList(evaluaciones);
    }

    @Override
    public EstadisticasAlumnoDTO obtenerEstadisticasAlumno(String correo) {
        List<Evaluacion> evaluaciones = evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(correo);

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

    @Override
    public List<EvaluacionHistorialDTO> obtenerHistorialAlumno(String correo, String sortBy, String sortDir) {
        String campoEntidad = traducirCampoSort(sortBy);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(campoEntidad).ascending()
                : Sort.by(campoEntidad).descending();

        List<Evaluacion> evaluaciones = evaluacionRepository.findByCorreoUsuario(correo, sort);
        if (evaluaciones.isEmpty()){
            throw new NotFoundException("El usuario seleccionado no ha hecho este examen.");
        }
        return evaluacionMapper.toHistorialDTOList(evaluaciones);
    }

    @Override
    public List<EvaluacionHistorialDTO> obtenerNotasExamen(Long idExamen, String sortBy, String sortDir) {
        String campoEntidad = traducirCampoSort(sortBy);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(campoEntidad).ascending()
                : Sort.by(campoEntidad).descending();

        List<Evaluacion> evaluaciones = evaluacionRepository.findByIdExamen(idExamen, sort);
        if (evaluaciones.isEmpty()){
            throw new NotFoundException("El examen seleccionado no ha sido realizado por ningún usuario aún.");
        }

        return evaluacionMapper.toHistorialDTOList(evaluaciones);
    }
}