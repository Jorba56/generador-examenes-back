package com.jorge.examenes.repository;

import com.jorge.examenes.entity.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluacionRepository  extends JpaRepository<Evaluacion, Long> {
    /**
     * Devuelve todas las notas sacadas por todos los alumnos en un examen específico.
     * Ideal para el panel del profesor.
     */
    List<Evaluacion> findByIdExamen(Long idExamen);

    /**
     * Devuelve el historial de exámenes de un alumno en concreto ordenados por fecha descendente.
     * Ideal para hacer una pantalla de "Mis Notas".
     */
    List<Evaluacion> findByCorreoUsuarioOrderByFechaDesc(String correoUsuario);

    // cuenta cuántas veces aparece un examen asociado a un correo concreto
    int countByIdExamenAndCorreoUsuario(Long idExamen, String correoUsuario);

    // busca los intentos de un alumno concreto en un examen específico
    List<Evaluacion> findByIdExamenAndCorreoUsuarioOrderByFechaDesc(Long idExamen, String correoUsuario);
}
