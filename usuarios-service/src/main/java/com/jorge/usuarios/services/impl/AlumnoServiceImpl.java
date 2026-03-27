package com.jorge.usuarios.services.impl;

import com.jorge.usuarios.dto.AlumnoDTO;
import com.jorge.usuarios.entity.User;
import com.jorge.usuarios.exceptions.NotFoundException;
import com.jorge.usuarios.mapping.AlumnoMapper;
import com.jorge.usuarios.repository.UserRepository;
import com.jorge.usuarios.services.AlumnoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio de Alumnos.
 * <p>
 * Se encarga de filtrar y recuperar específicamente a los usuarios que
 * tienen asignado el rol de "ALUMNO", interactuando con el repositorio general de usuarios.
 * </p>
 */
@Service
public class AlumnoServiceImpl implements AlumnoService {

    private final UserRepository userRepository;
    private final AlumnoMapper alumnoMapper;
    String a="ALUMNO";

    /**
     * Constructor que inyecta las dependencias necesarias.
     *
     * @param userRepository Repositorio de acceso a datos de usuarios.
     * @param alumnoMapper Mapper para convertir entidades User en AlumnoDTO.
     */
    public AlumnoServiceImpl(UserRepository userRepository, AlumnoMapper alumnoMapper) {
        this.userRepository = userRepository;
        this.alumnoMapper = alumnoMapper;
    }

    /**
     * Obtiene la lista completa de todos los usuarios que poseen el rol de ALUMNO.
     *
     * @return Lista de {@link AlumnoDTO} con los datos públicos de los alumnos.
     */
    @Override
    public List<AlumnoDTO> obtenerTodosLosAlumnos() {
        // Le pasamos directamente el texto "ALUMNO"
        List<User> alumnos = userRepository.findByRoles_Name(a);
        return alumnoMapper.toAlumnoDTOList(alumnos);
    }

    /**
     * Busca a un alumno específico mediante su dirección de correo electrónico.
     *
     * @param email Correo electrónico exacto del alumno a buscar.
     * @return {@link AlumnoDTO} con los datos del alumno encontrado.
     * @throws NotFoundException Si no se encuentra ningún alumno con ese correo en la base de datos.
     */
    @Override
    public AlumnoDTO obtenerAlumnoPorCorreo(String email) throws NotFoundException{
        // Le pasamos directamente el email y el texto "ALUMNO"
        User alumno =userRepository.findByEmailUsuarioAndRoles_Name(email, a).
                orElseThrow(() -> new NotFoundException("No se ha encontrado ningún alumno con el correo: " + email));

        return alumnoMapper.toAlumnoDTO(alumno);
    }

    /**
     * Obtiene una lista paginada y ordenada de los alumnos registrados en el sistema.
     *
     * @param page Número de la página a consultar (comienza en 0).
     * @param size Cantidad máxima de alumnos a mostrar por página.
     * @param sortBy Campo de ordenación ("nombre", "apellidos" o "correo").
     * @param sortDir Dirección de la ordenación ("asc" para ascendente, "desc" para descendente).
     * @return Un objeto {@link Page} que contiene los {@link AlumnoDTO} correspondientes a la página solicitada.
     */
    @Override
    public Page<AlumnoDTO> obtenerAlumnosPaginados(int page, int size, String sortBy, String sortDir) {
        // Traductor rápido para alumnos
        String campoEntidad = switch (sortBy.toLowerCase()) {
            case "nombre" -> "nombreUsuario";
            case "apellidos" -> "apellidoUsuario";
            case "correo" -> "emailUsuario";
            default -> "idUser";
        };

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(campoEntidad).ascending() : Sort.by(campoEntidad).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> paginaAlumnos = userRepository.findByRoles_Name(a, pageable);
        return paginaAlumnos.map(alumnoMapper::toAlumnoDTO);
    }
}