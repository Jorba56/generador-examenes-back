package com.jorge.sprintdef;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="rol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private boolean activo;

    public Long getId() { return id; }

    public String getName() { return name; }

    public boolean getActivo() { return activo; }

    public void setId(Long id) { this.id= id; }

    public void setName(String name) { this.name= name; }

    public void setActivo(boolean activo) { this.activo= activo; }
}