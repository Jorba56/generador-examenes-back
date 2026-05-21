package com.jorge.usuarios.services;

public interface ReporteService {
    byte[] generarReporteExamen(Long idEvaluacion) throws Exception;
}
