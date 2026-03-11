package com.jorge.sprintdef.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="rol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @NotNull
    @Column(name = "name", length = 100)
    private String name;

    @NotNull
    private boolean activo=true;

    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol= idRol; }

    public String getName() { return name; }
    public void setName(String name) { this.name= name; }

    public boolean getActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo= activo; }

}