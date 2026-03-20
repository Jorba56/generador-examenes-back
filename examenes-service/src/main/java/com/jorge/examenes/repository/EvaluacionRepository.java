package com.jorge.examenes.repository;

import com.jorge.examenes.entity.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluacionRepository  extends JpaRepository<Evaluacion, Long> {
    /**
     * Devuelve el historial de exámenes de un alumno en concreto.
     * Ideal para hacer una pantalla de "Mis Notas".
     */
    List<Evaluacion> findByCorreoUsuario(String correoUsuario);

    /**
     * Devuelve todas las notas sacadas por todos los alumnos en un examen específico.
     * Ideal para el panel del profesor.
     */
    List<Evaluacion> findByIdExamen(Long idExamen);

    /**
     * Comprueba si un alumno ya ha realizado un examen concreto.
     * Muy útil para lanzar una excepción si intenta repetir un examen que ya entregó.
     */
    boolean existsByCorreoUsuarioAndIdExamen(String correoUsuario, Long idExamen);

    List<Evaluacion> findByCorreoUsuarioOrderByFechaDesc(String correoUsuario);
}
