package com.jorge.examenes.services.impl;

import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.repository.PreguntaRepository;
import com.jorge.examenes.services.PreguntaService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio de gestión de Preguntas.
 * <p>
 * Encapsula la lógica para operar sobre el repositorio de preguntas,
 * asegurando validaciones básicas antes de interactuar con la base de datos.
 * </p>
 */
@Service
public class PreguntaServiceImpl implements PreguntaService {

    private final PreguntaRepository preguntaRepository;

    /**
     * Constructor que inyecta la dependencia del repositorio de preguntas.
     *
     * @param preguntaRepository Repositorio de Spring Data JPA para la entidad {@link Pregunta}.
     */
    public PreguntaServiceImpl(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }

    /**
     * Recupera la lista completa de todas las preguntas almacenadas en el sistema.
     *
     * @return Lista de entidades {@link Pregunta}.
     */
    @Override
    public List<Pregunta> obtenerTodas() {
        return preguntaRepository.findAll();
    }

    /**
     * Recupera una pregunta garantizando su existencia en la base de datos.
     *
     * @param id Identificador único de la pregunta a buscar.
     * @return La entidad {@link Pregunta} encontrada.
     * @throws NotFoundException Si el ID proporcionado no corresponde a ninguna pregunta registrada.
     */
    @Override
    public Pregunta obtenerPorId(Long id) {
        return preguntaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pregunta no encontrada con ID: " + id));
    }

    /**
     * Guarda una nueva pregunta en el repositorio de base de datos.
     *
     * @param pregunta Entidad {@link Pregunta} con los datos a persistir.
     * @return La entidad {@link Pregunta} persistida, incluyendo el ID autogenerado.
     */
    @Override
    public Pregunta guardarPregunta(Pregunta pregunta) {
        return preguntaRepository.save(pregunta);
    }

    /**
     * Sobrescribe los atributos de una pregunta existente con nuevos valores.
     *
     * @param id Identificador de la pregunta objetivo que se desea modificar.
     * @param preguntaActualizada Objeto {@link Pregunta} con los datos actualizados (enunciado, opciones y respuesta correcta).
     * @return La entidad {@link Pregunta} tras aplicar y guardar los cambios.
     * @throws NotFoundException Si la pregunta a actualizar no existe en la base de datos.
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
     * @return Mensaje de texto confirmando la eliminación exitosa.
     * @throws NotFoundException Si la pregunta que se intenta borrar no existe.
     */
    @Override
    public String borrarPregunta(Long id) {
        Pregunta pregunta = obtenerPorId(id);
        preguntaRepository.delete(pregunta);
        return ("Pregunta eliminada correctamente.");
    }

    /**
     * Recupera una lista paginada de preguntas, permitiendo ordenación dinámica
     * para facilitar la visualización y gestión en el frontend.
     *
     * @param page Número de la página a consultar (comienza en 0).
     * @param size Cantidad de preguntas por página.
     * @param sortBy Campo por el cual se ordenarán los resultados (ej: "id", "enunciado").
     * @param sortDir Dirección de la ordenación ("asc" para ascendente, "desc" para descendente).
     * @return Un objeto {@link org.springframework.data.domain.Page} que contiene las preguntas de la página solicitada.
     */
    @Override
    public org.springframework.data.domain.Page<Pregunta> obtenerPreguntasPaginadas(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return preguntaRepository.findAll(pageable);
    }
}