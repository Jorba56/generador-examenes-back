Aquí tienes la documentación Javadoc completa y enriquecida para la clase `ExamenController`. Se han añadido las etiquetas requeridas (`@author`, `@version`, `@throws`), descripciones detalladas y ejemplos de uso prácticos en formato HTTP para cada endpoint.

```markdown
# Documentación Javadoc - ExamenController

A continuación se presenta el código de la clase `ExamenController` con los bloques de comentarios Javadoc completamente desarrollados según los requisitos solicitados.

```java
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

/**
 * Controlador REST que gestiona las operaciones relacionadas con los exámenes.
 * <p>
 * Expone los endpoints para la creación, consulta, modificación y borrado de exámenes.
 * Interacciona directamente con el frontend y delega la lógica de negocio
 * al {@link com.jorge.examenes.services.ExamenService}.
 * </p>
 * <p><b>Ejemplo de uso general:</b></p>
 * <pre>
 * // Obtener todos los exámenes
 * GET /examenes
 * </pre>
 *
 * @author Jorge
 * @version 1.0
 * @since 2023
 */
@RestController
@RequestMapping("/examenes")
@Tag(name = "Exámenes", description = "Endpoints para consultar, generar, editar y realizar exámenes.")
public class ExamenController {

    private final ExamenService examenService;

    /**
     * Constructor que inyecta la dependencia del servicio de exámenes.
     *
     * @param examenService Servicio que contiene la lógica de negocio principal para la gestión de exámenes.
     */
    public ExamenController(ExamenService examenService) {
        this.examenService = examenService;
    }

    /**
     * Recupera una lista completa con el resumen de todos los exámenes registrados en el sistema.
     * <p>
     * Este endpoint es útil para mostrar listados rápidos donde no se requiere el contenido
     * íntegro de las preguntas.
     * </p>
     * <p><b>Ejemplo de petición:</b></p>
     * <pre>
     * GET /examenes
     * </pre>
     *
     * @return {@link ResponseEntity} que contiene una lista de {@link ExamenGetDTO} con la información básica de los exámenes.
     * @throws org.springframework.security.access.AccessDeniedException si el usuario no tiene los roles adecuados.
     */
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

    /**
     * Consulta los detalles completos de un examen específico, incluyendo su lista de preguntas.
     * <p>
     * Las preguntas devueltas estarán numeradas y, dependiendo de la lógica de negocio,
     * las respuestas correctas podrían estar censuradas para los alumnos.
     * </p>
     * <p><b>Ejemplo de petición:</b></p>
     * <pre>
     * GET /examenes/5
     * </pre>
     *
     * @param id Identificador único del examen en la base de datos.
     * @return {@link ResponseEntity} con el {@link ExamenDetalleDTO} que contiene todos los datos detallados del examen.
     * @throws RuntimeException (o excepción personalizada) si el examen con el ID proporcionado no existe (HTTP 404).
     */
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

    /**
     * Genera un nuevo examen de forma aleatoria seleccionando preguntas existentes en la base de datos.
     * <p>
     * Ideal para crear pruebas rápidas sin necesidad de seleccionar manualmente cada pregunta.
     * </p>
     * <p><b>Ejemplo de petición:</b></p>
     * <pre>
     * POST /examenes/generar?titulo=Examen%20Final&descripcion=Prueba%20de%20nivel&numPreguntas=10
     * </pre>
     *
     * @param titulo       Título que se le asignará al nuevo examen.
     * @param descripcion  Descripción o instrucciones generales del examen.
     * @param numPreguntas Cantidad de preguntas aleatorias que contendrá el examen.
     * @return {@link ResponseEntity} con el {@link ExamenDetalleDTO} del examen recién creado y un estado HTTP 201 (CREATED).
     * @throws IllegalArgumentException si no hay suficientes preguntas en la base de datos para cubrir {@code numPreguntas}.
     */
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

    /**
     * Modifica los detalles básicos de un examen existente (título y descripción) sin alterar sus preguntas.
     * <p>
     * Los parámetros son opcionales; solo se actualizarán aquellos que se envíen en la petición.
     * </p>
     * <p><b>Ejemplo de petición:</b></p>
     * <pre>
     * PUT /examenes/5?titulo=Nuevo%20Titulo
     * </pre>
     *
     * @param id          Identificador único del examen a modificar.
     * @param titulo      Nuevo título para el examen (opcional).
     * @param descripcion Nueva descripción para el examen (opcional).
     * @return {@link ResponseEntity} con el {@link ExamenDetalleDTO} actualizado.
     * @throws RuntimeException si el examen con el ID proporcionado no se encuentra en el sistema.
     */
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

    /**
     * Elimina de forma definitiva un examen de la base de datos.
     * <p>
     * Esta acción es irreversible y eliminará la asociación del examen con sus preguntas
     * (aunque no borrará las preguntas del banco general).
     * </p>
     * <p><b>Ejemplo de petición:</b></p>
     * <pre>
     * DELETE /examenes/5
     * </pre>
     *
     * @param id Identificador del examen que se desea borrar.
     * @return {@link ResponseEntity} con un mensaje de texto confirmando la eliminación.
     * @throws RuntimeException si el examen a eliminar no existe.
     */
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

    /**
     * Agrega un conjunto de preguntas nuevas a un examen ya existente.
     * <p>
     * Las preguntas se añaden al listado actual sin eliminar las que el examen ya tuviera
     * asignadas previamente. Se ignorarán los IDs de preguntas que ya pertenezcan al examen.
     * </p>
     * <p><b>Ejemplo de petición:</b></p>
     * <pre>
     * POST /examenes/5/preguntas
     * Content-Type: application/json
     * 
     * [12, 15, 22]
     * </pre>
     *
     * @param id                 Identificador del examen a modificar.
     * @param idsPreguntasNuevas Lista de identificadores de las preguntas que se desean añadir.
     * @return {@link ResponseEntity} con el {@link ExamenDetalleDTO} actualizado con las nuevas preguntas.
     * @throws RuntimeException si el examen o alguna de las preguntas proporcionadas no existen.
     */
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

    /**
     * Recupera una lista paginada de todos los exámenes registrados en el sistema.
     * <p>
     * Permite ordenar los resultados dinámicamente por diferentes campos, facilitando
     * la visualización en tablas de datos en el frontend.
     * </p>
     * <p><b>Ejemplo de petición:</b></p>
     * <pre>
     * GET /examenes/paginados?page=1&size=5&sortBy=titulo&sortDir=asc
     * </pre>
     *
     * @param page    Número de la página a consultar (empieza en 0). Por defecto es 0.
     * @param size    Cantidad de elementos por página. Por defecto es 10.
     * @param sortBy  Nombre del campo por el cual se ordenarán los resultados (ej: "fecha", "titulo"). Por defecto es "fecha".
     * @param sortDir Dirección de la ordenación ("asc" para ascendente, "desc" para descendente). Por defecto es "desc".
     * @return {@link ResponseEntity} con una página ({@link Page}) que contiene objetos DTO con el resumen de los exámenes.
     * @throws IllegalArgumentException si los parámetros de paginación u ordenación son inválidos.
     */
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
```
```