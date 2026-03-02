package com.jorge.sprintdef.services;

import com.jorge.sprintdef.Rol;
import com.jorge.sprintdef.RolRepository;
import com.jorge.sprintdef.User;
import com.jorge.sprintdef.dto.RolPostUser;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;
import com.jorge.sprintdef.UserRepository;

import com.jorge.sprintdef.mapping.UserMapper;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final RolRepository rolRep;
    private final UserRepository userRep;
    private final UserMapper userMap;

    public UserService(UserRepository userRep, UserMapper userMap, RolRepository rolRep) {
        this.userRep = userRep;
        this.userMap = userMap;
        this.rolRep = rolRep;
    }

    public List<UsersAllDTO> listarUsuarios(){
        List<User> encontrados = userRep.findUsersByActivoIs(true);
        List<UsersAllDTO> usuarios=new ArrayList<>();
        for (User encontrado : encontrados) usuarios.add(userMap.mappingADTO(encontrado));
        return usuarios;
    }

    public UserIdDTo buscarPorId(Long id) {
        // 1. Buscamos el usuario y si no está, devolvemos null (abrimos la caja Optional)
        User usuarioEncontrado = userRep.findById(id).orElse(null);

        // 2. Si no existe, cortamos aquí y devolvemos null
        if (usuarioEncontrado == null) {
            return null;
        }

        // 3. Si existe, usamos el mapper con el usuario real
        return userMap.userToIdDTO(usuarioEncontrado);
    }

    public UsersAllDTO addUsuario (UserAddDTO usuario){
        User usuario2= userMap.userAddDTO(usuario);
        userRep.save(usuario2);
        return(userMap.mappingADTO(usuario2));
    }

    public String actualizarUsuario(String rolEditor, Long id, User usuario) {

        String salida;

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

    public List<Rol>rolesUser(Long idUser){
        List<Rol> roles=new ArrayList<>();
        User encontrado=userRep.findById(idUser).orElse(null);
        if (encontrado!=null) { roles = encontrado.getRoles();}
        return roles;
    }

    public String addRolUser(Long idUser, RolPostUser idRol){
        String salida;
        User encontrado=userRep.findById(idUser).orElse(null);
        Rol rolN=rolRep.findById(idRol.getIdRol()).orElse(null);
        if (encontrado!=null) {
            List<Rol> roles = encontrado.getRoles();
            roles.add(rolN);
            encontrado.setRoles(roles);
            userRep.save(encontrado);
            salida= "rol añdadido a usuario";
        } else{ salida="Usuario no encontrado";}
        return salida;
    }

    public String deleteRolUser(Long idUser, Long idRol) {

        // Cláusula de guarda: Comprobamos el usuario primero y salimos si falla
        User usuario = userRep.findById(idUser).orElse(null);
        if (usuario == null) {
            return "Usuario no encontrado";
        }

        // 2. Cláusula de guarda: Comprobamos el rol y salimos si falla
        Rol rolN = rolRep.findById(idRol).orElse(null);
        if (rolN == null) {
            return "Ese rol no existe en la base de datos";
        }

        // 3. Acción directa: removeIf hace el bucle y el borrado de forma segura
        boolean rolBorrado = usuario.getRoles().removeIf(rol ->
                Objects.equals(rol.getIdRol(), rolN.getIdRol())
        );

        // 4. Guardamos solo si de verdad se ha borrado algo
        if (rolBorrado) {
            userRep.save(usuario);
            return "Rol eliminado correctamente";
        }

        return "El usuario no tenía asignado ese rol";
    }
}
