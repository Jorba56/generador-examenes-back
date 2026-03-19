package com.jorge.examenes;

import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.repository.PreguntaRepository;
import com.jorge.examenes.services.impl.PreguntaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreguntaServiceImplTest {

    @Mock
    private PreguntaRepository preguntaRepository;

    @InjectMocks
    private PreguntaServiceImpl preguntaService;

    private Pregunta preguntaMock;

    @BeforeEach
    void setUp() {
        preguntaMock = new Pregunta();
        preguntaMock.setId(1L);
        preguntaMock.setEnunciado("Enunciado test");
        preguntaMock.setOpcionA("A");
        preguntaMock.setOpcionB("B");
        preguntaMock.setOpcionC("C");
        preguntaMock.setOpcionD("D");
        preguntaMock.setCorrecta("A");
    }

    @Test
    void obtenerTodas_DebeRetornarLista() {
        when(preguntaRepository.findAll()).thenReturn(Collections.singletonList(preguntaMock));
        List<Pregunta> resultado = preguntaService.obtenerTodas();
        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerPorId_CuandoExiste_DebeRetornarPregunta() {
        when(preguntaRepository.findById(1L)).thenReturn(Optional.of(preguntaMock));
        Pregunta resultado = preguntaService.obtenerPorId(1L);
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        when(preguntaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> preguntaService.obtenerPorId(99L));
    }

    @Test
    void guardarPregunta_DebeRetornarPreguntaGuardada() {
        when(preguntaRepository.save(any(Pregunta.class))).thenReturn(preguntaMock);
        Pregunta resultado = preguntaService.guardarPregunta(preguntaMock);
        assertEquals("Enunciado test", resultado.getEnunciado());
    }

    @Test
    void actualizarPregunta_DebeActualizarCamposYGuardar() {
        Pregunta preguntaModificada = new Pregunta();
        preguntaModificada.setEnunciado("Nuevo");
        preguntaModificada.setOpcionA("X");
        preguntaModificada.setOpcionB("Y");
        preguntaModificada.setOpcionC("Z");
        preguntaModificada.setOpcionD("W");
        preguntaModificada.setCorrecta("B");

        when(preguntaRepository.findById(1L)).thenReturn(Optional.of(preguntaMock));
        when(preguntaRepository.save(any(Pregunta.class))).thenReturn(preguntaMock);

        Pregunta resultado = preguntaService.actualizarPregunta(1L, preguntaModificada);

        assertEquals("Nuevo", preguntaMock.getEnunciado());
        assertEquals("X", preguntaMock.getOpcionA());
        verify(preguntaRepository, times(1)).save(preguntaMock);
    }

    @Test
    void borrarPregunta_DebeInvocarDelete() {
        when(preguntaRepository.findById(1L)).thenReturn(Optional.of(preguntaMock));
        doNothing().when(preguntaRepository).delete(preguntaMock);

        preguntaService.borrarPregunta(1L);

        verify(preguntaRepository, times(1)).delete(preguntaMock);
    }
}