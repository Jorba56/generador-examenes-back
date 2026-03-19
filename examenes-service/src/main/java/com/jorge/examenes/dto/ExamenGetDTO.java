package com.jorge.examenes.dto;

public class ExamenGetDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private int numeroPreguntas; // <-- NUEVO CAMPO

    public ExamenGetDTO() {}

    public ExamenGetDTO(Long id, String titulo, String descripcion, int numeroPreguntas) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.numeroPreguntas = numeroPreguntas;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getNumeroPreguntas() { return numeroPreguntas; }
    public void setNumeroPreguntas(int numeroPreguntas) { this.numeroPreguntas = numeroPreguntas; }
}
