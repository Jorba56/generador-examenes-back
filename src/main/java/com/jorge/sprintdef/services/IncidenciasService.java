package com.jorge.sprintdef.services;

import com.jorge.sprintdef.Incidencia;
import com.jorge.sprintdef.IncidenciasRepository; // Interfaz que extiende de JpaRepository<Incidencia, Long>
import com.jorge.sprintdef.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidenciasService {

    private final IncidenciasRepository incidenciaRepository;

    public IncidenciasService(IncidenciasRepository incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    public List<Incidencia> obtenerTodas() {
        return incidenciaRepository.findAll();
    }

    public void guardar(Incidencia incidencia) {
        incidenciaRepository.save(incidencia);
    }

    public Incidencia obtenerPorId(Long id) {
        return incidenciaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado ninguna incidencia con el ID: " + id));
    }

    public List<Incidencia> obtenerPorUsuario(Long idUsuario) {
        List<Incidencia> incidencias = incidenciaRepository.findByIdUsuario(idUsuario);
        if (incidencias.isEmpty()) {
            throw new NotFoundException("No se han encontrado incidencias para el usuario con ID: " + idUsuario);
        }
        return incidencias;
    }

    public List<Incidencia> obtenerPorClase(String clase) {
        List<Incidencia> incidencias = incidenciaRepository.findByClase(clase);
        if (incidencias.isEmpty()) {
            throw new NotFoundException("No se han encontrado incidencias originadas en la clase: " + clase);
        }
        return incidencias;
    }

    public List<Incidencia> obtenerPorMetodo(String metodo) {
        List<Incidencia> incidencias = incidenciaRepository.findByMetodo(metodo);
        if (incidencias.isEmpty()) {
            throw new NotFoundException("No se han encontrado incidencias originadas en el método: " + metodo);
        }
        return incidencias;
    }
}