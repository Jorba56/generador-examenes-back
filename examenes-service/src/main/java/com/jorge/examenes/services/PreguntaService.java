package com.jorge.examenes.services;

import com.jorge.examenes.entity.Pregunta;

import java.util.List;

public interface PreguntaService {
    List<Pregunta> obtenerTodas();

    Pregunta obtenerPorId(Long id);

    Pregunta guardarPregunta(Pregunta pregunta);

    Pregunta actualizarPregunta(Long id, Pregunta preguntaActualizada);

    void borrarPregunta(Long id);
}
