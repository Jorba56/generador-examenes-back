package com.jorge.examenes;

import com.jorge.examenes.dto.ExamenDetalleDTO;
import com.jorge.examenes.dto.PreguntaExamenDTO;
import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.mapping.ExamenMapper;
import com.jorge.examenes.mapping.PreguntaMapper;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamenServiceImplTest {

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private PreguntaRepository preguntaRepository;

    @Mock
    private ExamenMapper examenMapper;

    @Mock
    private PreguntaMapper preguntaMapper;

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
        // 1. Datos de entrada (Entidades)
        List<Pregunta> mockPreguntas = Arrays.asList(p1, p2);

        Examen examenGuardado = new Examen();
        examenGuardado.setId(100L);
        examenGuardado.setTitulo("Titulo F1");
        examenGuardado.setDescripcion("Desc F1");
        examenGuardado.setPreguntas(mockPreguntas);

        // 2. CREAMOS LA LISTA MANUALMENTE (En vez de usar el mapper mockeado)
        List<PreguntaExamenDTO> preg = Arrays.asList(new PreguntaExamenDTO(), new PreguntaExamenDTO());

        // 3. Preparamos nuestro DTO de salida
        ExamenDetalleDTO resultadoMock = new ExamenDetalleDTO();
        resultadoMock.setId(examenGuardado.getId());
        resultadoMock.setDescripcion(examenGuardado.getDescripcion());
        resultadoMock.setTitulo(examenGuardado.getTitulo());
        resultadoMock.setPreguntas(preg); // <--- Ahora sí le estamos metiendo 2 preguntas

        // 4. Comportamientos
        when(preguntaRepository.findPreguntasAleatorias(2)).thenReturn(mockPreguntas);
        when(examenRepository.save(any(Examen.class))).thenReturn(examenGuardado);
        given(examenMapper.toDetalleDTO(any(Examen.class))).willReturn(resultadoMock);

        // 5. Ejecutamos
        ExamenDetalleDTO resultado = examenService.generarExamenAleatorio("Titulo F1", "Desc F1", 2);

        // 6. Aserciones
        assertEquals(100L, resultado.getId());
        assertEquals("Titulo F1", resultado.getTitulo());
        assertEquals(2, resultado.getPreguntas().size()); // ¡Ahora sí será 2!

        verify(preguntaRepository, times(1)).findPreguntasAleatorias(2);
        verify(examenMapper).toDetalleDTO(any(Examen.class));
        verify(examenRepository, times(1)).save(any(Examen.class));
    }

    @Test
    void generarExamenAleatorio_CuandoNoHaySuficientesPreguntas_DebeLanzarExcepcion() {
        // tenemos 2 preguntas, pero el usuario va a pedir 5
        List<Pregunta> mockPreguntas = Arrays.asList(p1, p2);

        when(preguntaRepository.findPreguntasAleatorias(5)).thenReturn(mockPreguntas);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            examenService.generarExamenAleatorio("Titulo F1", "Desc F1", 5);
        });

        assertEquals("No hay suficientes preguntas en la base de datos.", exception.getMessage());

        // verificamos que, al saltar la excepción, jamás se llega a guardar el examen
        verify(preguntaRepository, times(1)).findPreguntasAleatorias(5);
        verify(examenRepository, never()).save(any(Examen.class));
    }
}