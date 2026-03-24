package com.jorge.examenes.controller;

import com.jorge.examenes.dto.ExamenDetalleDTO;
import com.jorge.examenes.dto.ExamenGetDTO;
import com.jorge.examenes.services.ExamenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/examenes")
@CrossOrigin(origins = "http://localhost:8080") // Corrección de seguridad para SonarQube
@Tag(name = "Exámenes", description = "Endpoints para consultar, generar, editar y realizar exámenes.")
public class ExamenController {

    private final ExamenService examenService;

    public ExamenController(ExamenService examenService) {
        this.examenService = examenService;
    }

    @Operation(summary = "Listar todos los exámenes (Vista Resumen)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de exámenes devuelta con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para acceder")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR', 'USER', 'ALUMNO')")
    @GetMapping
    public ResponseEntity<List<ExamenGetDTO>> listarTodos() {
        return ResponseEntity.ok(examenService.obtenerTodosResumen());
    }

    @Operation(summary = "Ver detalles de un examen (Preguntas numeradas y censuradas)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Examen encontrado y devuelto con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para acceder"),
            @ApiResponse(responseCode = "404", description = "El examen no existe en la base de datos")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR', 'USER', 'ALUMNO')")
    @GetMapping("/{id}")
    public ResponseEntity<ExamenDetalleDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(examenService.obtenerDetallePorId(id));
    }

    @Operation(summary = "Generar Examen Aleatorio", description = "Genera un examen aleatorio escogiendo preguntas almacenadas en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Examen generado y guardado con éxito"),
            @ApiResponse(responseCode = "400", description = "No hay suficientes preguntas en la base de datos"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para generar exámenes")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @PostMapping("/generar")
    public ResponseEntity<ExamenDetalleDTO> generarExamen(
            @RequestParam String titulo,
            @RequestParam String descripcion,
            @RequestParam int numPreguntas) {
        ExamenDetalleDTO nuevoExamen = examenService.generarExamenAleatorio(titulo, descripcion, numPreguntas);
        return new ResponseEntity<>(nuevoExamen, HttpStatus.CREATED);
    }

    @Operation(summary = "Editar detalles básicos de un examen (Título y Descripción)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Examen actualizado con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para editar exámenes"),
            @ApiResponse(responseCode = "404", description = "El examen no existe")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ExamenDetalleDTO> actualizarExamen(
            @PathVariable Long id,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String descripcion) {
        return ResponseEntity.ok(examenService.actualizarDetallesExamen(id, titulo, descripcion));
    }

    @Operation(summary = "Borrar un examen")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Examen eliminado con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar exámenes"),
            @ApiResponse(responseCode = "404", description = "El examen no existe")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> borrarExamen(@PathVariable Long id) {
        examenService.borrarExamen(id);
        return ResponseEntity.ok("Examen eliminado correctamente.");
    }

    @Operation(summary = "AÑADIR nuevas preguntas a un examen existente",
            description = "Agrega las preguntas sin borrar las que el examen ya tenía.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preguntas añadidas con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos"),
            @ApiResponse(responseCode = "404", description = "El examen o las preguntas no existen")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @PostMapping("/{id}/preguntas")
    public ResponseEntity<ExamenDetalleDTO> anadirPreguntas(
            @PathVariable Long id,
            @RequestBody List<Long> idsPreguntasNuevas) {
        return ResponseEntity.ok(examenService.anadirPreguntas(id, idsPreguntasNuevas));
    }

    @Operation(summary = "Listar exámenes paginados")
    @GetMapping("/paginados")
    public ResponseEntity<Page<ExamenGetDTO>> listarExamenesPaginados(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "fecha", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc", required = false) String sortDir) {

        return ResponseEntity.ok(examenService.obtenerExamenesPaginados(page, size, sortBy, sortDir));
    }

}