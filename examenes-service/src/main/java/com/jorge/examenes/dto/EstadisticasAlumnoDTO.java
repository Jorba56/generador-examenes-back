package com.jorge.examenes.dto;

/**
 * Objeto de Transferencia de Datos (DTO) que consolida el rendimiento académico
 * global de un alumno.
 * Transporta métricas calculadas como la nota media histórica, el número total
 * de exámenes realizados y el recuento de aprobados frente a suspensos.
 */
public class EstadisticasAlumnoDTO {

    private String correoAlumno;
    private int totalExamenesRealizados;
    private double notaMedia;
    private int examenesAprobados;
    private int examenesSuspendidos;

    public EstadisticasAlumnoDTO() {}

    public EstadisticasAlumnoDTO(String correoAlumno, int totalExamenesRealizados, double notaMedia, int examenesAprobados, int examenesSuspendidos) {
        this.correoAlumno = correoAlumno;
        this.totalExamenesRealizados = totalExamenesRealizados;
        this.notaMedia = notaMedia;
        this.examenesAprobados = examenesAprobados;
        this.examenesSuspendidos = examenesSuspendidos;
    }

    // --- GETTERS Y SETTERS ---
    public String getCorreoAlumno() { return correoAlumno; }
    public void setCorreoAlumno(String correoAlumno) { this.correoAlumno = correoAlumno; }

    public int getTotalExamenesRealizados() { return totalExamenesRealizados; }
    public void setTotalExamenesRealizados(int totalExamenesRealizados) { this.totalExamenesRealizados = totalExamenesRealizados; }

    public double getNotaMedia() { return notaMedia; }
    public void setNotaMedia(double notaMedia) { this.notaMedia = notaMedia; }

    public int getExamenesAprobados() { return examenesAprobados; }
    public void setExamenesAprobados(int examenesAprobados) { this.examenesAprobados = examenesAprobados; }

    public int getExamenesSuspendidos() { return examenesSuspendidos; }
    public void setExamenesSuspendidos(int examenesSuspendidos) { this.examenesSuspendidos = examenesSuspendidos; }
}