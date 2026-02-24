package com.jorge.sprintdef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @NotNull
    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @NotNull
    @Column(name = "apellido_usuario")
    private String apellidoUsuario;


    @NotNull
    @Column(name = "contrasenha_usuario")
    private String contrasenhaUsuario;

    @NotNull
    @Email
    @Column(name = "email_usuario")
    private String emailUsuario;

    @NotNull
    private boolean activo;

    @ManyToMany
    @JoinTable(name = "roles_usuario",
    joinColumns=@JoinColumn(name="id_user"),
    inverseJoinColumns = @JoinColumn(name = "id"))
    private List<Rol> roles= new ArrayList<>();

    // Getters and setters
    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario =emailUsuario; }

    public String getApellidoUsuario() { return apellidoUsuario; }
    public void setApellidoUsuario(String apellidoUsuario) { this.apellidoUsuario = apellidoUsuario; }

    public String getContrasenhaUsuario() { return contrasenhaUsuario; }
    public void setContrasenhaUsuario(String contrasenhaUsuario) { this.contrasenhaUsuario = contrasenhaUsuario; }

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
    public List<Long> getRolId() {
        List<Long> rolesFinal=new ArrayList<>();
        if (this.roles != null) {
            for(int i=0; i<roles.size();i++){
                rolesFinal.add((roles.get(i)).getId());
            }
            return rolesFinal;
        }

        return rolesFinal; // Si el usuario aún no tiene rol, devolverá null en vez de dar error
    }
    // Jackson usará esto cuando envíes "rol_id": 2 desde Postman
    public String setRolId(List<Long> ids)  {
        if (ids != null) {
            this.roles = new ArrayList<>();
            for (Long id : ids) {
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





