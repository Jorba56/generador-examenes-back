package com.jorge.usuarios;

import com.jorge.usuarios.dto.AlumnoDTO;
import com.jorge.usuarios.entity.User;
import com.jorge.usuarios.exceptions.NotFoundException;
import com.jorge.usuarios.mapping.AlumnoMapper;
import com.jorge.usuarios.repository.UserRepository;
import com.jorge.usuarios.services.impl.AlumnoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlumnoServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AlumnoMapper alumnoMapper;

    @InjectMocks
    private AlumnoServiceImpl alumnoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void obtenerTodosLosAlumnos_DebeRetornarListaDeDTOs() {
        // Arrange
        User user = new User();
        AlumnoDTO dto = new AlumnoDTO();
        when(userRepository.findByRoles_Name("ALUMNO")).thenReturn(Arrays.asList(user));
        when(alumnoMapper.toAlumnoDTOList(anyList())).thenReturn(Arrays.asList(dto));

        // Act
        List<AlumnoDTO> resultado = alumnoService.obtenerTodosLosAlumnos();

        // Assert
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(userRepository, times(1)).findByRoles_Name("ALUMNO");
    }

    @Test
    void obtenerAlumnoPorCorreo_CuandoExiste_DebeRetornarDTO() throws NotFoundException {
        // Arrange
        String email = "alumno@test.com";
        User user = new User();
        AlumnoDTO dto = new AlumnoDTO();
        when(userRepository.findByEmailUsuarioAndRoles_Name(email, "ALUMNO")).thenReturn(Optional.of(user));
        when(alumnoMapper.toAlumnoDTO(user)).thenReturn(dto);

        // Act
        AlumnoDTO resultado = alumnoService.obtenerAlumnoPorCorreo(email);

        // Assert
        assertNotNull(resultado);
        verify(userRepository).findByEmailUsuarioAndRoles_Name(email, "ALUMNO");
    }

    @Test
    void obtenerAlumnoPorCorreo_CuandoNoExiste_DebeLanzarNotFound() {
        // Arrange
        String email = "noexiste@test.com";
        when(userRepository.findByEmailUsuarioAndRoles_Name(email, "ALUMNO")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> alumnoService.obtenerAlumnoPorCorreo(email));
    }
}