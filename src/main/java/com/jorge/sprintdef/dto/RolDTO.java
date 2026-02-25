package com.jorge.sprintdef.dto;

import jakarta.validation.constraints.NotNull;


public class RolDTO {

    @NotNull
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name= name; }

}