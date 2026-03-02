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

/**
 * Servicio encargado de gestionar la lógica de negocio relacionada con los Usuarios.
 * Actúa como intermediario entre el controlador y la base de datos,
 * procesando las transformaciones de DTOs y validando las reglas de negocio.
 */
@Service
public class UserService {

    private final RolRepository rolRep;
    private final UserRepository userRep;
    private final UserMapper userMap;
    /**
     * Crea una nueva instancia del servicio de usuarios inyectando sus dependencias.
     * Inicializa el repositorio de usuarios, el mapper de usuarios y el repositorio de roles
     * necesarios para las operaciones del servicio.
     *
     * @param userRep repositorio para realizar operaciones de persistencia sobre usuarios
     * @param userMap mapper encargado de transformar entidades de usuario y sus DTOs
     * @param rolRep repositorio para la gestión de la persistencia de roles asociados a usuarios
     */
    public UserService(UserRepository userRep, UserMapper userMap, RolRepository rolRep) {
        this.userRep = userRep;
        this.userMap = userMap;
        this.rolRep = rolRep;
    }

    /**
     * Obtiene todos los usuarios que están activos y los proyecta a {@code UsersAllDTO}.
     *
     * @return lista de usuarios activos convertidos a {@code UsersAllDTO} para su correcta visualización; si no hay, una lista vacía
     */
    public List<UsersAllDTO> listarUsuarios(){
        List<User> encontrados = userRep.findUsersByActivoIs(true);
        List<UsersAllDTO> usuarios=new ArrayList<>();
        for (User encontrado : encontrados) usuarios.add(userMap.mappingADTO(encontrado));
        return usuarios;
    }

    /**
     * Busca un usuario por su identificador y lo proyecta a {@code UserIdDTo}.
     * Si no existe, devuelve {@code null}.
     *
     * @param id identificador del usuario a consultar
     * @return representación {@code UserIdDTo} del usuario encontrado o {@code null} si no existe
     */
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

    /**
     * Crea y persiste un nuevo usuario a partir de los datos proporcionados en {@code UserAddDTO}.
     * Devuelve la representación del usuario persistido como {@code UsersAllDTO}.
     *
     * @param usuario DTO con la información del usuario a crear
     * @return el usuario creado proyectado a {@code UsersAllDTO}
     */
    public UsersAllDTO addUsuario (UserAddDTO usuario){
        User usuario2= userMap.userAddDTO(usuario);
        userRep.save(usuario2);
        return(userMap.mappingADTO(usuario2));
    }

    /**
     * Actualiza los datos de un usuario existente siempre que el editor tenga rol de administrador.
     * Se consideran válidos los valores de {@code rolEditor} "admin" o "administrador" (ignorando mayúsculas/minúsculas).
     * Actualiza datos básicos y la colección de roles del usuario.
     *
     * @param rolEditor rol de quien solicita la edición, usado para autorizar la operación
     * @param id identificador del usuario a actualizar
     * @param usuario entidad con los nuevos valores a aplicar
     * @return mensaje indicando el resultado: éxito, usuario no encontrado o editor no autorizado
     */
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

        salida="Usuario con id "+id+" editado correctamente";
        } else {
            salida="Rol de editor no válido. Solo el administrador puede editar usuarios.";
        }
        return salida;
    }

    /**
     * Desactiva (borrado lógico) un usuario estableciendo su campo {@code activo} a {@code false}.
     *
     * @param id identificador del usuario a desactivar
     * @return mensaje indicando si el usuario fue desactivado o si no existe
     */
    public String desactivarUsuario (Long id){
        User userSelect=userRep.findById(id).orElse(null);
        String salida;
        if (userSelect!=null) {
            userSelect.setActivo(false);
            userRep.save(userSelect);
            salida= ("Usuario con id "+id+" borrado correctamente");
        }else{ salida= "No existe un usuario con el id "+id;}
        return salida;
    }

    /**
     * Recupera los roles asignados a un usuario.
     * Si el usuario no existe, devuelve una lista vacía.
     *
     * @param idUser identificador del usuario del que se desean consultar los roles
     * @return lista de roles asignados al usuario; vacía si el usuario no existe
     */
    public List<Rol>rolesUser(Long idUser){
        List<Rol> roles=new ArrayList<>();
        User encontrado=userRep.findById(idUser).orElse(null);
        if (encontrado!=null) { roles = encontrado.getRoles();}
        return roles;
    }

    /**
     * Añade un rol existente a la colección de roles de un usuario.
     *
     * @param idUser identificador del usuario al que se le asignará el rol
     * @param idRol DTO que contiene el identificador del rol a asignar
     * @return mensaje indicando si la asignación se realizó correctamente o si no se encontró usuario/rol
     */
    public String addRolUser(Long idUser, RolPostUser idRol){
        String salida;
        User encontrado=userRep.findById(idUser).orElse(null);
        Rol rolN=rolRep.findById(idRol.getIdRol()).orElse(null);
        if (encontrado!=null && rolN!=null) {
            List<Rol> roles = encontrado.getRoles();
            roles.add(rolN);
            encontrado.setRoles(roles);
            userRep.save(encontrado);
            salida= "Rol con id "+rolN.getIdRol()+" añdadido correctamente a usuario con id "+idUser;
        } else{ salida="Usuario/Rol no encontrado";}
        return salida;
    }

    /**
     * Elimina la asociación de un rol con un usuario.
     * Si el usuario y el rol existen y el rol está asignado al usuario, se elimina y se persiste el cambio.
     *
     * @param idUser identificador del usuario al que se le desea eliminar el rol
     * @param idRol identificador del rol que se desea quitar del usuario
     * @return mensaje indicando el resultado: usuario/rol no encontrado, rol no asignado o éxito en la eliminación
     */
    public String deleteRolUser(Long idUser, Long idRol) {

        User usuario = userRep.findById(idUser).orElse(null);
        if (usuario == null) {
            return "Usuario no encontrado";
        }

        Rol rolN = rolRep.findById(idRol).orElse(null);
        if (rolN == null) {
            return "Ese rol no existe en la base de datos";
        }

        //Acción directa: removeIf hace el bucle y el borrado de forma segura, guarda el resultado en un boolean
        boolean rolBorrado = usuario.getRoles().removeIf(rol ->
                Objects.equals(rol.getIdRol(), rolN.getIdRol())
        );

        if (rolBorrado) {
            userRep.save(usuario);
            return "Rol con id "+idRol+" eliminado correctamente del usuario con id"+idUser;
        }
        return "El usuario no tenía asignado ese rol";
    }
}
