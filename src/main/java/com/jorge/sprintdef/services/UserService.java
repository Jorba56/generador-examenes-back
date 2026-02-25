package com.jorge.sprintdef.services;

import com.jorge.sprintdef.User;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;
import com.jorge.sprintdef.UserRepository;

import org.springframework.stereotype.Service;
import com.jorge.sprintdef.mapping.UserMapper;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRep;
    private final UserMapper userMap;

    public UserService(UserRepository userRep, UserMapper userMap) {
        this.userRep = userRep;
        this.userMap = userMap;

    }

    public List<UsersAllDTO> listarUsuarios(){
        List<User> encontrados = userRep.findUsersByActivoIs(true);
        List<UsersAllDTO> usuarios=new ArrayList<>();
        for (User encontrado : encontrados) usuarios.add(userMap.mappingADTO(encontrado));
        return usuarios;
    }

    public UserIdDTo buscarPorId(Long id) {
        // 1. Buscamos el usuario de forma segura

        User usuario = userRep.findById(id).orElseThrow(() -> new RuntimeException("Mensaje de error"));

        // 2. Si no existe, devolvemos un 404 (Not Found) y cortamos la ejecución

        // 3. Si existe, lo mapeamos y lo envolvemos en un 200 (OK)

        return userMap.userToIdDTO(usuario);
    }

    public String addUsuario (User usuario){
        userRep.save(usuario);
        return( "usuario añadido con exito");
    }

    public String actualizarUsuario(String rolEditor, Long id, User usuario) {

        String salida="";

        if(rolEditor.equalsIgnoreCase("admin") || (rolEditor.equalsIgnoreCase("administrador"))){

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

        salida="Usuario editado correctamente";
        } else {
            salida="Rol de editor no válido. Solo el administrador puede editar usuarios.";
        }
        return salida;
    }

    public String desactivarUsuario (Long id){
        User userSelect=userRep.findById(id).orElse(null);
        if (userSelect!=null) {
            userSelect.setActivo(false);
            userRep.save(userSelect);
        }
        return ("usuario borrado correctamente");
    }



}
