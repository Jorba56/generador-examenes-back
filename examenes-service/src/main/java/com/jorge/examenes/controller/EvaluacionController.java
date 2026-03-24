package com.jorge.examenes.controller;

import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import com.jorge.examenes.dto.EvaluacionResultDTO;
import com.jorge.examenes.dto.ExamenSubmitDTO;
import com.jorge.examenes.dto.EstadisticasAlumnoDTO;
import com.jorge.examenes.exceptions.BadRequestException;
import com.jorge.examenes.services.impl.EvaluacionServiceImpl;
import com.jorge.examenes.utils.EvaluacionExcelExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/evaluaciones")
@Tag(name = "Evaluaciones", description = "Endpoints para la corrección automática y registro de notas")
public class EvaluacionController {

    private final EvaluacionServiceImpl evaluacionService;

    public EvaluacionController(EvaluacionServiceImpl evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    @Operation(summary = "Realizar/Corregir un examen",
            description = "Recibe las respuestas de un alumno, calcula la nota en base a las respuestas correctas y guarda la evaluación en el historial. El alumno se extrae automáticamente del token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Examen corregido y nota calculada con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos de entrega inválidos o incompletos"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para realizar exámenes"),
            @ApiResponse(responseCode = "404", description = "El examen que intentas realizar no existe")
    })
    @PreAuthorize("hasAnyAuthority('ALUMNO', 'ADMIN')")
    @PostMapping("/{id}")
    public ResponseEntity<EvaluacionResultDTO> evaluarExamen(
            @PathVariable Long id,
            @RequestBody ExamenSubmitDTO submitDTO) throws BadRequestException{

        EvaluacionResultDTO resultado = evaluacionService.corregirExamen(id, submitDTO);
        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Ver mi historial de notas",
            description = "Devuelve todas las evaluaciones del alumno logueado ordenadas por fecha descendente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial devuelto con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    @PreAuthorize("hasAuthority('ALUMNO')") // solo los alumnos deberían ver sus notas aquí
    @GetMapping("/mis-notas")
    public ResponseEntity<List<EvaluacionHistorialDTO>> verMisNotas() throws BadRequestException {

        List<EvaluacionHistorialDTO> historial = evaluacionService.obtenerMisNotas();
        return ResponseEntity.ok(historial);
    }

    @Operation(summary = "Buscar notas de un alumno en un examen",
            description = "Permite a un profesor ver los intentos (máximo 2) de un alumno específico en un examen concreto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos (Solo ADMIN o PROFESOR)")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping("/examen/{idExamen}/alumno/{correoAlumno}")
    public ResponseEntity<List<EvaluacionHistorialDTO>> buscarNotasDeAlumno(
            @PathVariable Long idExamen,
            @PathVariable String correoAlumno) {

        List<EvaluacionHistorialDTO> notas = evaluacionService.obtenerNotasDeAlumnoEnExamen(idExamen, correoAlumno);
        return ResponseEntity.ok(notas);
    }@Operation(summary = "Obtener las estadísticas (media, aprobados...) de un alumno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estadísticas calculadas con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos (Solo ADMIN o PROFESOR)")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping("/estadisticas/{correo:.+}")
    public ResponseEntity<EstadisticasAlumnoDTO> obtenerEstadisticas(@PathVariable String correo) {
        EstadisticasAlumnoDTO estadisticas = evaluacionService.obtenerEstadisticasAlumno(correo);
        return ResponseEntity.ok(estadisticas);
    }

    @Operation(summary = "Ver el historial completo de exámenes de un alumno ordenable")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR', 'ALUMNO')")
    @GetMapping("/alumno/{correo:.+}")
    public ResponseEntity<List<EvaluacionHistorialDTO>> listarHistorialAlumno(
            @PathVariable String correo,
            @RequestParam(value = "sortBy", defaultValue = "fecha", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir) {

        return ResponseEntity.ok(evaluacionService.obtenerHistorialAlumno(correo, sortBy, sortDir));
    }

    @Operation(summary = "Ver las notas de todos los alumnos en un examen concreto (Ranking)")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping("/examen/{idExamen}")
    public ResponseEntity<List<EvaluacionHistorialDTO>> listarNotasDeExamen(
            @PathVariable Long idExamen,
            @RequestParam(value = "sortBy", defaultValue = "nota", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir) {

        return ResponseEntity.ok(evaluacionService.obtenerNotasExamen(idExamen, sortBy, sortDir));
    }

    @Operation(summary = "Exportar ranking de un examen a Excel", description = "Descarga un archivo .xlsx con las notas de los alumnos para un examen concreto.")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping("/examen/{idExamen}/exportar/excel")
    public void exportarNotasAExcel(@PathVariable Long idExamen, HttpServletResponse response) throws IOException {

        response.setContentType("application/octet-stream");
        DateFormat formateador = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
        String fechaActual = formateador.format(new Date());

        String cabeceraClave = "Content-Disposition";
        String cabeceraValor = "attachment; filename=notas_examen_" + idExamen + "_" + fechaActual + ".xlsx";
        response.setHeader(cabeceraClave, cabeceraValor);

        // obtenemos las notas ordenadas de mayor a menor (desc)
        List<EvaluacionHistorialDTO> notas = evaluacionService.obtenerNotasExamen(idExamen, "nota", "desc");

        EvaluacionExcelExporter exportador = new EvaluacionExcelExporter(notas);
        exportador.exportar(response);
    }

}
