package com.jorge.usuarios.services;

import com.jorge.usuarios.dto.AlumnoDTO;
import com.jorge.usuarios.exceptions.NotFoundException;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interfaz que define el contrato para el servicio de gestión de Alumnos.
 * <p>
 * Establece las operaciones permitidas para filtrar y consultar exclusivamente
 * a los usuarios que poseen el rol de ALUMNO dentro del sistema.
 * </p>
 */
public interface AlumnoService {

    /**
     * Obtiene la lista completa de todos los usuarios que poseen el rol de ALUMNO.
     *
     * @return Lista de {@link AlumnoDTO}.
     */
    List<AlumnoDTO> obtenerTodosLosAlumnos();

    /**
     * Busca a un alumno específico mediante su correo electrónico.
     *
     * @param email Correo electrónico a buscar.
     * @return {@link AlumnoDTO} con los datos del alumno.
     * @throws NotFoundException Si no se encuentra un alumno con ese correo.
     */
    AlumnoDTO obtenerAlumnoPorCorreo(String email) throws NotFoundException;

    /**
     * Obtiene una lista paginada y ordenada de los alumnos registrados.
     *
     * @param page Número de página a consultar.
     * @param size Tamaño máximo de la página.
     * @param sortBy Campo de ordenación.
     * @param sortDir Dirección de ordenación ("asc" o "desc").
     * @return Objeto {@link Page} que contiene una lista de {@link AlumnoDTO}.
     */
    Page<AlumnoDTO> obtenerAlumnosPaginados(int page, int size, String sortBy, String sortDir);
}
