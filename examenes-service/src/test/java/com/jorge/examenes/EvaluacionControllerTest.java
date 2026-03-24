package com.jorge.examenes;

import com.jorge.examenes.controller.EvaluacionController;
import com.jorge.examenes.dto.EstadisticasAlumnoDTO;
import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import com.jorge.examenes.dto.EvaluacionResultDTO;
import com.jorge.examenes.dto.ExamenSubmitDTO;
import com.jorge.examenes.exceptions.BadRequestException;
import com.jorge.examenes.services.impl.EvaluacionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluacionControllerTest {

    @Mock
    private EvaluacionServiceImpl evaluacionService;

    @InjectMocks
    private EvaluacionController evaluacionController;

    @Test
    void deberiaEvaluarExamenYDevolver200OK() throws BadRequestException {

        Long idExamen = 1L;
        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();

        EvaluacionResultDTO resultadoEsperado = new EvaluacionResultDTO(1, 0, 0, 10.0);

        when(evaluacionService.corregirExamen(eq(idExamen), any(ExamenSubmitDTO.class)))
                .thenReturn(resultadoEsperado);

        ResponseEntity<EvaluacionResultDTO> respuesta = evaluacionController.evaluarExamen(idExamen, submitDTO);

        assertNotNull(respuesta);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(10.0, respuesta.getBody().getNotaFinal());
    }

    @SuppressWarnings("java:S1130")
    @Test
    void verMisNotas_DeberiaDevolver200YListaDeHistorial() throws Exception {

        List<EvaluacionHistorialDTO> dtosEsperados = Arrays.asList(
                new EvaluacionHistorialDTO(),
                new EvaluacionHistorialDTO()
        );

        when(evaluacionService.obtenerMisNotas()).thenReturn(dtosEsperados);

        ResponseEntity<List<EvaluacionHistorialDTO>> respuesta = evaluacionController.verMisNotas();

        assertNotNull(respuesta);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(2, respuesta.getBody().size());

        verify(evaluacionService).obtenerMisNotas();
    }

    @Test
    void buscarNotasDeAlumno_DeberiaDevolver200YLista() {

        List<EvaluacionHistorialDTO> dtosEsperados = List.of(new EvaluacionHistorialDTO());

        when(evaluacionService.obtenerNotasDeAlumnoEnExamen(1L, "alumno@test.com"))
                .thenReturn(dtosEsperados);

        ResponseEntity<List<EvaluacionHistorialDTO>> respuesta = evaluacionController.buscarNotasDeAlumno(1L, "alumno@test.com");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());

        verify(evaluacionService).obtenerNotasDeAlumnoEnExamen(1L, "alumno@test.com");
    }

    @Test
    void obtenerEstadisticas_CorreoValido_DeberiaDevolver200YEstadisticas() {
        // Arrange (Preparación)
        String correo = "alumno@gmail.com";

        EstadisticasAlumnoDTO estadisticasEsperadas = new EstadisticasAlumnoDTO();
        estadisticasEsperadas.setCorreoAlumno(correo);
        estadisticasEsperadas.setTotalExamenesRealizados(4);
        estadisticasEsperadas.setNotaMedia(7.25);
        estadisticasEsperadas.setExamenesAprobados(3);
        estadisticasEsperadas.setExamenesSuspendidos(1);

        when(evaluacionService.obtenerEstadisticasAlumno(correo))
                .thenReturn(estadisticasEsperadas);

        // Act (Ejecución llamando directamente al método de Java)
        ResponseEntity<EstadisticasAlumnoDTO> respuesta = evaluacionController.obtenerEstadisticas(correo);

        // Assert (Verificaciones HTTP y de contenido)
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        // Verificamos que los datos del cuerpo son los que tocan
        assertEquals(correo, respuesta.getBody().getCorreoAlumno());
        assertEquals(4, respuesta.getBody().getTotalExamenesRealizados());
        assertEquals(7.25, respuesta.getBody().getNotaMedia());
        assertEquals(3, respuesta.getBody().getExamenesAprobados());
        assertEquals(1, respuesta.getBody().getExamenesSuspendidos());

        // Verificamos que el controlador ha llamado al servicio correcto
        verify(evaluacionService).obtenerEstadisticasAlumno(correo);
    }

    @Test
    void listarHistorialAlumno_DeberiaDevolver200YListaOrdenada() {
        List<EvaluacionHistorialDTO> dtosEsperados = List.of(new EvaluacionHistorialDTO());
        when(evaluacionService.obtenerHistorialAlumno("alumno@test.com", "fecha", "desc"))
                .thenReturn(dtosEsperados);

        ResponseEntity<List<EvaluacionHistorialDTO>> respuesta = evaluacionController.listarHistorialAlumno("alumno@test.com", "fecha", "desc");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
    }

    @Test
    void listarNotasDeExamen_DeberiaDevolver200YListaOrdenada() {
        List<EvaluacionHistorialDTO> dtosEsperados = List.of(new EvaluacionHistorialDTO());
        when(evaluacionService.obtenerNotasExamen(1L, "nota", "desc"))
                .thenReturn(dtosEsperados);

        ResponseEntity<List<EvaluacionHistorialDTO>> respuesta = evaluacionController.listarNotasDeExamen(1L, "nota", "desc");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
    }

    @Test
    void exportarNotasAExcel_DeberiaConfigurarCabecerasYDescargarArchivo() throws Exception {
        // Simulamos la respuesta HTTP y su flujo de salida para que el Excel no dé NullPointer
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        List<EvaluacionHistorialDTO> dtos = List.of(new EvaluacionHistorialDTO());
        when(evaluacionService.obtenerNotasExamen(1L, "nota", "desc")).thenReturn(dtos);

        // Llamamos al método
        evaluacionController.exportarNotasAExcel(1L, response);

        // Verificamos que se han inyectado las cabeceras correctas de descarga de Excel
        verify(response).setContentType("application/octet-stream");
        verify(response).setHeader(eq("Content-Disposition"), anyString());
    }
}