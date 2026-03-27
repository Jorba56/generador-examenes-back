package com.jorge.examenes;

import com.jorge.examenes.dto.EstadisticasAlumnoDTO;
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
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    @Test
    void corregirExamen_DeberiaLanzarBadRequestSiAlcanzaLimiteIntentos() {
        Examen examen = new Examen();
        examen.setId(1L);
        examen.setPreguntas(new ArrayList<>());

        when(examenRepository.findById(1L)).thenReturn(Optional.of(examen));

        when(evaluacionRepository.countByIdExamenAndCorreoUsuario(1L, "alumno@test.com")).thenReturn(2);

        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();
        submitDTO.setRespuestas(new HashMap<>());

        assertThrows(BadRequestException.class, () -> {
            evaluacionService.corregirExamen(1L, submitDTO);
        });

        verify(evaluacionRepository, never()).save(any(Evaluacion.class));
    }

    @Test
    void obtenerNotasDeAlumnoEnExamen_DeberiaDevolverHistorialParaElProfesor() {

        List<Evaluacion> evaluaciones = List.of(new Evaluacion());
        List<EvaluacionHistorialDTO> dtos = List.of(new EvaluacionHistorialDTO());

        when(evaluacionRepository.findByIdExamenAndCorreoUsuarioOrderByFechaDesc(1L, "alumno@test.com"))
                .thenReturn(evaluaciones);
        when(evaluacionMapper.toHistorialDTOList(evaluaciones)).thenReturn(dtos);

        List<EvaluacionHistorialDTO> resultado = evaluacionService.obtenerNotasDeAlumnoEnExamen(1L, "alumno@test.com");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(evaluacionRepository).findByIdExamenAndCorreoUsuarioOrderByFechaDesc(1L, "alumno@test.com");
    }

    @Test
    void obtenerEstadisticasAlumno_ConExamenes_DeberiaCalcularMediaYAprobados() {
        String correo = "alumno@gmail.com";

        // tres exámenes ficticios: 4.0, 8.0 y 6.0
        // la media debería ser (4 + 8 + 6) / 3 = 6.0. debería haber 2 aprobados y 1 suspenso.
        Evaluacion eval1 = new Evaluacion();
        eval1.setNota(4.0);

        Evaluacion eval2 = new Evaluacion();
        eval2.setNota(8.0);

        Evaluacion eval3 = new Evaluacion();
        eval3.setNota(6.0);

        List<Evaluacion> listaExamenes = new ArrayList<>();
        listaExamenes.add(eval1);
        listaExamenes.add(eval2);
        listaExamenes.add(eval3);

        given(evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(correo)).willReturn(listaExamenes);

        EstadisticasAlumnoDTO resultado = evaluacionService.obtenerEstadisticasAlumno(correo);

        assertNotNull(resultado);
        assertEquals(correo, resultado.getCorreoAlumno());
        assertEquals(3, resultado.getTotalExamenesRealizados());
        assertEquals(6.0, resultado.getNotaMedia());
        assertEquals(2, resultado.getExamenesAprobados());
        assertEquals(1, resultado.getExamenesSuspendidos());
    }

    @Test
    void obtenerEstadisticasAlumno_SinExamenes_DeberiaLanzarNotFound() {
        String correo = "nuevo@gmail.com";

        given(evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(correo)).willReturn(new ArrayList<>());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> evaluacionService.obtenerEstadisticasAlumno(correo)
        );

        assertEquals("El alumno no ha realizado ningún examen aún.", ex.getMessage());
        verify(evaluacionRepository).findByCorreoUsuarioOrderByFechaDesc(correo);
    }

    @Test
    void obtenerEstadisticas_CuandoListaEsNula_DeberiaLanzarNotFound() {
        String correo = "nuevo@test.com";

        when(evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(correo)).thenReturn(null);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> evaluacionService.obtenerEstadisticasAlumno(correo)
        );

        assertEquals("El alumno no ha realizado ningún examen aún.", ex.getMessage());
        verify(evaluacionRepository).findByCorreoUsuarioOrderByFechaDesc(correo);
    }

    @Test
    void obtenerEstadisticas_CuandoListaEstaVacia_DeberiaLanzarNotFound() {
        String correo = "nuevo@test.com";

        // simulamos que el repositorio devuelve una lista vacía
        when(evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(correo))
                .thenReturn(java.util.Collections.emptyList());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> evaluacionService.obtenerEstadisticasAlumno(correo)
        );

        assertEquals("El alumno no ha realizado ningún examen aún.", ex.getMessage());
        verify(evaluacionRepository).findByCorreoUsuarioOrderByFechaDesc(correo);
    }

    @Test
    void obtenerHistorialAlumno_CoberturaTotalDeAliasDelSwitch() {
        List<Evaluacion> evaluaciones = List.of(new Evaluacion());
        List<EvaluacionHistorialDTO> dtos = List.of(new EvaluacionHistorialDTO());

        when(evaluacionRepository.findByCorreoUsuario(eq("alumno@test.com"), any(Sort.class)))
                .thenReturn(evaluaciones);
        when(evaluacionMapper.toHistorialDTOList(evaluaciones)).thenReturn(dtos);

        // forzamos pasar por todas las ramas posibles del switch para el 100% de branch coverage
        evaluacionService.obtenerHistorialAlumno("alumno@test.com", "nota", "asc");
        evaluacionService.obtenerHistorialAlumno("alumno@test.com", "fecha", "desc");
        evaluacionService.obtenerHistorialAlumno("alumno@test.com", "correo", "asc");
        evaluacionService.obtenerHistorialAlumno("alumno@test.com", "inventado", "desc"); // default

        verify(evaluacionRepository, times(4)).findByCorreoUsuario(eq("alumno@test.com"), any(Sort.class));
    }

    @Test
    void obtenerNotasExamen_DeberiaDevolverRankingOrdenado() {
        List<Evaluacion> evaluaciones = List.of(new Evaluacion());
        List<EvaluacionHistorialDTO> dtos = List.of(new EvaluacionHistorialDTO());

        when(evaluacionRepository.findByIdExamen(eq(1L), any(Sort.class))).thenReturn(evaluaciones);
        when(evaluacionMapper.toHistorialDTOList(evaluaciones)).thenReturn(dtos);

        List<EvaluacionHistorialDTO> resultado = evaluacionService.obtenerNotasExamen(1L, "nota", "desc");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(evaluacionRepository).findByIdExamen(eq(1L), any(Sort.class));
    }

    @Test
    void obtenerNotasExamen_OrdenacionAscendente_CoberturaTernario() {
        List<Evaluacion> evaluaciones = List.of(new Evaluacion());
        List<EvaluacionHistorialDTO> dtos = List.of(new EvaluacionHistorialDTO());

        when(evaluacionRepository.findByIdExamen(eq(1L), any(Sort.class))).thenReturn(evaluaciones);
        when(evaluacionMapper.toHistorialDTOList(evaluaciones)).thenReturn(dtos);

        // forzamos el "asc" para cubrir la rama que faltaba del operador ternario
        List<EvaluacionHistorialDTO> resultado = evaluacionService.obtenerNotasExamen(1L, "nota", "asc");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(evaluacionRepository).findByIdExamen(eq(1L), any(Sort.class));
    }

    @Test
    void obtenerMisNotas_DeberiaLanzarNotFoundSiNoHayExamenes() {
        // Simulamos que la base de datos devuelve una lista vacía
        when(evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc("alumno@test.com"))
                .thenReturn(new ArrayList<>());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                evaluacionService.obtenerMisNotas()
        );

        assertEquals("No has realizado ningún examen aún.", ex.getMessage());
        verify(evaluacionRepository).findByCorreoUsuarioOrderByFechaDesc("alumno@test.com");
    }

    @Test
    void obtenerNotasDeAlumnoEnExamen_DeberiaLanzarNotFoundSiNoHayNotas() {
        when(evaluacionRepository.findByIdExamenAndCorreoUsuarioOrderByFechaDesc(1L, "alumno@test.com"))
                .thenReturn(new ArrayList<>());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                evaluacionService.obtenerNotasDeAlumnoEnExamen(1L, "alumno@test.com")
        );

        assertEquals("El usuario seleccionado no ha hecho este examen.", ex.getMessage());
    }

    @Test
    void obtenerHistorialAlumno_DeberiaLanzarNotFoundSiNoHayHistorial() {
        when(evaluacionRepository.findByCorreoUsuario(eq("alumno@test.com"), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                evaluacionService.obtenerHistorialAlumno("alumno@test.com", "nota", "asc")
        );

        assertEquals("El usuario seleccionado no ha hecho este examen.", ex.getMessage());
    }

    @Test
    void obtenerNotasExamen_DeberiaLanzarNotFoundSiNadieLoHaHecho() {
        when(evaluacionRepository.findByIdExamen(eq(1L), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                evaluacionService.obtenerNotasExamen(1L, "nota", "asc")
        );

        assertEquals("El examen seleccionado no ha sido realizado por ningún usuario aún.", ex.getMessage());
    }
}