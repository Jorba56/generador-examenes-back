package com.jorge.sprintdef.dto;

import jakarta.validation.constraints.NotNull;


public class RolPutDTO {

    @NotNull
    private String name;
    private boolean activo;

    public String getName() { return name; }
    public void setName(String name) { this.name= name; }

    public boolean getActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo= activo; }

}

