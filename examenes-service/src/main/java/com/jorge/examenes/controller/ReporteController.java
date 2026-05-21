package com.jorge.examenes.controller;

import com.jorge.examenes.services.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // ENDPOINT 1: Reporte individual (Solo acepta números gracias al \\d+)
    @PreAuthorize("hasAnyAuthority('ALUMNO', 'ADMIN', 'PROFESOR')")
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<byte[]> descargarReporte(@PathVariable Long id) {
        System.out.println("\n[BIRT-DEBUG] ---> Petición GET recibida en /reportes/" + id);
        try {
            byte[] pdf = reporteService.generarReporteExamen(id);
            System.out.println("[BIRT-DEBUG] ---> PDF generado correctamente con tamaño: " + pdf.length + " bytes");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_examen_" + id + ".pdf");

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("\n[BIRT-DEBUG] ❌ ¡ERROR CRÍTICO EN EL CONTROLLER!");
            System.err.println("[BIRT-DEBUG] Mensaje del error: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ENDPOINT 2: Reporte General (El que estabas intentando llamar)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESOR')")
    @GetMapping("/general")
    public ResponseEntity<byte[]> descargarReporteGeneral() {
        System.out.println("\n[BIRT-DEBUG] ---> Petición GET recibida en /reportes/general");
        try {
            byte[] pdf = reporteService.generarReporteGeneralExamenes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Listado_General_Examenes.pdf");

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("\n[BIRT-DEBUG] ❌ ERROR generando reporte general");
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}