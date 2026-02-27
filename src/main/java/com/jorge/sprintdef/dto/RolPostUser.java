package com.jorge.sprintdef.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class RolPostUser {
    @NotNull
    @JsonProperty("id_rol")
    private Long idRol;

    public void setIdRol(Long idRol) { this.idRol=idRol; }
    public Long getIdRol() { return idRol; }
}
