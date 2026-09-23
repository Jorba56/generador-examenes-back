package com.jorge.examenes.services;

import com.jorge.examenes.dto.ExamenDetalleDTO;
import com.jorge.examenes.dto.ExamenGetDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interfaz que define el contrato para el servicio de gestión de Exámenes.
 * <p>
 * Establece las operaciones disponibles para consultar, crear, modificar y eliminar
 * exámenes dentro del sistema, actuando como la capa de abstracción entre los controladores
 * y la lógica de acceso a datos.
 * </p>
 */
public interface ExamenService {

    /**
     * Obtiene una lista con la información resumida de todos los exámenes registrados.
     *
     * @return Lista de {@link ExamenGetDTO} con el resumen de cada examen.
     */
    List<ExamenGetDTO> obtenerTodosResumen();

    /**
     * Recupera los detalles completos de un examen específico.
     * Las preguntas se devuelven numeradas y con las respuestas correctas censuradas (si aplica).
     *
     * @param id Identificador único del examen a consultar.
     * @return {@link ExamenDetalleDTO} con la información detallada del examen.
     */
    ExamenDetalleDTO obtenerDetallePorId(Long id);

    /**
     * Genera un examen de forma aleatoria seleccionando un número específico de preguntas
     * del banco general disponible en la base de datos.
     *
     * @param titulo Título que se le asignará al nuevo examen.
     * @param descripcion Breve texto descriptivo sobre el contenido del examen.
     * @param numPreguntas Cantidad de preguntas aleatorias que debe contener.
     * @return {@link ExamenDetalleDTO} del examen recién creado y persistido.
     */
    ExamenDetalleDTO generarExamenAleatorio(String titulo, String descripcion, int numPreguntas);

    /**
     * Actualiza la lista completa de preguntas asociadas a un examen existente,
     * reemplazando la lista anterior por la nueva.
     *
     * @param idExamen Identificador del examen que se va a modificar.
     * @param idsNuevasPreguntas Lista con los nuevos IDs de las preguntas a asociar.
     * @return {@link ExamenDetalleDTO} con los detalles actualizados del examen.
     */
    ExamenDetalleDTO actualizarPreguntasDeExamen(Long idExamen, List<Long> idsNuevasPreguntas);

    /**
     * Modifica los datos básicos de un examen existente (título y descripción) sin alterar sus preguntas.
     *
     * @param id Identificador único del examen a modificar.
     * @param titulo Nuevo título del examen.
     * @param descripcion Nueva descripción del examen.
     * @return {@link ExamenDetalleDTO} con la información actualizada.
     */
    ExamenDetalleDTO actualizarDetallesExamen(Long id, String titulo, String descripcion);

    /**
     * Elimina un examen de la base de datos de manera definitiva.
     *
     * @param id Identificador del examen que se desea borrar.
     */
    void borrarExamen(Long id);

    /**
     * Añade un conjunto de preguntas adicionales a un examen existente, manteniendo
     * las preguntas previas y evitando posibles duplicados.
     *
     * @param idExamen Identificador del examen al que se le añadirán las preguntas.
     * @param idsPreguntasNuevas Lista de los identificadores de las preguntas a añadir.
     * @return {@link ExamenDetalleDTO} reflejando el examen con las nuevas preguntas incluidas.
     */
    ExamenDetalleDTO anadirPreguntas(Long idExamen, List<Long> idsPreguntasNuevas);

    /**
     * Recupera una lista paginada de todos los exámenes, permitiendo ordenación dinámica.
     *
     * @param page Número de página a consultar (comienza en 0).
     * @param size Cantidad de resultados por página.
     * @param sortBy Campo de ordenación ("titulo", "fecha", etc.).
     * @param sortDir Dirección de la ordenación ("asc" o "desc").
     * @return Objeto {@link Page} que contiene una lista de {@link ExamenGetDTO}.
     */
    Page<ExamenGetDTO> obtenerExamenesPaginados(int page, int size, String sortBy, String sortDir);
}