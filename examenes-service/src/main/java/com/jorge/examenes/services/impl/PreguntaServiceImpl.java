package com.jorge.examenes.services.impl;

import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.repository.PreguntaRepository;
import com.jorge.examenes.services.PreguntaService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio de gestión de Preguntas.
 * Encapsula la lógica para operar sobre el repositorio de preguntas,
 * asegurando validaciones básicas antes de interactuar con la base de datos.
 */
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

    /**
     * Recupera una pregunta garantizando su existencia en la base de datos.
     *
     * @param id Identificador de la pregunta.
     * @return La entidad Pregunta encontrada.
     * @throws NotFoundException Si el ID no corresponde a ninguna pregunta registrada.
     */
    @Override
    public Pregunta obtenerPorId(Long id) {
        return preguntaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pregunta no encontrada con ID: " + id));
    }

    /**
     * Guarda una nueva pregunta en el repositorio.
     *
     * @param pregunta Entidad de la pregunta a guardar.
     * @return Entidad persistida con su ID generado.
     */
    @Override
    public Pregunta guardarPregunta(Pregunta pregunta) {
        return preguntaRepository.save(pregunta);
    }

    /**
     * Sobrescribe los atributos de una pregunta existente con nuevos valores.
     *
     * @param id Identificador de la pregunta objetivo.
     * @param preguntaActualizada Datos actualizados (texto, opciones y respuesta correcta).
     * @return La entidad Pregunta tras aplicar los cambios.
     * @throws NotFoundException Si la pregunta a actualizar no existe.
     */
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

    /**
     * Elimina permanentemente una pregunta del banco de datos.
     *
     * @param id Identificador de la pregunta a borrar.
     * @return Mensaje de confirmación de borrado.
     */
    @Override
    public String borrarPregunta(Long id) {
        Pregunta pregunta = obtenerPorId(id);
        preguntaRepository.delete(pregunta);
        return ("Pregunta eliminada correctamente.");
    }
}