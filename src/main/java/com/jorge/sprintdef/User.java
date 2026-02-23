package com.jorge.sprintdef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_user;

    @NotNull
    private String nombre_usuario;

    @NotNull
    private String apellido_usuario;


    @NotNull
    private String contrasenha_usuario;

    @NotNull
    @Email
    private String email_usuario;

    @NotNull
    private boolean activo;

    @ManyToMany
    @JoinTable(name = "roles_usuario",
    joinColumns=@JoinColumn(name="id_user"),
    inverseJoinColumns = @JoinColumn(name = "id"))
    private List<Rol> roles= new ArrayList<>();

    // Getters and setters
    public Long getId() { return id_user; }
    public void setId(Long id) { this.id_user = id_user; }

    public String getNombre_usuario() { return nombre_usuario; }
    public void setNombre_usuario(String nombre_usuario) { this.nombre_usuario = nombre_usuario; }

    public String getEmail_usuario() { return email_usuario; }
    public void setEmail_usuario(String email_usuario) { this.email_usuario =email_usuario; }

    public String getApellido_usuario() { return apellido_usuario; }
    public void setApellido_usuario(String apellido_usuario) { this.apellido_usuario = apellido_usuario; }

    public String getContrasenha_usuario() { return contrasenha_usuario; }
    public void setContrasenha_usuario(String contrasenha_usuario) { this.contrasenha_usuario = contrasenha_usuario; }

    public boolean getActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo= activo; }
    @JsonIgnore
    public List<Rol> getRoles() {
        return roles;
    }

    // 2. Mantenemos el setter normal para que tu POST siga funcionando
    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }


    // 3. El "Getter Falso": Jackson lee esto y crea la clave "rol_id" automáticamente
    public List<Integer> getRol_id() {
        if (this.roles != null) {
            List<Integer> rolesFinal=new ArrayList<>();
            for(int i=0; i<roles.size();i++){
                rolesFinal.add((roles.get(i)).getId());
            }
            return rolesFinal;
        }
        return null; // Si el usuario aún no tiene rol, devolverá null en vez de dar error
    }
    // Jackson usará esto cuando envíes "rol_id": 2 desde Postman
    public String setRol_id(List<Integer> ids)  {
        if (ids != null) {
            this.roles = new ArrayList<>();
            for (Integer id : ids) {
                Rol nuevoRol = new Rol();
                nuevoRol.setId(id);
                this.roles.add(nuevoRol);
            }
        } else {
            this.roles = null;
        }
        return "Roles aignados";
    }
}





