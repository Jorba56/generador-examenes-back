package com.jorge.examenes.controller;

import com.jorge.examenes.dto.EvaluacionResultDTO;
import com.jorge.examenes.dto.ExamenSubmitDTO;
import com.jorge.examenes.services.impl.EvaluacionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody ExamenSubmitDTO submitDTO) {

        EvaluacionResultDTO resultado = evaluacionService.corregirExamen(id, submitDTO);
        return ResponseEntity.ok(resultado);
    }
}
