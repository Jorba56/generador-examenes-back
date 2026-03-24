package com.jorge.examenes.controller;

import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.services.impl.PreguntaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la administración del banco de preguntas.
 * Permite a los profesores y administradores realizar operaciones CRUD (Crear, Leer,
 * Actualizar, Borrar) sobre las preguntas que posteriormente formarán los exámenes.
 */
@RestController
@RequestMapping("/preguntas")
@Tag(name = "Preguntas", description = "CRUD para la gestión del banco global de preguntas")
public class PreguntaController {

    private final PreguntaServiceImpl preguntaService;

    public PreguntaController(PreguntaServiceImpl preguntaService) {
        this.preguntaService = preguntaService;
    }

    /**
     * Recupera el listado completo de todas las preguntas almacenadas en el sistema.
     *
     * @return Lista de todas las preguntas del banco.
     */
    @Operation(summary = "Listar todas las preguntas del banco")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping
    public ResponseEntity<List<Pregunta>> listarTodas() {
        return ResponseEntity.ok(preguntaService.obtenerTodas());
    }

    /**
     * Busca los detalles de una pregunta específica por su identificador.
     *
     * @param id Identificador de la pregunta.
     * @return Los datos de la pregunta solicitada.
     */
    @Operation(summary = "Obtener una pregunta específica")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping("/{id}")
    public ResponseEntity<Pregunta> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(preguntaService.obtenerPorId(id));
    }

    /**
     * Registra una nueva pregunta en el banco general de preguntas.
     *
     * @param pregunta Objeto con los datos de la pregunta (enunciado, opciones y respuesta correcta).
     * @return La pregunta persistida en la base de datos.
     */
    @Operation(summary = "Crear una nueva pregunta")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @PostMapping
    public ResponseEntity<Pregunta> crearPregunta(@RequestBody Pregunta pregunta) {
        return new ResponseEntity<>(preguntaService.guardarPregunta(pregunta), HttpStatus.CREATED);
    }

    /**
     * Modifica los datos de una pregunta existente en el sistema.
     *
     * @param id Identificador de la pregunta a actualizar.
     * @param pregunta Objeto con los nuevos datos a aplicar.
     * @return La pregunta actualizada.
     */
    @Operation(summary = "Editar una pregunta existente")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Pregunta> actualizarPregunta(@PathVariable Long id, @RequestBody Pregunta pregunta) {
        return ResponseEntity.ok(preguntaService.actualizarPregunta(id, pregunta));
    }

    /**
     * Elimina permanentemente una pregunta del banco de datos.
     *
     * @param id Identificador de la pregunta a borrar.
     * @return Mensaje de confirmación de borrado.
     */
    @Operation(summary = "Borrar una pregunta")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> borrarPregunta(@PathVariable Long id) {
        preguntaService.borrarPregunta(id);
        return ResponseEntity.ok("Pregunta eliminada correctamente de la base de datos.");
    }
}