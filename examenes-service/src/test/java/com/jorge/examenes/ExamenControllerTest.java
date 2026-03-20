package com.jorge.examenes;

import com.jorge.examenes.controller.ExamenController;
import com.jorge.examenes.dto.ExamenDetalleDTO;
import com.jorge.examenes.exceptions.BadRequestException;
import com.jorge.examenes.mapping.ExamenMapper;
import com.jorge.examenes.services.impl.ExamenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamenControllerTest {

    @Mock
    private ExamenServiceImpl examenService;

    @Mock
    private ExamenMapper examenMap;

    @InjectMocks
    private ExamenController examenController;

    @Test
    void generarExamen_DebeRetornarExamenCreado()  throws BadRequestException {
        // 1. Preparamos el DTO "falso" CON los datos rellenados
        ExamenDetalleDTO dtoMock = new ExamenDetalleDTO();
        dtoMock.setId(1L);
        dtoMock.setTitulo("Test F1");
        dtoMock.setDescripcion("Desc");

        // 2. Le decimos al mock del SERVICIO que devuelva nuestro DTO
        // (Borramos el mock del mapper porque el controlador no lo usa)
        when(examenService.generarExamenAleatorio("Test F1", "Desc", 10)).thenReturn(dtoMock);

        // OJO: El controlador devuelve un ResponseEntity, lo guardamos ahí
        ResponseEntity<ExamenDetalleDTO> response = examenController.generarExamen("Test F1", "Desc", 10);

        // 4. Aserciones (Comprobamos el Status 201 y que el body tiene el título correcto)
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test F1", response.getBody().getTitulo()); // Accedemos al DTO con getBody()

        // 5. Verificamos que se llamó al servicio correctamente
        verify(examenService, times(1)).generarExamenAleatorio("Test F1", "Desc", 10);
    }
}