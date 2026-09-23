package com.jorge.examenes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "respuestas_usuario")
public class RespuestaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_evaluacion")
    private Long idEvaluacion;

    @Column(name = "id_pregunta")
    private Long idPregunta;

    @Column(name = "respuesta_marcada")
    private String respuestaMarcada;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdEvaluacion() { return idEvaluacion; }
    public void setIdEvaluacion(Long idEvaluacion) { this.idEvaluacion = idEvaluacion; }

    public Long getIdPregunta() { return idPregunta; }
    public void setIdPregunta(Long idPregunta) { this.idPregunta = idPregunta; }

    public String getRespuestaMarcada() { return respuestaMarcada; }
    public void setRespuestaMarcada(String respuestaMarcada) { this.respuestaMarcada = respuestaMarcada; }
}