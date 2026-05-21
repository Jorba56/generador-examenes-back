package com.jorge.examenes.services;

public interface ReporteService {
    byte[] generarReporteExamen(Long idEvaluacion) throws Exception;

    byte[] generarReporteGeneralExamenes() throws Exception;
}
