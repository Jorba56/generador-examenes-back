package com.jorge.examenes.services.impl;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        // 1. Extraemos el objeto de autenticación de forma segura

        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Paracaídas para SonarQube: Comprobamos que no sea nulo antes de sacar el nombre
        if (authentication == null) {
            throw new BadRequestException("El contexto de seguridad está vacío. No se puede identificar al usuario.");
        }

        // 3. Magia segura: Extraemos el correo
        String correoUsuarioLogueado = authentication.getName();
        // Guardamos la evaluación
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setCorreoUsuario(correoUsuarioLogueado);
        evaluacion.setIdExamen(examen.getId());
        evaluacion.setNota(notaFinal);
        evaluacion.setFecha(LocalDateTime.now());

        evaluacionRepository.save(evaluacion);

        return new EvaluacionResultDTO(aciertos, fallos, enBlanco, notaFinal);
    }

    public List<EvaluacionHistorialDTO> obtenerMisNotas() throws BadRequestException {
        // extraemos el usuario del token (igual que hicimos al corregir)
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new BadRequestException("No hay un usuario logueado en el sistema.");
        }

        String correoUsuario = authentication.getName();

        // buscamos sus evaluaciones en la base de datos
        List<Evaluacion> misEvaluaciones = evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(correoUsuario);


        return evaluacionMapper.toHistorialDTOList(misEvaluaciones);
    }
}