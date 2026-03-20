package com.jorge.examenes;

import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import com.jorge.examenes.dto.EvaluacionResultDTO;
import com.jorge.examenes.dto.ExamenSubmitDTO;
import com.jorge.examenes.entity.Evaluacion;
import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.exceptions.BadRequestException;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.mapping.EvaluacionMapper;
import com.jorge.examenes.repository.EvaluacionRepository;
import com.jorge.examenes.repository.ExamenRepository;
import com.jorge.examenes.services.impl.EvaluacionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluacionServiceImplTest {

    @Mock
    private EvaluacionRepository evaluacionRepository;

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private EvaluacionMapper evaluacionMapper;

    @InjectMocks
    private EvaluacionServiceImpl evaluacionService;

    @BeforeEach
    void setUp() {
        // simulamos un usuario logueado en el contexto de seguridad de spring
        Authentication authentication = mock(Authentication.class);

        // añadimos lenient() para que mockito no se queje si un test no llega a usar esto
        lenient().when(authentication.getName()).thenReturn("alumno@test.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
    @AfterEach
    void tearDown() {
        // limpiamos el contexto de seguridad después de cada test
        SecurityContextHolder.clearContext();
    }
    @SuppressWarnings("java:S1130")
    @Test
    void deberiaCorregirExamenCorrectamente() throws BadRequestException {
        // preparamos los datos simulados (Un examen con 3 preguntas)
        Examen examen = new Examen();
        examen.setId(1L);
        List<Pregunta> preguntas = new ArrayList<>();

        Pregunta p1 = new Pregunta(); p1.setId(10L); p1.setCorrecta("A"); preguntas.add(p1);
        Pregunta p2 = new Pregunta(); p2.setId(11L); p2.setCorrecta("B"); preguntas.add(p2);
        Pregunta p3 = new Pregunta(); p3.setId(12L); p3.setCorrecta("C"); preguntas.add(p3);
        examen.setPreguntas(preguntas);

        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));

        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();
        Map<Integer, String> respuestas = new HashMap<>();
        respuestas.put(1, "A"); // acierto
        respuestas.put(2, "C"); // fallo (era la b)
        // la 3 la dejamos sin enviar (en bl
        submitDTO.setRespuestas(respuestas);

        EvaluacionResultDTO resultado = evaluacionService.corregirExamen(1L, submitDTO);

        assertNotNull(resultado);
        assertEquals(1, resultado.getAciertos());
        assertEquals(1, resultado.getFallos());
        assertEquals(1, resultado.getEnBlanco());
        assertEquals(3.33, resultado.getNotaFinal());

        verify(evaluacionRepository, times(1)).save(any(Evaluacion.class));
    }

    @Test
    void corregirExamen_DeberiaContarRespuestasNulasOVaciasComoEnBlanco() throws Exception {

        Examen examen = new Examen();
        examen.setId(1L);
        Pregunta p1 = new Pregunta(); p1.setId(10L); p1.setCorrecta("A");
        Pregunta p2 = new Pregunta(); p2.setId(11L); p2.setCorrecta("B");
        examen.setPreguntas(Arrays.asList(p1, p2));

        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));

        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();
        Map<Integer, String> respuestas = new HashMap<>();

        //la pregunta 1 no la metemos en el map. al hacer get(1) devolverá 'null'
        //la pregunta 2 la metemos como espacios en blanco. cumplirá el 'trim().isempty()'
        respuestas.put(2, "   ");
        submitDTO.setRespuestas(respuestas);


        EvaluacionResultDTO resultado = evaluacionService.corregirExamen(1L, submitDTO);

        assertNotNull(resultado);
        assertEquals(2, resultado.getEnBlanco());
        assertEquals(0, resultado.getAciertos());
        assertEquals(0, resultado.getFallos());
        assertEquals(0.0, resultado.getNotaFinal());
    }

    @Test
    void deberiaLanzarNotFoundExceptionSiExamenNoExiste() {
        when(examenRepository.findById(99L)).thenReturn(Optional.empty());

        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();

        assertThrows(NotFoundException.class, () -> {
            evaluacionService.corregirExamen(99L, submitDTO);
        });

        verify(evaluacionRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarBadRequestExceptionSiNoHayUsuarioLogueado() {
        // vaciamos el contexto de seguridad a propósito para este test
        SecurityContextHolder.clearContext();

        Examen examen = new Examen();
        examen.setId(1L);
        examen.setPreguntas(new ArrayList<>());
        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));

        assertThrows(BadRequestException.class, () -> {
            evaluacionService.corregirExamen(1L, new ExamenSubmitDTO());
        });
    }

    @Test
    void obtenerMisNotas_DeberiaDevolverHistorial() throws Exception {

        List<Evaluacion> misEvaluaciones = Arrays.asList(new Evaluacion(), new Evaluacion());
        List<EvaluacionHistorialDTO> dtosEsperados = Arrays.asList(new EvaluacionHistorialDTO(), new EvaluacionHistorialDTO());


        when(evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc("alumno@test.com"))
                .thenReturn(misEvaluaciones);
        when(evaluacionMapper.toHistorialDTOList(misEvaluaciones))
                .thenReturn(dtosEsperados);


        List<EvaluacionHistorialDTO> resultado = evaluacionService.obtenerMisNotas();


        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(evaluacionRepository).findByCorreoUsuarioOrderByFechaDesc("alumno@test.com");
        verify(evaluacionMapper).toHistorialDTOList(misEvaluaciones);
    }

    @Test
    void obtenerMisNotas_DeberiaLanzarBadRequestSiNoHayUsuario() {

        SecurityContextHolder.clearContext();

        assertThrows(com.jorge.examenes.exceptions.BadRequestException.class, () ->
                evaluacionService.obtenerMisNotas()
        );

        verify(evaluacionRepository, never()).findByCorreoUsuarioOrderByFechaDesc(anyString());
    }
}