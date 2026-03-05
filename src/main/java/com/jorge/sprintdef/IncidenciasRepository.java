package com.jorge.sprintdef;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface IncidenciasRepository extends JpaRepository<Incidencia, Long> {
    List<Incidencia> findByClase(String clase);
    List<Incidencia> findByIdUsuario(Long id);
    List<Incidencia> findByMetodo(String metodo);
}

