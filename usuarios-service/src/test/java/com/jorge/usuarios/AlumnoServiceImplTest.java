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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.mockito.ArgumentCaptor;

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

    @Test
    void obtenerAlumnosPaginados_OrdenAscendente_PorNombre() {
        int page = 0, size = 10;
        String sortBy = "nombre", sortDir = "asc";

        User user = new User();
        AlumnoDTO dto = new AlumnoDTO();
        Page<User> paginaMock = new PageImpl<>(List.of(user));

        when(userRepository.findByRoles_Name(eq("ALUMNO"), any(Pageable.class))).thenReturn(paginaMock);
        when(alumnoMapper.toAlumnoDTO(user)).thenReturn(dto);

        Page<AlumnoDTO> resultado = alumnoService.obtenerAlumnosPaginados(page, size, sortBy, sortDir);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());

        ArgumentCaptor<Pageable> capturador = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByRoles_Name(eq("ALUMNO"), capturador.capture());

        assertEquals(Sort.by("nombreUsuario").ascending(), capturador.getValue().getSort());
    }

    @Test
    void obtenerAlumnosPaginados_OrdenDescendente_PorCorreoYDefault() {
        Page<User> paginaMock = new PageImpl<>(List.of());
        when(userRepository.findByRoles_Name(eq("ALUMNO"), any(Pageable.class))).thenReturn(paginaMock);

        // Hacemos dos llamadas para probar el switch "correo" y el caso "default"
        alumnoService.obtenerAlumnosPaginados(0, 10, "correo", "desc");
        alumnoService.obtenerAlumnosPaginados(0, 10, "campo_inventado", "desc");

        ArgumentCaptor<Pageable> capturador = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository, times(2)).findByRoles_Name(eq("ALUMNO"), capturador.capture());

        List<Pageable> capturados = capturador.getAllValues();
        // Verificamos que tradujo "correo" a "emailUsuario"
        assertEquals(Sort.by("emailUsuario").descending(), capturados.get(0).getSort());
        // Verificamos que el default se fue a "idUser"
        assertEquals(Sort.by("idUser").descending(), capturados.get(1).getSort());
    }

    @Test
    void obtenerAlumnosPaginados_OrdenPorApellidos() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "apellidos";
        String sortDir = "asc";

        // Creamos una página vacía, así no necesitamos meter ningún User ni AlumnoDTO
        Page<com.jorge.usuarios.entity.User> paginaVacia =
                new PageImpl<>(java.util.Collections.emptyList());

        when(userRepository.findByRoles_Name(eq("ALUMNO"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(paginaVacia);

        // Act
        Page<AlumnoDTO> resultado =
                alumnoService.obtenerAlumnosPaginados(page, size, sortBy, sortDir);

        // Assert
        assertNotNull(resultado);

        // El chivato: capturamos lo que el servicio le mandó al repositorio
        org.mockito.ArgumentCaptor<Pageable> capturador =
                org.mockito.ArgumentCaptor.forClass(Pageable.class);

        verify(userRepository).findByRoles_Name(eq("ALUMNO"), capturador.capture());

        // Verificamos la magia: ¿Tradujo "apellidos" a "apellidoUsuario"?
        assertEquals(Sort.by("apellidoUsuario").ascending(), capturador.getValue().getSort());
    }
}