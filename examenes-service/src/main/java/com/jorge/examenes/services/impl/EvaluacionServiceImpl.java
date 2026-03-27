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
import com.jorge.examenes.services.EvaluacionService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de evaluaciones.
 * <p>
 * Contiene la lógica de negocio central para la corrección algorítmica de exámenes,
 * el cálculo de estadísticas (medias, aprobados/suspensos) y la validación de intentos.
 * </p>
 */
@Service
public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final ExamenRepository examenRepository;
    private final EvaluacionMapper evaluacionMapper;

    /**
     * Constructor que inyecta las dependencias necesarias para la gestión de evaluaciones.
     *
     * @param evaluacionRepository Repositorio para la persistencia de las evaluaciones.
     * @param examenRepository Repositorio para consultar los datos y preguntas de los exámenes.
     * @param evaluacionMapper Mapper para convertir entre entidades de evaluación y sus respectivos DTOs.
     */
    public EvaluacionServiceImpl(EvaluacionRepository evaluacionRepository, ExamenRepository examenRepository, EvaluacionMapper evaluacionMapper) {
        this.evaluacionRepository = evaluacionRepository;
        this.examenRepository = examenRepository;
        this.evaluacionMapper = evaluacionMapper;
    }

    /**
     * Procesa la entrega de un examen comparando las respuestas dadas por el alumno
     * con las respuestas correctas de cada pregunta. Calcula la nota final sobre 10 y
     * guarda el intento en la base de datos si el alumno no ha superado el límite permitido.
     *
     * @param idExamen Identificador del examen a corregir.
     * @param submitDTO Objeto {@link ExamenSubmitDTO} que contiene las respuestas enviadas por el alumno.
     * @return {@link EvaluacionResultDTO} con el resultado detallado de la corrección (aciertos, fallos, blancos y nota final).
     * @throws BadRequestException Si el contexto de seguridad está vacío o si el alumno ya ha consumido sus 2 intentos.
     * @throws NotFoundException Si el examen solicitado no existe en la base de datos.
     */
    @Override
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

    /**
     * Recupera el historial completo de notas del usuario actualmente autenticado en el sistema.
     *
     * @return Lista de {@link EvaluacionHistorialDTO} ordenadas por fecha de forma descendente.
     * @throws BadRequestException Si no hay un usuario logueado válido en el contexto de seguridad.
     * @throws NotFoundException Si el usuario autenticado no ha realizado ningún examen todavía.
     */
    @Override
    public List<EvaluacionHistorialDTO> obtenerMisNotas() throws BadRequestException {
        // extraemos el usuario del token (igual que al corregir)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new BadRequestException("No hay un usuario logueado en el sistema.");
        }

        String correoUsuario = authentication.getName();

        // buscamos sus evaluaciones en la base de datos
        List<Evaluacion> misEvaluaciones = evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(correoUsuario);
        if (misEvaluaciones.isEmpty()){
            throw new NotFoundException("No has realizado ningún examen aún.");
        }
        return evaluacionMapper.toHistorialDTOList(misEvaluaciones);
    }

    /**
     * Obtiene el historial de intentos y calificaciones de un alumno específico para un examen concreto.
     *
     * @param idExamen Identificador del examen a consultar.
     * @param correoAlumno Correo electrónico del alumno cuyas notas se desean buscar.
     * @return Lista de {@link EvaluacionHistorialDTO} con las evaluaciones correspondientes, ordenadas por fecha descendente.
     * @throws NotFoundException Si el alumno especificado no ha realizado el examen indicado.
     */
    @Override
    public List<EvaluacionHistorialDTO> obtenerNotasDeAlumnoEnExamen(Long idExamen, String correoAlumno) {

        List<Evaluacion> evaluaciones = evaluacionRepository.findByIdExamenAndCorreoUsuarioOrderByFechaDesc(idExamen, correoAlumno);
        if (evaluaciones.isEmpty()){
            throw new NotFoundException("El usuario seleccionado no ha hecho este examen.");
        }
        return evaluacionMapper.toHistorialDTOList(evaluaciones);
    }

    /**
     * Calcula las estadísticas de rendimiento general de un alumno a partir de su historial completo.
     * <p>
     * Se evalúa el total de exámenes realizados, la nota media global y el número de exámenes aprobados y suspensos.
     * </p>
     *
     * @param correo Correo electrónico del alumno a analizar.
     * @return {@link EstadisticasAlumnoDTO} con las métricas calculadas.
     * @throws NotFoundException Si el alumno no tiene registros de exámenes en el sistema.
     */
    @Override
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

    /**
     * Recupera el historial completo de evaluaciones de un alumno específico,
     * aplicando ordenación dinámica según los parámetros indicados.
     *
     * @param correo Correo electrónico del alumno cuyo historial se desea consultar.
     * @param sortBy Campo de la entidad por el cual se ordenarán los resultados (ej: "fecha", "nota").
     * @param sortDir Dirección de la ordenación, "asc" para ascendente o "desc" para descendente.
     * @return Lista de {@link EvaluacionHistorialDTO} con el historial ordenado.
     * @throws NotFoundException Si el alumno no ha realizado ninguna evaluación.
     */
    @Override
    public List<EvaluacionHistorialDTO> obtenerHistorialAlumno(String correo, String sortBy, String sortDir) {
        String campoEntidad = traducirCampoSort(sortBy);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(campoEntidad).ascending()
                : Sort.by(campoEntidad).descending();

        // llamamos al nuevo métod del repositorio pasándole el sort
        List<Evaluacion> evaluaciones = evaluacionRepository.findByCorreoUsuario(correo, sort);
        if (evaluaciones.isEmpty()){
            throw new NotFoundException("El usuario seleccionado no ha hecho este examen.");
        }
        return evaluacionMapper.toHistorialDTOList(evaluaciones);
    }

    /**
     * Obtiene todas las evaluaciones registradas para un examen concreto,
     * aplicando criterios de ordenación dinámica (ideal para generar rankings).
     *
     * @param idExamen Identificador del examen del que se extraerán las notas.
     * @param sortBy Campo por el cual se ordenarán los resultados (ej: "nota").
     * @param sortDir Dirección de la ordenación ("asc" o "desc").
     * @return Lista de {@link EvaluacionHistorialDTO} con las calificaciones ordenadas de todos los participantes.
     * @throws NotFoundException Si el examen indicado no ha sido realizado por ningún usuario todavía.
     */
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