package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;

import org.springframework.web.bind.annotation.*;

import java.util.List;


import com.jorge.sprintdef.services.UserService;
@RestController
@RequestMapping("/api/v1/users")
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

    @PostMapping("/add")
    public String addUser (@RequestBody User usuario){
        return userService.addUsuario(usuario);
    }

    @PutMapping("/update/{rolEditor}/{id}")
    public String updateUser(@PathVariable String rolEditor, @PathVariable Long id, @RequestBody User usuario) {
        return userService.actualizarUsuario(rolEditor, id, usuario);
    }

    @DeleteMapping("/{id}")
    public String deleteUser (@PathVariable Long id){
        return userService.desactivarUsuario(id);
    }
}
