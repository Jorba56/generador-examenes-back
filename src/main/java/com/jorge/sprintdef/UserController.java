package com.jorge.sprintdef;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRep;

    public UserController(UserRepository userRep) {
        this.userRep = userRep;
    }


    @GetMapping
    public List<UserDTO> getAllUsers() {
        List<User> encontrados = userRep.findUsersByActivoIs(true);
        List<UserDTO> usuarios=new ArrayList<>();
        for (User encontrado : encontrados) usuarios.add(mappingADTO(encontrado));
        return usuarios;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserId(@PathVariable Long id) {
        // 1. Buscamos el usuario de forma segura
        User usuario = userRep.findById(id).orElse(null);

        // 2. Si no existe, devolvemos un 404 (Not Found) y cortamos la ejecución
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. Si existe, lo mapeamos y lo envolvemos en un 200 (OK)
        UserDTO dto = mappingADTO(usuario);
        return ResponseEntity.ok(dto);
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
        User userSelect=userRep.findById(id).orElse(null);
        if (userSelect!=null) {
            userSelect.setActivo(false);
            userRep.save(userSelect);
        }
        return ("usuario borrado correctamente");
    }

    UserDTO mappingADTO (User usuario){
        UserDTO dto= new UserDTO();
        dto.setIdUser(usuario.getIdUser());
        dto.setRolId(usuario.getRolId());
        dto.setNombreUsuario(usuario.getNombreUsuario());
        dto.setApellidoUsuario(usuario.getApellidoUsuario());
        dto.setEmailUsuario(usuario.getEmailUsuario());
        return dto;
    }

}
