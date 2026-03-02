package com.jorge.sprintdef.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class UserByRol{

    @NotNull
    @JsonProperty("nombre_usuario")
    private String nombreUsuario;

    @NotNull
    private boolean activo;

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public boolean getActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo= activo; }

}

