package com.jorge.examenes.repository;

import com.jorge.examenes.entity.Evaluacion;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluacionRepository  extends JpaRepository<Evaluacion, Long> {

        List<Evaluacion> findByIdExamen(Long idExamen);
        List<Evaluacion> findByCorreoUsuarioOrderByFechaDesc(String correoUsuario);
        int countByIdExamenAndCorreoUsuario(Long idExamen, String correoUsuario);
        List<Evaluacion> findByIdExamenAndCorreoUsuarioOrderByFechaDesc(Long idExamen, String correoUsuario);

        List<Evaluacion> findByIdExamen(Long idExamen, Sort sort);

        List<Evaluacion> findByCorreoUsuario(String correoUsuario, Sort sort);
    }

