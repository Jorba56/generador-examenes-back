package com.jorge.sprintdef;

import com.jorge.sprintdef.controller.IncidenciasController;
import com.jorge.sprintdef.exceptions.NotFoundException;
import com.jorge.sprintdef.services.IncidenciasService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IncidenciasControllerTest {

    @Mock
    private IncidenciasService incidenciasService;

    @InjectMocks
    private IncidenciasController incidenciasController;

    @Test
    void getAllIncidencias() {
        Incidencia incidencia = new Incidencia();
        List<Incidencia> lista = List.of(incidencia);

        given(incidenciasService.obtenerTodas()).willReturn((lista));

        List<Incidencia> incidencias = incidenciasController.getAllIncidencias();

        assertFalse(incidencias.isEmpty());
        assertEquals(1, incidencias.size());
        verify(incidenciasService).obtenerTodas();
    }

    @Test
    void guardar() {
        Incidencia incidencia = new Incidencia();
        incidenciasController.createIncidencia(incidencia);
        verify(incidenciasService).guardar(incidencia);
    }

    @Test
    void obtenerPorId_Exito() {
        Incidencia incidencia = new Incidencia();
        given(incidenciasService.obtenerPorId(1L)).willReturn((incidencia));

        Incidencia resultado = incidenciasController.getIncidenciaById(1L);

        assertNotNull(resultado);
        verify(incidenciasService).obtenerPorId(1L);
    }

    @Test
    void obtenerPorClase_NoEncontrado() {
        // forzar lista vacia para entrar al if
        given(incidenciasService.obtenerPorClase("123")).willThrow(
                new NotFoundException("No se han encontrado incidencias originadas en la clase: 123")
        );

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> incidenciasController.getIncidenciasByClase("123") // Llamamos al controller, no al service
        );

        assertEquals("No se han encontrado incidencias originadas en la clase: 123", ex.getMessage());
    }

    @Test
    void obtenerPorMetodo_Exito() {
        Incidencia incidencia = new Incidencia();
        given(incidenciasService.obtenerPorMetodo("login")).willReturn(List.of(incidencia));

        List<Incidencia> resultado = incidenciasController.getIncidenciasByMetodo("login");

        assertFalse(resultado.isEmpty());
        verify(incidenciasService).obtenerPorMetodo("login");
    }

    @Test
    void obtenerPorMetodo_NoEncontrado() {
        // forzar lista vacia para entrar al if
        given(incidenciasService.obtenerPorMetodo("metodoInventado")).willThrow(
                new NotFoundException("No se han encontrado incidencias originadas en el método: metodoInventado")
        );

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> incidenciasController.getIncidenciasByMetodo("metodoInventado")
        );

        assertEquals("No se han encontrado incidencias originadas en el método: metodoInventado", ex.getMessage());
    }

    @Test
    void getIncidenciasByUsuario_Exito() {

        Incidencia incidencia = new Incidencia();
        List<Incidencia> listaEsperada = List.of(incidencia);

        given(incidenciasService.obtenerPorUsuario(1L)).willReturn(listaEsperada);

        List<Incidencia> resultado = incidenciasController.getIncidenciasByUsuario(1L);

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        verify(incidenciasService).obtenerPorUsuario(1L); // Verificamos que el controller llamó al service
    }

    @Test
    void getIncidenciasByUsuario_NoEncontrado() {

        given(incidenciasService.obtenerPorUsuario(99L)).willThrow(
                new NotFoundException("No se han encontrado incidencias para el usuario con ID: 99")
        );

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> incidenciasController.getIncidenciasByUsuario(99L)
        );

        assertEquals("No se han encontrado incidencias para el usuario con ID: 99", ex.getMessage());
    }
}
