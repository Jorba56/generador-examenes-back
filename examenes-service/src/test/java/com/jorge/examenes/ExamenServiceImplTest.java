package com.jorge.examenes;

import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.repository.ExamenRepository;
import com.jorge.examenes.repository.PreguntaRepository;
import com.jorge.examenes.services.impl.ExamenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamenServiceImplTest {

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private PreguntaRepository preguntaRepository;

    @InjectMocks
    private ExamenServiceImpl examenService;

    private Pregunta p1;
    private Pregunta p2;

    @BeforeEach
    void setUp() {
        p1 = new Pregunta();
        p1.setId(1L);

        p2 = new Pregunta();
        p2.setId(2L);
    }

    @Test
    void generarExamenAleatorio_DebeCrearYGuardarExamen() {
        List<Pregunta> mockPreguntas = Arrays.asList(p1, p2);

        Examen examenGuardado = new Examen();
        examenGuardado.setId(100L);
        examenGuardado.setTitulo("Titulo F1");
        examenGuardado.setDescripcion("Desc F1");
        examenGuardado.setPreguntas(mockPreguntas);

        when(preguntaRepository.findPreguntasAleatorias(2)).thenReturn(mockPreguntas);
        when(examenRepository.save(any(Examen.class))).thenReturn(examenGuardado);

        Examen resultado = examenService.generarExamenAleatorio("Titulo F1", "Desc F1", 2);

        assertEquals(100L, resultado.getId());
        assertEquals("Titulo F1", resultado.getTitulo());
        assertEquals(2, resultado.getPreguntas().size());

        verify(preguntaRepository, times(1)).findPreguntasAleatorias(2);
        verify(examenRepository, times(1)).save(any(Examen.class));
    }

    @Test
    void generarExamenAleatorio_CuandoNoHaySuficientesPreguntas_DebeLanzarExcepcion() {
        // tenemos 2 preguntas, pero el usuario va a pedir 5
        List<Pregunta> mockPreguntas = Arrays.asList(p1, p2);

        when(preguntaRepository.findPreguntasAleatorias(5)).thenReturn(mockPreguntas);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examenService.generarExamenAleatorio("Titulo F1", "Desc F1", 5);
        });

        assertEquals("No hay suficientes preguntas en la base de datos para generar este examen.", exception.getMessage());

        // verificamos que, al saltar la excepción, jamás se llega a guardar el examen
        verify(preguntaRepository, times(1)).findPreguntasAleatorias(5);
        verify(examenRepository, never()).save(any(Examen.class));
    }
}