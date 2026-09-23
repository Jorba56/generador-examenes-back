package com.jorge.examenes.dto;

import java.time.LocalDateTime;

/**
 * Objeto de Transferencia de Datos (DTO) que proporciona una vista resumida de un examen.
 * Se utiliza principalmente en listados y vistas paginadas para enviar al cliente
 * la información básica (título, descripción, fecha y número de preguntas) sin
 * sobrecargar la red con la lista completa de preguntas.
 */
public class ExamenGetDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private int numeroPreguntas;
    private LocalDateTime fechaCreacion;

    public ExamenGetDTO() {}

    public ExamenGetDTO(Long id, String titulo, String descripcion, int numeroPreguntas, LocalDateTime fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.numeroPreguntas = numeroPreguntas;
        this.fechaCreacion=fechaCreacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getNumeroPreguntas() { return numeroPreguntas; }
    public void setNumeroPreguntas(int numeroPreguntas) { this.numeroPreguntas = numeroPreguntas; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
