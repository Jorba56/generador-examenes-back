package com.jorge.usuarios.services;

import com.jorge.usuarios.dto.AlumnoDTO;
import com.jorge.usuarios.exceptions.NotFoundException;

import java.util.List;

public interface AlumnoService {
    List<AlumnoDTO> obtenerTodosLosAlumnos();

    AlumnoDTO obtenerAlumnoPorCorreo(String email) throws NotFoundException;
}
