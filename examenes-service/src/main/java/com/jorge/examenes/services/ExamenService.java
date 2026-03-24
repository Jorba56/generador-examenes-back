package com.jorge.examenes.services;

import com.jorge.examenes.dto.ExamenDetalleDTO;
import com.jorge.examenes.dto.ExamenGetDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ExamenService {
    // listar todos (resumen)
    List<ExamenGetDTO> obtenerTodosResumen();

    // detalle por id (numerado y censurado)
    ExamenDetalleDTO obtenerDetallePorId(Long id);

    // generar aleatorio
    ExamenDetalleDTO generarExamenAleatorio(String titulo, String descripcion, int numPreguntas);

    ExamenDetalleDTO actualizarPreguntasDeExamen(Long idExamen, List<Long> idsNuevasPreguntas);

    ExamenDetalleDTO actualizarDetallesExamen(Long id, String titulo, String descripcion);

    void borrarExamen(Long id);

    ExamenDetalleDTO anadirPreguntas(Long idExamen, List<Long> idsPreguntasNuevas);

    Page<ExamenGetDTO> obtenerExamenesPaginados(int page, int size, String sortBy, String sortDir);
}
