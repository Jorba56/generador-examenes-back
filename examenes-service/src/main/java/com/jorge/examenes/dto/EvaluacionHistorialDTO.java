package com.jorge.examenes.dto;

import java.time.LocalDateTime;

/**
 * Objeto de Transferencia de Datos (DTO) utilizado para representar el registro
 * histórico de un intento de examen.
 * Agrupa los datos del intento (nota y fecha) con los datos del usuario
 * (correo, nombre y apellidos) para su visualización en tablas de seguimiento
 * o su exportación a documentos externos.
 */
public class EvaluacionHistorialDTO {

    private Long idEvaluacion;
    private Long idExamen;
    private double nota;
    private LocalDateTime fecha;
    private String correoUsuario;

    public EvaluacionHistorialDTO() {}

    public EvaluacionHistorialDTO(Long idEvaluacion, Long idExamen, double nota, LocalDateTime fecha) {
        this.idEvaluacion = idEvaluacion;
        this.idExamen = idExamen;
        this.nota = nota;
        this.fecha = fecha;
    }
    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }

    public Long getIdEvaluacion() { return idEvaluacion; }
    public void setIdEvaluacion(Long idEvaluacion) { this.idEvaluacion = idEvaluacion; }

    public Long getIdExamen() { return idExamen; }
    public void setIdExamen(Long idExamen) { this.idExamen = idExamen; }

    public double getNota() { return nota; }
    public void setNota(double nota) { this.nota = nota; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}