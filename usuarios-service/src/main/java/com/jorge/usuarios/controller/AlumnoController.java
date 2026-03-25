package com.jorge.usuarios.controller;

import com.jorge.usuarios.dto.AlumnoDTO;
import com.jorge.usuarios.exceptions.NotFoundException;
import com.jorge.usuarios.services.AlumnoService;
import com.jorge.usuarios.utils.AlumnoExcelExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/alumnos")
@Tag(name = "Alumnos", description = "Gestión exclusiva de alumnos para uso del profesorado")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    @Operation(summary = "Listar todos los alumnos",
            description = "Devuelve una lista con todos los usuarios que tienen el rol de ALUMNO en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de alumnos devuelta con éxito"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos (Solo ADMIN o PROFESOR)")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping
    public ResponseEntity<List<AlumnoDTO>> listarAlumnos() {
        return ResponseEntity.ok(alumnoService.obtenerTodosLosAlumnos());
    }

    @Operation(summary = "Buscar un alumno por su correo electrónico",
            description = "Devuelve los datos de un alumno específico buscándolo por su email. Si el correo existe pero pertenece a un profesor o admin, devolverá un error 404.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alumno encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos"),
            @ApiResponse(responseCode = "404", description = "El alumno no existe o no tiene el rol adecuado")
    })
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping("/{email}")
    public ResponseEntity<AlumnoDTO> obtenerAlumnoPorCorreo(@PathVariable String email) throws NotFoundException {
        return ResponseEntity.ok(alumnoService.obtenerAlumnoPorCorreo(email));
    }
    @Operation(summary = "Exportar lista de alumnos a Excel", description = "Genera y descarga un archivo .xlsx con todos los alumnos.")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping("/exportar/excel")
    public void exportarAlumnosAExcel(HttpServletResponse response) throws IOException {

        // configuramos el tipo de archivo de respuesta para Excel
        response.setContentType("application/octet-stream");

        DateFormat formateador = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String fechaActual = formateador.format(new Date());

        String cabeceraClave = "Content-Disposition";
        String cabeceraValor = "attachment; filename=alumnos_" + fechaActual + ".xlsx";
        response.setHeader(cabeceraClave, cabeceraValor);

        List<AlumnoDTO> alumnos = alumnoService.obtenerTodosLosAlumnos();

        // generamos el archivo
        AlumnoExcelExporter exportador = new AlumnoExcelExporter(alumnos);
        exportador.exportar(response);
    }

    @Operation(summary = "Listar alumnos paginados")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping("/paginados")
    public ResponseEntity<Page<AlumnoDTO>> listarAlumnosPaginados(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "apellidos", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir) {

        return ResponseEntity.ok(alumnoService.obtenerAlumnosPaginados(page, size, sortBy, sortDir));
    }
}