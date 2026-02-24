package com.jorge.sprintdef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UserDTO {

    @NotNull
    @JsonProperty("id_user")
    private Long idUser;

    @JsonProperty("nombre_usuario")
    private String nombreUsuario;

    @JsonProperty("apellido_usuario")
    private String apellidoUsuario;

    @JsonProperty("email_usuario")
    private String emailUsuario;

    @JsonIgnore
    @JsonProperty("roles_usuario")
    private List<Rol> roles= new ArrayList<>();

    // Getters y setters
    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario =emailUsuario; }

    public String getApellidoUsuario() { return apellidoUsuario; }
    public void setApellidoUsuario(String apellidoUsuario) { this.apellidoUsuario = apellidoUsuario; }

    public List<Rol> getRoles() {
        return roles;
    }

    // 2. Mantenemos el setter normal para que tu POST siga funcionando
    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }

    public List<Long> getRolId() {
        List<Long> rolesFinal=new ArrayList<>();
        if (this.roles != null) {
            for(int i=0; i<roles.size();i++){
                rolesFinal.add((roles.get(i)).getIdRol());
            }
            return rolesFinal;
        }
        return rolesFinal; // Si el usuario aún no tiene rol, devolverá null en vez de dar error
    }
    // Jackson usará esto cuando envíes "rol_id": 2 desde Postman
    public void setRolId(List<Long> ids)  {
        if (ids != null) {
            this.roles = new ArrayList<>();
            for (Long id : ids) {
                Rol nuevoRol = new Rol();
                nuevoRol.setIdRol(id);
                this.roles.add(nuevoRol);
            }
        } else {
            this.roles = null;
        }
    }
}
