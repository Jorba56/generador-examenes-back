package com.jorge.examenes;

import com.jorge.examenes.dto.ExamenDetalleDTO;
import com.jorge.examenes.dto.ExamenGetDTO;
import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.mapping.ExamenMapper;
import com.jorge.examenes.repository.ExamenRepository;
import com.jorge.examenes.repository.PreguntaRepository;
import com.jorge.examenes.services.ExamenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamenServiceImplTest {

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private PreguntaRepository preguntaRepository;

    @Mock
    private ExamenMapper examenMapper;

    @InjectMocks
    private ExamenService examenService;

    @Test
    void obtenerTodosResumen_DeberiaDevolverLista() {
        List<Examen> examenes = Arrays.asList(new Examen(), new Examen());
        List<ExamenGetDTO> dtos = Arrays.asList(new ExamenGetDTO(), new ExamenGetDTO());

        when(examenRepository.findAll()).thenReturn(examenes);
        when(examenMapper.toResumenDTOList(examenes)).thenReturn(dtos);

        List<ExamenGetDTO> resultado = examenService.obtenerTodosResumen();

        assertEquals(2, resultado.size());
        verify(examenRepository).findAll();
    }

    @Test
    void obtenerDetallePorId_DeberiaDevolverExamen() {
        Examen examen = new Examen();
        examen.setId(1L);
        ExamenDetalleDTO dto = new ExamenDetalleDTO();

        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));
        when(examenMapper.toDetalleDTO(examen)).thenReturn(dto);

        ExamenDetalleDTO resultado = examenService.obtenerDetallePorId(1L);

        assertNotNull(resultado);
        verify(examenRepository).findById(1L);
    }

    @Test
    void obtenerDetallePorId_DeberiaLanzarNotFound() {
        when(examenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> examenService.obtenerDetallePorId(99L));
    }

    @Test
    void generarExamenAleatorio_DeberiaCrearYGuardar() {
        List<Pregunta> preguntas = Arrays.asList(new Pregunta(), new Pregunta());
        Examen examenGuardado = new Examen();
        ExamenDetalleDTO dto = new ExamenDetalleDTO();

        when(preguntaRepository.findPreguntasAleatorias(2)).thenReturn(preguntas);
        when(examenRepository.save(any(Examen.class))).thenReturn(examenGuardado);
        when(examenMapper.toDetalleDTO(examenGuardado)).thenReturn(dto);

        ExamenDetalleDTO resultado = examenService.generarExamenAleatorio("Test", "Desc", 2);

        assertNotNull(resultado);
        verify(examenRepository).save(any(Examen.class));
    }

    @Test
    void generarExamenAleatorio_DeberiaLanzarNotFoundSiFaltanPreguntas() {
        when(preguntaRepository.findPreguntasAleatorias(10)).thenReturn(new ArrayList<>());

        assertThrows(NotFoundException.class, () ->
                examenService.generarExamenAleatorio("Test", "Desc", 10)
        );
    }

    @Test
    void actualizarDetallesExamen_DeberiaActualizar() {
        Examen examen = new Examen();
        examen.setId(1L);
        ExamenDetalleDTO dto = new ExamenDetalleDTO();

        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));
        when(examenRepository.save(any(Examen.class))).thenReturn(examen);
        when(examenMapper.toDetalleDTO(examen)).thenReturn(dto);

        ExamenDetalleDTO resultado = examenService.actualizarDetallesExamen(1L, "Nuevo Titulo", "Nueva Desc");

        assertNotNull(resultado);
        assertEquals("Nuevo Titulo", examen.getTitulo());
        assertEquals("Nueva Desc", examen.getDescripcion());
        verify(examenRepository).save(examen);
    }

    @Test
    void borrarExamen_DeberiaBorrarSiExiste() {
        when(examenRepository.existsById(1L)).thenReturn(true);

        examenService.borrarExamen(1L);

        verify(examenRepository).deleteById(1L);
    }

    @Test
    void borrarExamen_DeberiaLanzarNotFoundSiNoExiste() {
        when(examenRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> examenService.borrarExamen(99L));
        verify(examenRepository, never()).deleteById(anyLong());
    }

    @Test
    void anadirPreguntas_DeberiaAnadirSinDuplicados() {
        Examen examen = new Examen();
        examen.setId(1L);
        Pregunta p1 = new Pregunta(); p1.setId(10L);
        examen.setPreguntas(new ArrayList<>(List.of(p1)));

        Pregunta p2 = new Pregunta(); p2.setId(20L);
        List<Pregunta> nuevasPreguntas = List.of(p2);
        ExamenDetalleDTO dto = new ExamenDetalleDTO();

        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));
        when(preguntaRepository.findAllById(anyList())).thenReturn(nuevasPreguntas);
        when(examenRepository.save(any(Examen.class))).thenReturn(examen);
        when(examenMapper.toDetalleDTO(examen)).thenReturn(dto);

        ExamenDetalleDTO resultado = examenService.anadirPreguntas(1L, List.of(20L));

        assertNotNull(resultado);
        assertEquals(2, examen.getPreguntas().size()); // la vieja + la nueva
        verify(examenRepository).save(examen);
    }

    @Test
    void actualizarPreguntasDeExamen_DeberiaActualizarYQuitarDuplicados() {
        // preparamos el examen
        Examen examen = new Examen();
        examen.setId(1L);

        // creamos una sola pregunta, pero la metemos dos veces en la lista
        Pregunta p1 = new Pregunta();
        p1.setId(10L);
        List<Pregunta> nuevasPreguntasConDuplicados = Arrays.asList(p1, p1); // <-- la misma referencia dos veces

        ExamenDetalleDTO dto = new ExamenDetalleDTO();

        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));
        when(preguntaRepository.findAllById(anyList())).thenReturn(nuevasPreguntasConDuplicados);
        when(examenRepository.save(any(Examen.class))).thenReturn(examen);
        when(examenMapper.toDetalleDTO(examen)).thenReturn(dto);

        ExamenDetalleDTO resultado = examenService.actualizarPreguntasDeExamen(1L, Arrays.asList(10L, 10L));

        assertNotNull(resultado);
        assertEquals(1, examen.getPreguntas().size());
        verify(examenRepository).save(examen);
    }

    @Test
    void actualizarPreguntasDeExamen_DeberiaLanzarNotFoundSiExamenNoExiste() {
        when(examenRepository.findById(99L)).thenReturn(Optional.empty());

        // creación de la lista fuera de la lambda
        List<Long> idsNuevasPreguntas = List.of(10L);

        assertThrows(NotFoundException.class, () ->
                examenService.actualizarPreguntasDeExamen(99L, idsNuevasPreguntas)
        );

        verify(examenRepository, never()).save(any());
    }
    @Test
    void actualizarDetallesExamen_DeberiaIgnorarTituloNull() {
        Examen examen = new Examen();
        examen.setId(1L);
        examen.setTitulo("Original");
        ExamenDetalleDTO dto = new ExamenDetalleDTO();

        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));
        when(examenRepository.save(any(Examen.class))).thenReturn(examen);
        when(examenMapper.toDetalleDTO(examen)).thenReturn(dto);

        // pasamos null en el título (falla la primera condición del &&)
        examenService.actualizarDetallesExamen(1L, null, "Nueva Desc");

        assertEquals("Original", examen.getTitulo()); // comprobamos que no ha cambiado
        verify(examenRepository).save(examen);
    }

    @Test
    void actualizarDetallesExamen_DeberiaIgnorarTituloVacio() {
        Examen examen = new Examen();
        examen.setId(1L);
        examen.setTitulo("Original");
        ExamenDetalleDTO dto = new ExamenDetalleDTO();

        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));
        when(examenRepository.save(any(Examen.class))).thenReturn(examen);
        when(examenMapper.toDetalleDTO(examen)).thenReturn(dto);

        examenService.actualizarDetallesExamen(1L, "", "Nueva Desc");

        assertEquals("Original", examen.getTitulo());
        verify(examenRepository).save(examen);
    }

    @Test
    void actualizarDetallesExamen_IgnorarNulosYVacios() {
        //preparamos un examen que ya tiene datos
        Examen examen = new Examen();
        examen.setId(1L);
        examen.setTitulo("Titulo Original");
        examen.setDescripcion("Desc Original");

        ExamenDetalleDTO dto = new ExamenDetalleDTO();

        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));
        when(examenRepository.save(any(Examen.class))).thenReturn(examen);
        when(examenMapper.toDetalleDTO(examen)).thenReturn(dto);

        ExamenDetalleDTO resultado = examenService.actualizarDetallesExamen(1L, "", null);

        assertNotNull(resultado);
        assertEquals("Titulo Original", examen.getTitulo());
        assertEquals("Desc Original", examen.getDescripcion());
        verify(examenRepository).save(examen);
    }
}