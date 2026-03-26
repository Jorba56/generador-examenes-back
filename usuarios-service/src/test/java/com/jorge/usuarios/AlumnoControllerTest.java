package com.jorge.usuarios;

import com.jorge.usuarios.controller.AlumnoController;
import com.jorge.usuarios.dto.AlumnoDTO;
import com.jorge.usuarios.services.AlumnoService;
import org.apache.catalina.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AlumnoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlumnoService alumnoService;

    @InjectMocks
    private AlumnoController alumnoController;

    @BeforeEach
    void setUp() {
        // configuración aislada sin spring security para test unitario puro
        mockMvc = MockMvcBuilders.standaloneSetup(alumnoController).build();
    }

    @Test
    void listarAlumnos_DeberiaDevolverLista() throws Exception {
        // Arrange
        AlumnoDTO alumno = new AlumnoDTO(1L, "Jorge", "Sánchez", "jorge@gmail.com");
        List<AlumnoDTO> lista = List.of(alumno);

        given(alumnoService.obtenerTodosLosAlumnos()).willReturn(lista);

        // Act & Assert
        mockMvc.perform(get("/alumnos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Jorge"))
                .andExpect(jsonPath("$[0].correo").value("jorge@gmail.com"));
    }

    @Test
    void obtenerAlumnoPorCorreo_DeberiaDevolverAlumno() throws Exception {
        // Arrange
        String email = "jorge@gmail.com";
        AlumnoDTO alumno = new AlumnoDTO(1L, "Jorge", "Sánchez", email);

        given(alumnoService.obtenerAlumnoPorCorreo(anyString())).willReturn(alumno);

        // Act & Assert
        mockMvc.perform(get("/alumnos/" + email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jorge"))
                .andExpect(jsonPath("$.correo").value(email));
    }
    @Test
    void exportarAlumnosAExcel_DeberiaDescargarArchivo() throws Exception {
        given(alumnoService.obtenerTodosLosAlumnos()).willReturn(List.of(new AlumnoDTO()));

        mockMvc.perform(get("/alumnos/exportar/excel"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", "application/octet-stream"));
    }

    @Test
    void listarAlumnosPaginados_DeberiaDevolverPagina() {
        // Arrange
        int page = 0, size = 10;
        String sortBy = "apellidos", sortDir = "asc";

        AlumnoDTO dto = new AlumnoDTO(1L, "Jorge", "Barriga", "jorge@test.com");
        org.springframework.data.domain.Page<AlumnoDTO> pageMock = new org.springframework.data.domain.PageImpl<>(java.util.List.of(dto));

        given(alumnoService.obtenerAlumnosPaginados(page, size, sortBy, sortDir)).willReturn(pageMock);


        ResponseEntity<Page<AlumnoDTO>> response =
                alumnoController.listarAlumnosPaginados(page, size, sortBy, sortDir);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Jorge", response.getBody().getContent().getFirst().getNombre());

        verify(alumnoService, org.mockito.Mockito.times(1)).obtenerAlumnosPaginados(page, size, sortBy, sortDir);
    }

}