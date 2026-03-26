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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.mockito.ArgumentCaptor;
import java.util.List;

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
        assertEquals("Nuevo", resultado.getEnunciado());
        verify(preguntaRepository, times(1)).save(preguntaMock);
    }

    @Test
    void borrarPregunta_DebeInvocarDelete() {
        when(preguntaRepository.findById(1L)).thenReturn(Optional.of(preguntaMock));
        doNothing().when(preguntaRepository).delete(preguntaMock);

        preguntaService.borrarPregunta(1L);

        verify(preguntaRepository, times(1)).delete(preguntaMock);
    }

    @Test
    void obtenerPreguntasPaginadas_DebeRetornarPagina_ConOrdenAscendente() {
        // 1. Arrange (Preparación)
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "asc"; // Provocamos la rama ascendente

        // Simulamos la respuesta del repositorio (una página con nuestra pregunta mock)
        Page<Pregunta> paginaMock = new PageImpl<>(List.of(preguntaMock));
        when(preguntaRepository.findAll(any(Pageable.class))).thenReturn(paginaMock);

        // 2. Act (Ejecución)
        Page<Pregunta> resultado = preguntaService.obtenerPreguntasPaginadas(page, size, sortBy, sortDir);

        // 3. Assert (Verificación)
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());

        // CAPTURADOR: Verificamos que se construyó el Pageable exactamente como queríamos
        ArgumentCaptor<Pageable> capturador = ArgumentCaptor.forClass(Pageable.class);
        verify(preguntaRepository, times(1)).findAll(capturador.capture());

        Pageable pageableGenerado = capturador.getValue();
        assertEquals(page, pageableGenerado.getPageNumber());
        assertEquals(size, pageableGenerado.getPageSize());
        assertEquals(Sort.by(sortBy).ascending(), pageableGenerado.getSort());
    }

    @Test
    void obtenerPreguntasPaginadas_DebeRetornarPagina_ConOrdenDescendente() {
        // 1. Arrange (Preparación)
        int page = 2;
        int size = 5;
        String sortBy = "enunciado";
        String sortDir = "desc"; // Provocamos la rama descendente

        // Simulamos una página vacía por variar el escenario
        Page<Pregunta> paginaMock = new PageImpl<>(Collections.emptyList());
        when(preguntaRepository.findAll(any(Pageable.class))).thenReturn(paginaMock);

        // 2. Act (Ejecución)
        Page<Pregunta> resultado = preguntaService.obtenerPreguntasPaginadas(page, size, sortBy, sortDir);

        // 3. Assert (Verificación)
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        // CAPTURADOR: Verificamos que se construyó el Pageable con orden DESC
        ArgumentCaptor<Pageable> capturador = ArgumentCaptor.forClass(Pageable.class);
        verify(preguntaRepository, times(1)).findAll(capturador.capture());

        Pageable pageableGenerado = capturador.getValue();
        assertEquals(page, pageableGenerado.getPageNumber());
        assertEquals(size, pageableGenerado.getPageSize());
        assertEquals(Sort.by(sortBy).descending(), pageableGenerado.getSort());
    }
}