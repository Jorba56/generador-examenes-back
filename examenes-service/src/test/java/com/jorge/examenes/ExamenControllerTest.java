package com.jorge.examenes;

import com.jorge.examenes.controller.ExamenController;
import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.services.impl.ExamenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamenControllerTest {

    @Mock
    private ExamenServiceImpl examenService;

    @InjectMocks
    private ExamenController examenController;

    @Test
    void generarExamen_DebeRetornarExamenCreado() {
        Examen examenMock = new Examen();
        examenMock.setId(1L);
        examenMock.setTitulo("Test F1");

        when(examenService.generarExamenAleatorio("Test F1", "Desc", 10)).thenReturn(examenMock);

        ResponseEntity<Examen> response = examenController.generarExamen("Test F1", "Desc", 10);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals("Test F1", response.getBody().getTitulo());
        verify(examenService, times(1)).generarExamenAleatorio("Test F1", "Desc", 10);
    }
}