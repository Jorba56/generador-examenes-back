package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.RolPostUser;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;

import org.springframework.web.bind.annotation.*;

import java.util.List;


import com.jorge.sprintdef.services.UserService;
@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UsersAllDTO> getAllUsers() {
        return userService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public UserIdDTo getUserId(@PathVariable Long id) {
        return userService.buscarPorId(id);
    }

    @PostMapping("")
    public UsersAllDTO addUser (@RequestBody UserAddDTO usuario){
        return userService.addUsuario(usuario);
    }

    @PutMapping("/{rolEditor}/{id}")
    public String updateUser(@PathVariable String rolEditor, @PathVariable Long id, @RequestBody User usuario) {
        return userService.actualizarUsuario(rolEditor, id, usuario);
    }

    @DeleteMapping("/{id}")
    public String deleteUser (@PathVariable Long id){
        return userService.desactivarUsuario(id);
    }

    @GetMapping("/{id}/roles")
    public List<Rol> rolesUser(@PathVariable Long id){
        return userService.rolesUser(id);
    }

    @PostMapping("/{id}/roles")
    public String userAddRol(@PathVariable Long id, @RequestBody RolPostUser idRol){
        return userService.addRolUser(id,idRol);
    }

    @DeleteMapping("/{idUsuario}/roles/{idRol}") //Quitarle un rol a un usuario
    public String deleteRolUser (@PathVariable Long idRol, @PathVariable Long idUsuario) {
        return userService.deleteRolUser(idUsuario, idRol );
    }
}
