package com.jorge.examenes.services.impl;

import com.jorge.examenes.dto.EstadisticasAlumnoDTO;
import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import com.jorge.examenes.dto.EvaluacionResultDTO;
import com.jorge.examenes.dto.ExamenSubmitDTO;
import com.jorge.examenes.entity.Evaluacion;
import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.entity.RespuestaUsuario;
import com.jorge.examenes.exceptions.BadRequestException;
import com.jorge.examenes.exceptions.NotFoundException;
import com.jorge.examenes.mapping.EvaluacionMapper;
import com.jorge.examenes.repository.EvaluacionRepository;
import com.jorge.examenes.repository.ExamenRepository;
import com.jorge.examenes.repository.RespuestaUsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluacionServiceImplTest {

    @Mock
    private EvaluacionRepository evaluacionRepository;

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private EvaluacionMapper evaluacionMapper;

    @Mock
    private RespuestaUsuarioRepository respuestaUsuarioRepository;

    @InjectMocks
    private EvaluacionServiceImpl evaluacionService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private final String CORREO_TEST = "alumno@test.com";

    @BeforeEach
    void setUp() {
        // Limpiar el contexto de seguridad antes de cada test por precaución
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(CORREO_TEST);
    }

    // ==========================================
    // TESTS PARA: corregirExamen
    // ==========================================

    @Test
    void deberia_corregirExamen_cuando_datosSonValidos() throws BadRequestException {
        // Arrange
        Long idExamen = 1L;
        Examen examen = new Examen();
        examen.setId(idExamen);
        
        Pregunta p1 = new Pregunta(); p1.setId(10L); p1.setCorrecta("A");
        Pregunta p2 = new Pregunta(); p2.setId(20L); p2.setCorrecta("B");
        Pregunta p3 = new Pregunta(); p3.setId(30L); p3.setCorrecta("C");
        examen.setPreguntas(Arrays.asList(p1, p2, p3));

        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();
        Map<Integer, String> respuestas = new HashMap<>();
        respuestas.put(10, "A"); // Acierto
        respuestas.put(20, "C"); // Fallo
        respuestas.put(30, "");  // En blanco
        submitDTO.setRespuestas(respuestas);

        when(examenRepository.findById(idExamen)).thenReturn(Optional.of(examen));
        
        mockSecurityContext();
        when(evaluacionRepository.countByIdExamenAndCorreoUsuario(idExamen, CORREO_TEST)).thenReturn(0);

        Evaluacion evaluacionGuardada = new Evaluacion();
        evaluacionGuardada.setId(100L);
        when(evaluacionRepository.save(any(Evaluacion.class))).thenReturn(evaluacionGuardada);

        // Act
        EvaluacionResultDTO result = evaluacionService.corregirExamen(idExamen, submitDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getAciertos());
        assertEquals(1, result.getFallos());
        assertEquals(1, result.getEnBlanco());
        assertEquals(3.33, result.getNotaFinal()); // (1/3) * 10 = 3.333... redondeado a 3.33

        verify(evaluacionRepository).save(any(Evaluacion.class));
        verify(respuestaUsuarioRepository, times(3)).save(any(RespuestaUsuario.class));
    }

    @Test
    void deberia_manejarRespuestasNulas_cuando_submitDTONoTieneRespuestas() throws BadRequestException {
        // Arrange
        Long idExamen = 1L;
        Examen examen = new Examen();
        examen.setId(idExamen);
        Pregunta p1 = new Pregunta(); p1.setId(10L); p1.setCorrecta("A");
        examen.setPreguntas(List.of(p1));

        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();
        submitDTO.setRespuestas(null); // Caso límite: respuestas nulas

        when(examenRepository.findById(idExamen)).thenReturn(Optional.of(examen));
        mockSecurityContext();
        when(evaluacionRepository.countByIdExamenAndCorreoUsuario(idExamen, CORREO_TEST)).thenReturn(0);
        
        Evaluacion evaluacionGuardada = new Evaluacion();
        evaluacionGuardada.setId(100L);
        when(evaluacionRepository.save(any(Evaluacion.class))).thenReturn(evaluacionGuardada);

        // Act
        EvaluacionResultDTO result = evaluacionService.corregirExamen(idExamen, submitDTO);

        // Assert
        assertEquals(0, result.getAciertos());
        assertEquals(0, result.getFallos());
        assertEquals(1, result.getEnBlanco());
        assertEquals(0.0, result.getNotaFinal());
    }

    @Test
    void deberia_lanzarNotFoundException_cuando_examenNoExiste() {
        // Arrange
        Long idExamen = 99L;
        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();
        when(examenRepository.findById(idExamen)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            evaluacionService.corregirExamen(idExamen, submitDTO)
        );
        assertEquals("Examen no encontrado con ID: 99", exception.getMessage());
    }

    @Test
    void deberia_lanzarBadRequestException_cuando_noHayUsuarioLogueado() {
        // Arrange
        Long idExamen = 1L;
        Examen examen = new Examen();
        examen.setId(idExamen);
        examen.setPreguntas(Collections.emptyList());
        
        when(examenRepository.findById(idExamen)).thenReturn(Optional.of(examen));
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null); // Sin autenticación

        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> 
            evaluacionService.corregirExamen(idExamen, submitDTO)
        );
        assertEquals("El contexto de seguridad está vacío. No se puede identificar al usuario.", exception.getMessage());
    }

    @Test
    void deberia_lanzarBadRequestException_cuando_maximoIntentosAlcanzado() {
        // Arrange
        Long idExamen = 1L;
        Examen examen = new Examen();
        examen.setId(idExamen);
        examen.setPreguntas(Collections.emptyList());
        
        when(examenRepository.findById(idExamen)).thenReturn(Optional.of(examen));
        mockSecurityContext();
        
        when(evaluacionRepository.countByIdExamenAndCorreoUsuario(idExamen, CORREO_TEST)).thenReturn(2); // Límite alcanzado

        ExamenSubmitDTO submitDTO = new ExamenSubmitDTO();

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> 
            evaluacionService.corregirExamen(idExamen, submitDTO)
        );
        assertEquals("Has alcanzado el número máximo de intentos (2) para este examen.", exception.getMessage());
    }

    // ==========================================
    // TESTS PARA: obtenerMisNotas
    // ==========================================

    @Test
    void deberia_obtenerMisNotas_cuando_usuarioLogueadoYTieneNotas() throws BadRequestException {
        // Arrange
        mockSecurityContext();
        List<Evaluacion> evaluaciones = List.of(new Evaluacion(), new Evaluacion());
        List<EvaluacionHistorialDTO> dtos = List.of(new EvaluacionHistorialDTO(), new EvaluacionHistorialDTO());

        when(evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(CORREO_TEST)).thenReturn(evaluaciones);
        when(evaluacionMapper.toHistorialDTOList(evaluaciones)).thenReturn(dtos);

        // Act
        List<EvaluacionHistorialDTO> result = evaluacionService.obtenerMisNotas();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(evaluacionRepository).findByCorreoUsuarioOrderByFechaDesc(CORREO_TEST);
    }

    @Test
    void deberia_lanzarBadRequestException_cuando_obtenerMisNotasSinUsuarioLogueado() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> 
            evaluacionService.obtenerMisNotas()
        );
        assertEquals("No hay un usuario logueado en el sistema.", exception.getMessage());
    }

    @Test
    void deberia_lanzarNotFoundException_cuando_obtenerMisNotasYNoHayNotas() {
        // Arrange
        mockSecurityContext();
        when(evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(CORREO_TEST)).thenReturn(Collections.emptyList());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            evaluacionService.obtenerMisNotas()
        );
        assertEquals("No has realizado ningún examen aún.", exception.getMessage());
    }

    // ==========================================
    // TESTS PARA: obtenerNotasDeAlumnoEnExamen
    // ==========================================

    @Test
    void deberia_obtenerNotasDeAlumnoEnExamen_cuando_existenNotas() {
        // Arrange
        Long idExamen = 1L;
        List<Evaluacion> evaluaciones = List.of(new Evaluacion());
        List<EvaluacionHistorialDTO> dtos = List.of(new EvaluacionHistorialDTO());

        when(evaluacionRepository.findByIdExamenAndCorreoUsuarioOrderByFechaDesc(idExamen, CORREO_TEST)).thenReturn(evaluaciones);
        when(evaluacionMapper.toHistorialDTOList(evaluaciones)).thenReturn(dtos);

        // Act
        List<EvaluacionHistorialDTO> result = evaluacionService.obtenerNotasDeAlumnoEnExamen(idExamen, CORREO_TEST);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void deberia_lanzarNotFoundException_cuando_noExistenNotasDeAlumnoEnExamen() {
        // Arrange
        Long idExamen = 1L;
        when(evaluacionRepository.findByIdExamenAndCorreoUsuarioOrderByFechaDesc(idExamen, CORREO_TEST)).thenReturn(Collections.emptyList());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            evaluacionService.obtenerNotasDeAlumnoEnExamen(idExamen, CORREO_TEST)
        );
        assertEquals("El usuario seleccionado no ha hecho este examen.", exception.getMessage());
    }

    // ==========================================
    // TESTS PARA: obtenerEstadisticasAlumno
    // ==========================================

    @Test
    void deberia_obtenerEstadisticasAlumno_cuando_tieneEvaluaciones() {
        // Arrange
        Evaluacion e1 = new Evaluacion(); e1.setNota(4.0); // Suspenso
        Evaluacion e2 = new Evaluacion(); e2.setNota(6.0); // Aprobado
        Evaluacion e3 = new Evaluacion(); e3.setNota(10.0); // Aprobado
        List<Evaluacion> evaluaciones = Arrays.asList(e1, e2, e3);

        when(evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(CORREO_TEST)).thenReturn(evaluaciones);

        // Act
        EstadisticasAlumnoDTO result = evaluacionService.obtenerEstadisticasAlumno(CORREO_TEST);

        // Assert
        assertNotNull(result);
        assertEquals(CORREO_TEST, result.getCorreoAlumno());
        assertEquals(3, result.getTotalExamenesRealizados());
        assertEquals(2, result.getExamenesAprobados());
        assertEquals(1, result.getExamenesSuspendidos());
        assertEquals(6.67, result.getNotaMedia()); // (4+6+10)/3 = 6.666... -> 6.67
    }

    @Test
    void deberia_lanzarNotFoundException_cuando_obtenerEstadisticasYNoHayEvaluaciones() {
        // Arrange
        when(evaluacionRepository.findByCorreoUsuarioOrderByFechaDesc(CORREO_TEST)).thenReturn(Collections.emptyList());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            evaluacionService.obtenerEstadisticasAlumno(CORREO_TEST)
        );
        assertEquals("El alumno no ha realizado ningún examen aún.", exception.getMessage());
    }

    // ==========================================
    // TESTS PARA: obtenerHistorialAlumno
    // ==========================================

    @Test
    void deberia_obtenerHistorialAlumno_cuando_existenEvaluaciones() {
        // Arrange
        String sortBy = "nota";
        String sortDir = "desc";
        List<Evaluacion> evaluaciones = List.of(new Evaluacion());
        List<EvaluacionHistorialDTO> dtos = List.of(new EvaluacionHistorialDTO());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(evaluacionRepository.findByCorreoUsuario(eq(CORREO_TEST), sortCaptor.capture())).thenReturn(evaluaciones);
        when(evaluacionMapper.toHistorialDTOList(evaluaciones)).thenReturn(dtos);

        // Act
        List<EvaluacionHistorialDTO> result = evaluacionService.obtenerHistorialAlumno(CORREO_TEST, sortBy, sortDir);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Sort capturedSort = sortCaptor.getValue();
        Sort.Order order = capturedSort.getOrderFor("nota");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void deberia_lanzarNotFoundException_cuando_obtenerHistorialAlumnoYNoHayEvaluaciones() {
        // Arrange
        when(evaluacionRepository.findByCorreoUsuario(eq(CORREO_TEST), any(Sort.class))).thenReturn(Collections.emptyList());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            evaluacionService.obtenerHistorialAlumno(CORREO_TEST, "fecha", "asc")
        );
        assertEquals("El usuario seleccionado no ha hecho este examen.", exception.getMessage());
    }

    // ==========================================
    // TESTS PARA: obtenerNotasExamen
    // ==========================================

    @Test
    void deberia_obtenerNotasExamen_cuando_existenEvaluaciones() {
        // Arrange
        Long idExamen = 1L;
        String sortBy = "fecha";
        String sortDir = "asc";
        List<Evaluacion> evaluaciones = List.of(new Evaluacion());
        List<EvaluacionHistorialDTO> dtos = List.of(new EvaluacionHistorialDTO());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(evaluacionRepository.findByIdExamen(eq(idExamen), sortCaptor.capture())).thenReturn(evaluaciones);
        when(evaluacionMapper.toHistorialDTOList(evaluaciones)).thenReturn(dtos);

        // Act
        List<EvaluacionHistorialDTO> result = evaluacionService.obtenerNotasExamen(idExamen, sortBy, sortDir);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Sort capturedSort = sortCaptor.getValue();
        Sort.Order order = capturedSort.getOrderFor("fecha");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void deberia_lanzarNotFoundException_cuando_obtenerNotasExamenYNoHayEvaluaciones() {
        // Arrange
        Long idExamen = 1L;
        when(evaluacionRepository.findByIdExamen(eq(idExamen), any(Sort.class))).thenReturn(Collections.emptyList());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            evaluacionService.obtenerNotasExamen(idExamen, "correo", "desc")
        );
        assertEquals("El examen seleccionado no ha sido realizado por ningún usuario aún.", exception.getMessage());
    }
}