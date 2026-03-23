package com.jorge.examenes;

import com.jorge.examenes.controller.EvaluacionController;
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
    void deberiaEvaluarExamenYDevolver200OK()  throws BadRequestException{

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
}