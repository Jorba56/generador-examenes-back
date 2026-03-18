package com.jorge.examenes.repository;

import com.jorge.examenes.entity.Examen;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de Spring Data JPA encargado de la persistencia de la entidad {@link Examen}.
 * Proporciona operaciones CRUD estándar y consultas derivadas personalizadas para filtrar
 * el historial de auditoría de errores del sistema.
 */
@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {

}

