package com.jorge.examenes.services.impl;

import com.jorge.examenes.dto.ExamenDetalleDTO;
import com.jorge.examenes.dto.ExamenGetDTO;
import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.mapping.ExamenMapper;
import com.jorge.examenes.repository.ExamenRepository;
import com.jorge.examenes.repository.PreguntaRepository;
import com.jorge.examenes.services.ExamenService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ExamenServiceImpl implements ExamenService {
    String nf="Examen no encontrado con ID: ";
    private final ExamenRepository examenRepository;
    private final PreguntaRepository preguntaRepository;
    private final ExamenMapper examenMapper; // <-- Inyectamos el Mapper

    public ExamenServiceImpl(ExamenRepository examenRepository, PreguntaRepository preguntaRepository, ExamenMapper examenMapper) {
        this.examenRepository = examenRepository;
        this.preguntaRepository = preguntaRepository;
        this.examenMapper = examenMapper;
    }

    // listar todos (resumen)
    @Override
    public List<ExamenGetDTO> obtenerTodosResumen() {
        List<Examen> examenes = examenRepository.findAll();
        return examenMapper.toResumenDTOList(examenes); // <-- 1 sola línea gracias a MapStruct
    }

    // detalle por id (numerado y censurado)
    @Override
    public ExamenDetalleDTO obtenerDetallePorId(Long id) {
        Examen examen = examenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(nf + id));

        return examenMapper.toDetalleDTO(examen); // <-- Automáticamente numerará las preguntas
    }

    // generar aleatorio
    @Override
    public ExamenDetalleDTO generarExamenAleatorio(String titulo, String descripcion, int numPreguntas) {
        Examen examen = new Examen();
        examen.setTitulo(titulo);
        examen.setDescripcion(descripcion);

        List<Pregunta> preguntasAleatorias = preguntaRepository.findPreguntasAleatorias(numPreguntas);
        if (preguntasAleatorias.size() < numPreguntas) {
            throw new NotFoundException("No hay suficientes preguntas en la base de datos.");
        }

        examen.setPreguntas(preguntasAleatorias);
        Examen examenGuardado = examenRepository.save(examen);

        // devolvemos el dto en lugar de la entidad cruda
        return examenMapper.toDetalleDTO(examenGuardado);
    }

    @Override
    public ExamenDetalleDTO actualizarPreguntasDeExamen(Long idExamen, List<Long> idsNuevasPreguntas) {
        Examen examen = examenRepository.findById(idExamen)
                .orElseThrow(() -> new NotFoundException(nf + idExamen));

        List<Pregunta> nuevasPreguntas = preguntaRepository.findAllById(idsNuevasPreguntas);
        Set<Pregunta> preguntasSinDuplicados = new LinkedHashSet<>(nuevasPreguntas);

        examen.setPreguntas(new ArrayList<>(preguntasSinDuplicados));
        Examen examenActualizado = examenRepository.save(examen);

        return examenMapper.toDetalleDTO(examenActualizado);
    }

    @Override
    public ExamenDetalleDTO actualizarDetallesExamen(Long id, String titulo, String descripcion) {
        Examen examen = examenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(nf + id));

        if (titulo != null && !titulo.isEmpty()) examen.setTitulo(titulo);
        if (descripcion != null) examen.setDescripcion(descripcion);

        Examen actualizado = examenRepository.save(examen);
        return examenMapper.toDetalleDTO(actualizado);
    }

    @Override
    public void borrarExamen(Long id) {
        if (!examenRepository.existsById(id)) {
            throw new NotFoundException(nf + id);
        }
        examenRepository.deleteById(id);
    }

    @Override
    public ExamenDetalleDTO anadirPreguntas(Long idExamen, List<Long> idsPreguntasNuevas) {
        Examen examen = examenRepository.findById(idExamen)
                .orElseThrow(() -> new NotFoundException(nf+idExamen));

        // buscamos las preguntas nuevas en la bd
        List<Pregunta> nuevasPreguntas = preguntaRepository.findAllById(idsPreguntasNuevas);

        // usamos un linkedhashset y metemos primero las preguntas viejas para no perderlas (ejemplo: ahora estaria asi [1, 5, 9])
        Set<Pregunta> preguntasUnificadas = new LinkedHashSet<>(examen.getPreguntas());

        // intentamos añadir de golpe las preguntas nuevas [5, 8, 2]
        preguntasUnificadas.addAll(nuevasPreguntas);

        //actual [1, 5, 9, 8, 2], se ponen al final y se evitan duplicados, y se pasa a la bbdd como lista
        examen.setPreguntas(new ArrayList<>(preguntasUnificadas));
        Examen actualizado = examenRepository.save(examen);

        return examenMapper.toDetalleDTO(actualizado);
    }

    @Override
    public Page<ExamenGetDTO> obtenerExamenesPaginados(int page, int size, String sortBy, String sortDir) {
        String campoEntidad = switch (sortBy.toLowerCase()) {
            case "titulo" -> "titulo";
            case "fecha" -> "fechaCreacion";
            default -> "id";
        };

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(campoEntidad).ascending() : Sort.by(campoEntidad).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Examen> paginaExamenes = examenRepository.findAll(pageable);

        return paginaExamenes.map(examenMapper::toResumenDTO);
    }
}