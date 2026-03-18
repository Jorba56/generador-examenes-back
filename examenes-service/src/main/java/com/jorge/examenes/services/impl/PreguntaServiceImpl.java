package com.jorge.examenes.services.impl;

import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.repository.PreguntaRepository;
import com.jorge.examenes.services.PreguntaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreguntaServiceImpl implements PreguntaService {

    private final PreguntaRepository preguntaRepository;

    public PreguntaServiceImpl(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }

    @Override
    public List<Pregunta> obtenerTodas() {
        return preguntaRepository.findAll();
    }

    @Override
    public Pregunta obtenerPorId(Long id) {
        return preguntaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pregunta no encontrada con ID: " + id));
    }

    @Override
    public Pregunta guardarPregunta(Pregunta pregunta) {
        return preguntaRepository.save(pregunta);
    }

    @Override
    public Pregunta actualizarPregunta(Long id, Pregunta preguntaActualizada) {
        Pregunta preguntaExistente = obtenerPorId(id);

        preguntaExistente.setEnunciado(preguntaActualizada.getEnunciado());
        preguntaExistente.setOpcionA(preguntaActualizada.getOpcionA());
        preguntaExistente.setOpcionB(preguntaActualizada.getOpcionB());
        preguntaExistente.setOpcionC(preguntaActualizada.getOpcionC());
        preguntaExistente.setOpcionD(preguntaActualizada.getOpcionD());
        preguntaExistente.setCorrecta(preguntaActualizada.getCorrecta());

        return preguntaRepository.save(preguntaExistente);
    }

    @Override
    public void borrarPregunta(Long id) {
        Pregunta pregunta = obtenerPorId(id);
        preguntaRepository.delete(pregunta);
    }
}