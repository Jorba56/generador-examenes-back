package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.RolPostUser;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import com.jorge.sprintdef.services.UserService;

/**
 * Controlador REST encargado de gestionar las peticiones HTTP relacionadas con los Usuarios.
 * Define los endpoints para el CRUD de usuarios y la gestión de sus roles asignados.
 */
@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Obtiene la lista completa de usuarios activos en el sistema.
     */
    @GetMapping
    public List<UsersAllDTO> getAllUsers() {
        return userService.listarUsuarios();
    }

    /**
     * Busca y devuelve los datos de un usuario específico mediante su ID.
     */
    @GetMapping("/{id}")
    public UserIdDTo getUserId(@PathVariable Long id) {
        return userService.buscarPorId(id);
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     */
    /*
    @PostMapping("")
    public UsersAllDTO addUser (@Valid @RequestBody UserAddDTO usuario){
        return userService.addUsuario(usuario);
    }*/

    /**
     * Actualiza la información de un usuario existente.
     * Requiere el rol del editor para validar permisos de modificación.
     */
    @PutMapping("/{rolEditor}/{id}")
    public String updateUser(@PathVariable String rolEditor, @PathVariable Long id, @RequestBody User usuario) {
        return userService.actualizarUsuario(rolEditor, id, usuario);
    }

    /**
     * Realiza un borrado lógico del usuario especificado.
     */
    @DeleteMapping("/{id}")
    public String deleteUser (@PathVariable Long id){
        return userService.desactivarUsuario(id);
    }

    /**
     * Obtiene la lista de roles que tiene asignados un usuario en concreto.
     */
    @GetMapping("/{id}/roles")
    public List<Rol> rolesUser(@PathVariable Long id){
        return userService.rolesUser(id);
    }

    /**
     * Asigna un nuevo rol a un usuario existente.
     */
    @PostMapping("/{id}/roles")
    public String userAddRol(@PathVariable Long id, @RequestBody RolPostUser idRol){
        return userService.addRolUser(id,idRol);
    }

    /**
     * Revoca (elimina) un rol específico de un usuario.
     */
    @DeleteMapping("/{idUsuario}/roles/{idRol}") //Quitarle un rol a un usuario
    public String deleteRolUser (@PathVariable Long idRol, @PathVariable Long idUsuario) {
        return userService.deleteRolUser(idUsuario, idRol );
    }
}
