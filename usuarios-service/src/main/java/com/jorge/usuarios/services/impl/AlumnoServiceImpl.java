package com.jorge.usuarios.services.impl;

import com.jorge.usuarios.dto.AlumnoDTO;
import com.jorge.usuarios.entity.User;
import com.jorge.usuarios.exceptions.NotFoundException;
import com.jorge.usuarios.mapping.AlumnoMapper;
import com.jorge.usuarios.repository.UserRepository;
import com.jorge.usuarios.services.AlumnoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlumnoServiceImpl implements AlumnoService {

    private final UserRepository userRepository;
    private final AlumnoMapper alumnoMapper;

    public AlumnoServiceImpl(UserRepository userRepository, AlumnoMapper alumnoMapper) {
        this.userRepository = userRepository;
        this.alumnoMapper = alumnoMapper;
    }

    @Override
    public List<AlumnoDTO> obtenerTodosLosAlumnos() {
        // Le pasamos directamente el texto "ALUMNO"
        List<User> alumnos = userRepository.findByRoles_Name("ALUMNO");
        return alumnoMapper.toAlumnoDTOList(alumnos);
    }

    @Override
    public AlumnoDTO obtenerAlumnoPorCorreo(String email) throws NotFoundException{
        // Le pasamos directamente el email y el texto "ALUMNO"
        User alumno =userRepository.findByEmailUsuarioAndRoles_Name(email, "ALUMNO").
                orElseThrow(() -> new NotFoundException("No se ha encontrado ningún alumno con el correo: " + email));

        return alumnoMapper.toAlumnoDTO(alumno);
    }
}