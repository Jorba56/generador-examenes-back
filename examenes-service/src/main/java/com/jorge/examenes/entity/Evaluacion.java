package com.jorge.examenes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la realización de un examen por parte de un alumno.
 * Mapea la tabla "evaluaciones" en la base de datos, almacenando la nota obtenida,
 * la fecha del intento y el identificador del usuario.
 */
@Entity
@Table(name = "evaluaciones")
public class Evaluacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cambiamos el idUsuario por el correo
    @Column(name = "correo_usuario")
    private String correoUsuario;

    @Column(name = "id_examen")
    private Long idExamen;

    private Double nota;
    private LocalDateTime fecha;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }

    public Long getIdExamen() { return idExamen; }
    public void setIdExamen(Long idExamen) { this.idExamen = idExamen; }

    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}