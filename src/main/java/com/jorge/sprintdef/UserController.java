package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.RolPostUser;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import com.jorge.sprintdef.services.UserService;

/**
 * Controlador REST encargado de gestionar las peticiones HTTP relacionadas con los Usuarios.
 * Define los endpoints para el CRUD de usuarios y la gestión de sus roles asignados.
 */
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Endpoints para el CRUD de usuarios y la gestión de sus roles asignados.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Obtiene la lista completa de usuarios activos en el sistema.
     */
    @Operation(summary = "Listar todos los usuarios", description = "Obtiene una lista con la información pública de todos los usuarios activos en el sistema.")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public List<UsersAllDTO> getAllUsers() {
        return userService.listarUsuarios();
    }

    /**
     * Busca y devuelve los datos de un usuario específico mediante su ID.
     */
    @Operation(summary = "Buscar usuario por ID", description = "Devuelve los detalles completos de un usuario específico. Solo accesible para administradores.")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    public UserIdDTo getUserId(@PathVariable Long id) {
        return userService.buscarPorId(id);
    }

    /**
     * Actualiza la información de un usuario existente.
     * Requiere el rol del editor para validar permisos de modificación.
     */
    @Operation(summary = "Actualizar usuario", description = "Modifica los datos de un usuario. Bloquea la edición de contraseñas para admins y roles para usuarios normales.")
    @PutMapping("/{id}")
    public String updateUser(@PathVariable String rolEditor, @PathVariable Long id, @RequestBody User usuario) {
        return userService.actualizarUsuario(rolEditor, id, usuario);
    }

    /**
     * Realiza un borrado lógico del usuario especificado.
     */
    @Operation(summary = "Desactivar usuario", description = "Realiza un borrado lógico del usuario especificado cambiando su estado activo a false.")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteUser (@PathVariable Long id){
        return userService.desactivarUsuario(id);
    }

    /**
     * Obtiene la lista de roles que tiene asignados un usuario en concreto.
     */
    @Operation(summary = "Ver roles de un usuario", description = "Obtiene la lista de los roles de seguridad que tiene asignados un usuario en concreto.")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}/roles")
    public List<Rol> rolesUser(@PathVariable Long id){
        return userService.rolesUser(id);
    }

    /**
     * Asigna un nuevo rol a un usuario existente.
     */
    @Operation(summary = "Añadir rol a un usuario", description = "Asigna un nuevo rol a la lista de roles del usuario.")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/roles")
    public String userAddRol(@PathVariable Long id, @RequestBody RolPostUser idRol){
        return userService.addRolUser(id,idRol);
    }

    /**
     * Revoca (elimina) un rol específico de un usuario.
     */
    @Operation(summary = "Revocar rol a un usuario", description = "Elimina la asociación de un rol específico con un usuario.")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{idUsuario}/roles/{idRol}") //Quitarle un rol a un usuario
    public String deleteRolUser (@PathVariable Long idRol, @PathVariable Long idUsuario) {
        return userService.deleteRolUser(idUsuario, idRol );
    }
}
