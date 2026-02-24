package com.jorge.sprintdef;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRep;

    public UserController(UserRepository userRep) {
        this.userRep = userRep;
    }


    @GetMapping
    public List<User> getAllUsers(){
        return userRep.findAll();
    }

    @GetMapping("/{id}")
    public User getUserId (@PathVariable Long id){
        return userRep.findById(id).orElse(null);
    }

    @PostMapping("/add")
    public String addUser (@RequestBody User usuario){
        userRep.save(usuario);
        return( "usuario añadido con exito");
    }

    @PutMapping("/update/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody User usuario) {

        // 1. Buscamos el usuario y abrimos el Optional de forma segura
        User userUpdate = userRep.findById(id).orElse(null);

        // 2. Comprobamos que exista
        if (userUpdate == null) {
            return "Error: Usuario no encontrado";
        }

        // 3. Actualizamos los datos
        userUpdate.setNombreUsuario(usuario.getNombreUsuario());
        userUpdate.setApellidoUsuario(usuario.getApellidoUsuario());
        userUpdate.setEmailUsuario(usuario.getEmailUsuario());
        userUpdate.setActivo(usuario.getActivo());
        userUpdate.setContrasenhaUsuario(usuario.getContrasenhaUsuario());

        // 4. Actualizamos el rol (ahora usuario.getRol() sí tendrá datos gracias al setter falso)
        userUpdate.setRoles(usuario.getRoles());

        // 5. Guardamos en la base de datos
        userRep.save(userUpdate);

        return "Usuario editado correctamente";
    }

    @DeleteMapping("/{id}")
    public String deleteUser (@PathVariable Long id){
        userRep.deleteById(id);
        return("usuario borrado con exito");
    }

}
