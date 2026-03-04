package com.jorge.sprintdef.services;


import com.jorge.sprintdef.Incidencia;
import com.jorge.sprintdef.IncidenciasRepository;
import com.jorge.sprintdef.TipoIncidencia;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IncidenciasService {

    private final IncidenciasRepository incidenciasRepository;

    public IncidenciasService(IncidenciasRepository incidenciasRepository) {
        this.incidenciasRepository = incidenciasRepository;
    }

    public List<Incidencia> listarIncidencias(){
        return incidenciasRepository.findAll();
    }

    public Optional<Incidencia> incidenciaPorId (Long id){
        return incidenciasRepository.findById(id);
    }

    public String newIncidencia (Incidencia inc){
        incidenciasRepository.save(inc);
        return("Incidencia añadido con exito");
    }

    public List<Incidencia> incidenciasPorTipo(TipoIncidencia tipo) {
        return incidenciasRepository.findByTipo(tipo);
    }

}
