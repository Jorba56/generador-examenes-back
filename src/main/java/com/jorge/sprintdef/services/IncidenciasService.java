package com.jorge.sprintdef.services;

import com.jorge.sprintdef.entity.Incidencia;

import java.util.List;

public interface IncidenciasService {
    List<Incidencia> obtenerTodas();

    void guardar(Incidencia incidencia);

    Incidencia obtenerPorId(Long id);

    List<Incidencia> obtenerPorUsuario(Long idUsuario);

    List<Incidencia> obtenerPorClase(String clase);

    List<Incidencia> obtenerPorMetodo(String metodo);
}
