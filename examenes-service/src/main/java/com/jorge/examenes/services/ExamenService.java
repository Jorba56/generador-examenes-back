package com.jorge.examenes.services;

import com.jorge.examenes.entity.Examen;

/**
 * Interfaz que define las operaciones principales para la gestión de exámenes.
 */
public interface ExamenService {

    /**
     * Genera automáticamente un nuevo examen seleccionando un número específico
     * de preguntas al azar desde la base de datos.
     *
     * @param titulo Título descriptivo del examen.
     * @param descripcion Descripción o instrucciones del examen.
     * @param numPreguntas Cantidad de preguntas aleatorias que contendrá el examen.
     * @return El examen generado y persistido en la base de datos.
     */
    Examen generarExamenAleatorio(String titulo, String descripcion, int numPreguntas);

}