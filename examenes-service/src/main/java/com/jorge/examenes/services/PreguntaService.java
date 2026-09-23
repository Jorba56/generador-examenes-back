package com.jorge.examenes.services;

import com.jorge.examenes.entity.Pregunta;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interfaz que define el contrato para el servicio de gestión de Preguntas.
 * <p>
 * Establece las operaciones permitidas sobre el banco de preguntas del sistema,
 * incluyendo operaciones CRUD completas y consultas paginadas para el frontend.
 * </p>
 */
public interface PreguntaService {

    /**
     * Recupera la lista completa de todas las preguntas almacenadas en el sistema.
     *
     * @return Lista de entidades {@link Pregunta}.
     */
    List<Pregunta> obtenerTodas();

    /**
     * Recupera una pregunta garantizando su existencia en la base de datos.
     *
     * @param id Identificador único de la pregunta a buscar.
     * @return La entidad {@link Pregunta} encontrada.
     */
    Pregunta obtenerPorId(Long id);

    /**
     * Guarda una nueva pregunta en el repositorio de base de datos.
     *
     * @param pregunta Entidad {@link Pregunta} con los datos a persistir.
     * @return La entidad {@link Pregunta} persistida, incluyendo su ID autogenerado.
     */
    Pregunta guardarPregunta(Pregunta pregunta);

    /**
     * Sobrescribe los atributos de una pregunta existente con nuevos valores.
     *
     * @param id Identificador de la pregunta objetivo que se desea modificar.
     * @param preguntaActualizada Objeto {@link Pregunta} con los datos actualizados.
     * @return La entidad {@link Pregunta} tras aplicar y guardar los cambios.
     */
    Pregunta actualizarPregunta(Long id, Pregunta preguntaActualizada);

    /**
     * Elimina permanentemente una pregunta del banco de datos.
     *
     * @param id Identificador de la pregunta a borrar.
     * @return Mensaje de confirmación de borrado.
     */
    String borrarPregunta(Long id);

    /**
     * Recupera una lista paginada de preguntas, permitiendo ordenación dinámica.
     *
     * @param page Número de la página a consultar (comienza en 0).
     * @param size Cantidad de preguntas por página.
     * @param sortBy Campo por el cual se ordenarán los resultados (ej: "id", "enunciado").
     * @param sortDir Dirección de la ordenación ("asc" o "desc").
     * @return Un objeto {@link Page} que contiene las preguntas de la página solicitada.
     */
    Page<Pregunta> obtenerPreguntasPaginadas(int page, int size, String sortBy, String sortDir);
}