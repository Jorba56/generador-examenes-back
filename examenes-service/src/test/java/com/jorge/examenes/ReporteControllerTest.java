package com.jorge.examenes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jorge.examenes.controller.ReporteController;
import com.jorge.examenes.services.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    @Mock
    private ReporteService reporteService;

    @InjectMocks
    private ReporteController reporteController;

    private byte[] mockPdfBytes;

    @BeforeEach
    void setUp() {
        mockPdfBytes = "contenido-falso-pdf".getBytes();
    }

    @Test
    void deberia_retornarPdfYStatusOk_cuando_idEsValido() throws Exception {
        Long idEvaluacion = 1L;
        when(reporteService.generarReporteExamen(idEvaluacion)).thenReturn(mockPdfBytes);

        ResponseEntity<byte[]> response = reporteController.descargarReporte(idEvaluacion);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(mockPdfBytes, response.getBody());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("reporte_examen_1.pdf"));
        
        verify(reporteService, times(1)).generarReporteExamen(idEvaluacion);
    }

    @Test
    void deberia_retornarStatus500_cuando_servicioLanzaExcepcionAlGenerarReporteIndividual() throws Exception {
        Long idEvaluacion = 99L;
        when(reporteService.generarReporteExamen(idEvaluacion)).thenThrow(new RuntimeException("Error de conexión con BIRT"));

        ResponseEntity<byte[]> response = reporteController.descargarReporte(idEvaluacion);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(reporteService, times(1)).generarReporteExamen(idEvaluacion);
    }

    @Test
    void deberia_retornarStatus500_cuando_idEsNulo() throws Exception {
        when(reporteService.generarReporteExamen(null)).thenThrow(new IllegalArgumentException("ID no puede ser nulo"));

        ResponseEntity<byte[]> response = reporteController.descargarReporte(null);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(reporteService, times(1)).generarReporteExamen(null);
    }

    @Test
    void deberia_retornarPdfYStatusOk_cuando_seSolicitaReporteGeneral() throws Exception {
        when(reporteService.generarReporteGeneralExamenes()).thenReturn(mockPdfBytes);

        ResponseEntity<byte[]> response = reporteController.descargarReporteGeneral();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(mockPdfBytes, response.getBody());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("Listado_General_Examenes.pdf"));
        
        verify(reporteService, times(1)).generarReporteGeneralExamenes();
    }

    @Test
    void deberia_retornarStatus500_cuando_servicioLanzaExcepcionAlGenerarReporteGeneral() throws Exception {
        when(reporteService.generarReporteGeneralExamenes()).thenThrow(new Exception("Error interno generando reporte general"));

        ResponseEntity<byte[]> response = reporteController.descargarReporteGeneral();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(reporteService, times(1)).generarReporteGeneralExamenes();
    }
}