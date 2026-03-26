package com.jorge.examenes;

import com.jorge.examenes.controller.PreguntaController;
import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.services.impl.PreguntaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreguntaControllerTest {

    @Mock
    private PreguntaServiceImpl preguntaService;

    @InjectMocks
    private PreguntaController preguntaController;

    private Pregunta preguntaMock;

    @BeforeEach
    void setUp() {
        preguntaMock = new Pregunta();
        preguntaMock.setId(1L);
        preguntaMock.setEnunciado("¿Pregunta de prueba?");
    }

    @Test
    void listarTodas_DebeRetornarListaDePreguntas() {
        when(preguntaService.obtenerTodas()).thenReturn(Collections.singletonList(preguntaMock));

        ResponseEntity<List<Pregunta>> response = preguntaController.listarTodas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals(1, response.getBody().size());
        verify(preguntaService, times(1)).obtenerTodas();
    }

    @Test
    void obtenerPorId_DebeRetornarPregunta() {
        when(preguntaService.obtenerPorId(1L)).thenReturn(preguntaMock);

        ResponseEntity<Pregunta> response = preguntaController.obtenerPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals("¿Pregunta de prueba?", response.getBody().getEnunciado());
    }

    @Test
    void crearPregunta_DebeRetornarPreguntaCreada() {
        when(preguntaService.guardarPregunta(any(Pregunta.class))).thenReturn(preguntaMock);

        ResponseEntity<Pregunta> response = preguntaController.crearPregunta(preguntaMock);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void actualizarPregunta_DebeRetornarPreguntaActualizada() {
        when(preguntaService.actualizarPregunta(eq(1L), any(Pregunta.class))).thenReturn(preguntaMock);

        ResponseEntity<Pregunta> response = preguntaController.actualizarPregunta(1L, preguntaMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals("¿Pregunta de prueba?", response.getBody().getEnunciado());
    }

    @Test
    void borrarPregunta_DebeRetornarMensaje() {
        // Le decimos a Mockito que devuelva true cuando el controlador llame al servicio
        when(preguntaService.borrarPregunta(1L)).thenReturn(("Pregunta eliminada correctamente de la base de datos."));

        ResponseEntity<String> response = preguntaController.borrarPregunta(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pregunta eliminada correctamente de la base de datos.", response.getBody());
        verify(preguntaService, times(1)).borrarPregunta(1L);
    }

    @Test
    void listarPreguntasPaginadas_DebeRetornarPaginaYStatusOk() {
        // 1. Arrange (Preparación)
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "desc";

        // Creamos una pregunta simulada para rellenar la página
        Pregunta preguntaMock = new Pregunta();
        preguntaMock.setId(1L);
        preguntaMock.setEnunciado("Pregunta paginada de prueba");

        // Simulamos la respuesta del servicio devolviendo una página con nuestro mock
        Page<Pregunta> paginaMock = new PageImpl<>(List.of(preguntaMock));
        when(preguntaService.obtenerPreguntasPaginadas(page, size, sortBy, sortDir)).thenReturn(paginaMock);

        // 2. Act (Ejecución)
        ResponseEntity<Page<Pregunta>> response = preguntaController.listarPreguntasPaginadas(page, size, sortBy, sortDir);

        // 3. Assert (Verificaciones)
        assertNotNull(response, "La respuesta no debería ser nula");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El status HTTP debe ser 200 OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals(1, response.getBody().getTotalElements(), "La página debe contener 1 elemento");
        assertEquals(paginaMock, response.getBody(), "El cuerpo debe ser exactamente la página mockeada");

        // Verificamos que el controlador no hace cosas raras y le pasa los datos exactos al servicio
        verify(preguntaService, times(1)).obtenerPreguntasPaginadas(page, size, sortBy, sortDir);
    }
}