package com.jorge.sprintdef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

//@Data
@Entity
@Table(name = "usuarios")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(name = "nombre_usuario", length = 100)
    private String nombreUsuario;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Column(name = "apellido_usuario", length = 150)
    private String apellidoUsuario;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(name = "contrasenha_usuario", length = 255)
    private String contrasenhaUsuario;

    @NotBlank(message = "El correo no puede estar vacío")
    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,8}$",message = "El formato del correo no es válido")
    @Column(name = "correo_usuario", length = 254, unique = true)
    private String emailUsuario;

    @NotNull
    private boolean activo=true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "roles_usuario",
    joinColumns=@JoinColumn(name="id_user"),
    inverseJoinColumns = @JoinColumn(name = "id_rol"))
    private List<Rol> roles= new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "incidencias_usuario",
            joinColumns=@JoinColumn(name="id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_incidencia"))
    private List<Incidencia> incidencias= new ArrayList<>();

    // Getters and setters
    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser= idUser; }

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

   public List<Incidencia> getIncidencias() {
        return incidencias;
    }

    public void setIncidencias(List<Incidencia> incidencias) {
        this.incidencias = incidencias;
    }

    public List<Long> getIdIncidencia() {
        List<Long> incidenciasFinal=new ArrayList<>();
        if (this.incidencias != null) {
            for(int i=0; i<incidencias.size();i++){
                incidenciasFinal.add((incidencias.get(i)).getIdIncidencia());
            }
            return incidenciasFinal;
        }
        return incidenciasFinal; // Si el usuario aún no tiene rol, devolverá null en vez de dar error
    }
    // Jackson usará esto cuando envíes "rol_id": 2 desde Postman
    public void setIdIncidencias (List<Long> ids)  {
        if (ids != null) {
            this.incidencias = new ArrayList<>();
            for (Long id : ids) {
                Incidencia nueva = new Incidencia();
                nueva.setIdIncidencia(id);
                this.incidencias.add(nueva);
            }
        } else {
            this.incidencias = null;
        }
    }
}





