package com.jorge.sprintdef;

import com.jorge.sprintdef.services.IncidenciasService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST encargado de gestionar las incidencias del sistema.
 * Permite registrar nuevos errores y consultar el historial (solo lectura y creación).
 */
@RestController
@RequestMapping("/usuario/incidencias")
public class IncidenciasController {

    private final IncidenciasService incidenciasService;

    public IncidenciasController(IncidenciasService incidenciasService) {
        this.incidenciasService = incidenciasService;
    }

    /**
     * Consulta el listado completo de todas las incidencias registradas.
     */
    @GetMapping
    public List<Incidencia> getAllIncidencias() {
        return incidenciasService.listarIncidencias();
    }
    /**
     * Busca los detalles de una incidencia específica por su ID.
     */
    @GetMapping("/{id}")
    public Optional<Incidencia> getIncidenciaById(@PathVariable Long id) {
        return incidenciasService.incidenciaPorId(id);
    }

    /**
     * Registra una nueva incidencia (error de login, registro, examen, etc.) en el sistema.
     */
    @PostMapping("")
    public String createIncidencia(@RequestBody Incidencia incidencia) {
        return incidenciasService.newIncidencia(incidencia);
    }
    @GetMapping("/tipo/{tipo}")
    public List<Incidencia> getIncidenciasTipo(@PathVariable TipoIncidencia tipo) {
        return incidenciasService.incidenciasPorTipo(tipo);
    }
}