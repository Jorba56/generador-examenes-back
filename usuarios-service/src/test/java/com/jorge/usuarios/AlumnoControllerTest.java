package com.jorge.usuarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jorge.usuarios.controller.AlumnoController;
import com.jorge.usuarios.dto.AlumnoDTO;
import com.jorge.usuarios.services.AlumnoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlumnoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlumnoService alumnoService;

    @InjectMocks
    private AlumnoController alumnoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Configuración aislada sin Spring Security para test unitario puro
        mockMvc = MockMvcBuilders.standaloneSetup(alumnoController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listarAlumnos_DeberiaDevolverLista() throws Exception {
        // Arrange
        AlumnoDTO alumno = new AlumnoDTO(1L, "Jorge", "Sánchez", "jorge@gmail.com");
        List<AlumnoDTO> lista = Arrays.asList(alumno);

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
}