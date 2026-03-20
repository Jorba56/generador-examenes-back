package com.jorge.examenes;

import com.jorge.examenes.controller.EvaluacionController;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluacionControllerTest {

    @Mock
    private EvaluacionServiceImpl evaluacionService;

    @InjectMocks
    private EvaluacionController evaluacionController;

    @Test
    void deberiaEvaluarExamenYDevolver200OK()  throws BadRequestException{
        // 1. Preparamos los datos de entrada
        Long idExamen = 1L;
        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();

        // 2. Preparamos lo que nos va a devolver el servicio simulado
        EvaluacionResultDTO resultadoEsperado = new EvaluacionResultDTO(1, 0, 0, 10.0);

        // 3. Le decimos al Mockito qué hacer cuando llamen al servicio
        when(evaluacionService.corregirExamen(eq(idExamen), any(ExamenSubmitDTO.class)))
                .thenReturn(resultadoEsperado);

        //
        ResponseEntity<EvaluacionResultDTO> respuesta = evaluacionController.evaluarExamen(idExamen, submitDTO);

        // 5. Comprobamos que devuelve un 200 OK y los datos correctos
        assertNotNull(respuesta);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(10.0, respuesta.getBody().getNotaFinal());
    }
}