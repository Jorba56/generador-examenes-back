package com.jorge.sprintdef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "incidencias")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia")
    private Long idIncidencia;

    @NotNull
    @Column(name = "descripcion_inc", length = 300)
    private String descripcion;



    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_incidencia")
    private TipoIncidencia tipo;


    @ManyToOne
    @JsonProperty("id_usuario")
    @JsonIgnore
    @JoinColumn(name = "id_user")
    private User usuario;

    public Long getIdIncidencia() {
        return idIncidencia;
    }

    public void setIdIncidencia(Long idIncidencia) {
        this.idIncidencia = idIncidencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // --- MAGIA PARA EL JSON (CUANDO HACES UN GET) ---
    @JsonProperty("usuario_id")
    public Long getUsuarioIdParaJson() {
        if (this.usuario != null) {
            return this.usuario.getIdUser(); // OJO: Pon aquí cómo se llame tu getter del ID en la clase User (getIdUsuario() o getIdUser())
        }
        return null;
    }

    // --- MAGIA PARA EL JSON (CUANDO HACES UN POST) ---
    @JsonProperty("usuario_id")
    public void setUsuarioIdDesdeJson(Long id) {
        if (id != null) {
            User u = new User();
            u.setIdUser(id); // OJO: Igual aquí, usa tu setter real
            this.usuario = u;
        }
    }

    public TipoIncidencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoIncidencia tipo) {
        this.tipo = tipo;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

}
