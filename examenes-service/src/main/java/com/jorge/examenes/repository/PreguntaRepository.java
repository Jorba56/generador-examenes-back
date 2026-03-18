package com.jorge.examenes.repository;

import com.jorge.examenes.entity.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    // mysql ordenará aleatoriamente y cogerá solo el límite que le pasemos
    @Query(value = "SELECT * FROM preguntas ORDER BY RAND() LIMIT :cantidad", nativeQuery = true)
    List<Pregunta> findPreguntasAleatorias(@Param("cantidad") int cantidad);
}