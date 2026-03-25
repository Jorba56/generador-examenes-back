package com.jorge.examenes.services;

import com.jorge.examenes.entity.Pregunta;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PreguntaService {
    List<Pregunta> obtenerTodas();

    Pregunta obtenerPorId(Long id);

    Pregunta guardarPregunta(Pregunta pregunta);

    Pregunta actualizarPregunta(Long id, Pregunta preguntaActualizada);

    String borrarPregunta(Long id);

    Page<Pregunta> obtenerPreguntasPaginadas(int page, int size, String sortBy, String sortDir);
}
