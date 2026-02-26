package com.jorge.sprintdef.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class UsuarioRolesDTO {
    @NotNull
    @JsonProperty("id_rol")
    private List<Long> idRoles;

    @NotNull
    @JsonProperty("id_usuario")
    private Long idUser;

    public List<Long> getIdRoles() { return idRoles; }
    public void setIdRoles(List<Long> idRoles ) { this.idRoles=idRoles; }

    public Long getIdUser() { return idUser; }
    public void setIdUser (Long idUser ) { this.idUser=idUser; }
}
