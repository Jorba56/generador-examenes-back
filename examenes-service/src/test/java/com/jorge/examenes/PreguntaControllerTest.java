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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        doNothing().when(preguntaService).borrarPregunta(1L);

        ResponseEntity<String> response = preguntaController.borrarPregunta(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pregunta eliminada correctamente de la base de datos.", response.getBody());
        verify(preguntaService, times(1)).borrarPregunta(1L);
    }
}