package com.jorge.examenes;

import com.jorge.examenes.controller.ExamenController;
import com.jorge.examenes.dto.ExamenDetalleDTO;
import com.jorge.examenes.dto.ExamenGetDTO;
import com.jorge.examenes.services.ExamenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamenControllerTest {

    @Mock
    private ExamenService examenService;

    @InjectMocks
    private ExamenController examenController;

    @Test
    void listarTodos_DeberiaDevolver200YLista() {
        List<ExamenGetDTO> lista = Arrays.asList(new ExamenGetDTO(), new ExamenGetDTO());
        when(examenService.obtenerTodosResumen()).thenReturn(lista);

        ResponseEntity<List<ExamenGetDTO>> response = examenController.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void obtenerPorId_DeberiaDevolver200YExamen() {
        ExamenDetalleDTO dto = new ExamenDetalleDTO();
        when(examenService.obtenerDetallePorId(1L)).thenReturn(dto);

        ResponseEntity<ExamenDetalleDTO> response = examenController.obtenerPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void generarExamen_DeberiaDevolver201YExamenGenerado() {
        ExamenDetalleDTO dto = new ExamenDetalleDTO();
        when(examenService.generarExamenAleatorio(anyString(), anyString(), anyInt())).thenReturn(dto);

        ResponseEntity<ExamenDetalleDTO> response = examenController.generarExamen("Titulo", "Desc", 10);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void actualizarExamen_DeberiaDevolver200YActualizado() {
        ExamenDetalleDTO dto = new ExamenDetalleDTO();
        when(examenService.actualizarDetallesExamen(eq(1L), anyString(), anyString())).thenReturn(dto);

        ResponseEntity<ExamenDetalleDTO> response = examenController.actualizarExamen(1L, "T", "D");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void borrarExamen_DeberiaDevolver200YMensaje() {
        ResponseEntity<String> response = examenController.borrarExamen(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Examen eliminado correctamente.", response.getBody());
        verify(examenService).borrarExamen(1L);
    }

    @Test
    void anadirPreguntas_DeberiaDevolver200() {
        ExamenDetalleDTO dto = new ExamenDetalleDTO();
        List<Long> idsNuevas = Arrays.asList(2L, 3L);
        when(examenService.anadirPreguntas(eq(1L), anyList())).thenReturn(dto);

        ResponseEntity<ExamenDetalleDTO> response = examenController.anadirPreguntas(1L, idsNuevas);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void listarExamenesPaginados_DeberiaDevolver200YPagina() {
        Page<ExamenGetDTO> paginaFalsa = new PageImpl<>(List.of(new ExamenGetDTO()));
        when(examenService.obtenerExamenesPaginados(0, 10, "fecha", "desc")).thenReturn(paginaFalsa);

        ResponseEntity<Page<ExamenGetDTO>> response = examenController.listarExamenesPaginados(0, 10, "fecha", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(examenService).obtenerExamenesPaginados(0, 10, "fecha", "desc");
    }


}