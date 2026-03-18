package com.jorge.examenes.controller;

import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.services.ExamenService;
import com.jorge.examenes.services.impl.ExamenServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que expone los endpoints para la consulta del registro de incidencias y errores.
 * Por motivos de seguridad y auditoría, el acceso a estos endpoints está estrictamente restringido
 * a usuarios con el rol de Administrador.
 */
@RestController
@RequestMapping("/examenes")
@CrossOrigin(origins = {"*"})
@Tag(name = "Examenes", description = "Endpoints para consultar, generar, realizar y evaluar exámenes.")
public class ExamenController {

    private final ExamenServiceImpl examenService;

    public ExamenController(ExamenServiceImpl examenService) {
        this.examenService = examenService;
    }

    /**
     * Consulta el listado completo de todas las incidencias registradas.
     */
    @Operation(summary = "Generar Examen", description = "Genera un examen aleatorio escogiendo preguntas aleatorias almacenadas en la base de datos.")
    @PreAuthorize("hasAuthority('PROFESOR')")
    @PostMapping("/generar")
    public ResponseEntity<Examen> generarExamen(
            @RequestParam String titulo,
            @RequestParam String descripcion,
            @RequestParam int numPreguntas) {

        Examen nuevoExamen = examenService.generarExamenAleatorio(titulo, descripcion, numPreguntas);
        return new ResponseEntity<>(nuevoExamen, HttpStatus.CREATED);
    }
}