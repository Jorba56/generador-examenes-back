package com.jorge.examenes.dto;

import java.util.List;

/**
 * Objeto de Transferencia de Datos (DTO) que proporciona la vista detallada y
 * completa de un examen.
 * Incluye tanto los metadatos del examen como la lista íntegra de sus preguntas
 * asociadas. Se utiliza cuando un profesor o administrador necesita ver o editar
 * el examen en profundidad.
 */
public class ExamenDetalleDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private List<PreguntaExamenDTO> preguntas;

    public ExamenDetalleDTO() {
    }

    public ExamenDetalleDTO(Long id, String titulo, String descripcion, List<PreguntaExamenDTO> preguntas) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.preguntas = preguntas;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<PreguntaExamenDTO> getPreguntas() { return preguntas; }
    public void setPreguntas(List<PreguntaExamenDTO> preguntas) { this.preguntas = preguntas; }
}