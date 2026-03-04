package com.jorge.sprintdef.services;

import com.jorge.sprintdef.Rol;
import com.jorge.sprintdef.RolRepository;
import com.jorge.sprintdef.User;
import com.jorge.sprintdef.dto.RolPostUser;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;
import com.jorge.sprintdef.UserRepository;

import com.jorge.sprintdef.exceptions.*;
import com.jorge.sprintdef.mapping.RolMapper;
import com.jorge.sprintdef.mapping.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea una nueva instancia del servicio de usuarios inyectando sus dependencias.
     * Inicializa el repositorio de usuarios, el mapper de usuarios y el repositorio de roles
     * necesarios para las operaciones del servicio.
     *
     * @param userRep repositorio para realizar operaciones de persistencia sobre usuarios
     * @param userMap mapper encargado de transformar entidades de usuario y sus DTOs
     * @param rolRep repositorio para la gestión de la persistencia de roles asociados a usuarios
     */
    public UserService(UserRepository userRep, UserMapper userMap, RolRepository rolRep, PasswordEncoder passwordEncoder, RolMapper rolMapper) {
        this.userRep = userRep;
        this.userMap = userMap;
        this.rolRep = rolRep;
        this.passwordEncoder = passwordEncoder;
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
        User usuarioEncontrado = userRep.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + id));

        if (!usuarioEncontrado.getActivo()) {
            throw new NotFoundException("El usuario con ID " + id + " está desactivado y no se puede mostrar.");
        }
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
        List <Rol> roles=new ArrayList<>();
        if ((userRep.findUserByEmailUsuario(usuario.getEmailUsuario()))!=null) {
            // Lanzamos una excepción controlada que luego podemos capturar para mostrar un error 400 al cliente
            throw new DuplicateException("El correo electrónico ya está en uso.");
        }

        User usuario2= userMap.userAddDTO(usuario);
        usuario2.setContrasenhaUsuario(passwordEncoder.encode(usuario.getContrasenhaUsuario()));

        Rol rolN=rolRep.findByName(("alumno")).orElseThrow(() -> new NotFoundException("El rol introducido no existe en el sistema."));
        roles.add(rolN);
        usuario2.setRoles(roles);
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

        User userUpdate = userRep.findById(id).orElseThrow(() -> new NotFoundException("El usuario introducido no existe en el sistema."));
        if (!userUpdate.getActivo()) {
            throw new ConflictException("No se puede actualizar un usuario desactivado.");
        }

        if(rolEditor.equalsIgnoreCase("admin") || (rolEditor.equalsIgnoreCase("administrador"))){
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
            throw new BadRequestException("Rol de editor no válido.");
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
        User userSelect=userRep.findById(id).orElseThrow(() -> new NotFoundException("El usuario introducido no existe en el sistema."));
        String salida;
        userSelect.setActivo(false);
        userRep.save(userSelect);
        salida= ("Usuario con id "+id+" borrado correctamente");
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
        User encontrado=userRep.findById(idUser).orElseThrow(() -> new NotFoundException("El usuario introducido no existe en el sistema."));
        roles = encontrado.getRoles();
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
        User encontrado=userRep.findById(idUser).orElseThrow(() -> new NotFoundException("El usuario introducido no existe en el sistema."));
        Rol rolN=rolRep.findById(idRol.getIdRol()).orElseThrow(() -> new NotFoundException("El rol introducido no existe en el sistema."));
        List<Rol> roles = encontrado.getRoles();
        for (Rol r:roles){
            if(Objects.equals(r.getIdRol(), idRol.getIdRol())){
                throw new DuplicateException("Error: El usuario ya tiene ese rol.");
            }
        }
        roles.add(rolN);
        encontrado.setRoles(roles);
        userRep.save(encontrado);
        salida= "Rol con id "+rolN.getIdRol()+" añdadido correctamente a usuario con id "+idUser;
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

        User usuario = userRep.findById(idUser).orElseThrow(() -> new NotFoundException("El usuario introducido no existe en el sistema."));


        Rol rolN = rolRep.findById(idRol).orElseThrow(() -> new NotFoundException("El rol introducido no existe en el sistema."));


        //Acción directa: removeIf hace el bucle y el borrado de forma segura, guarda el resultado en un boolean
        boolean rolBorrado = usuario.getRoles().removeIf(rol ->
                Objects.equals(rol.getIdRol(), rolN.getIdRol())
        );

        if (rolBorrado) {
            userRep.save(usuario);
            return "Rol con id "+idRol+" eliminado correctamente del usuario con id"+idUser;
        }
        throw new NotFoundException("Error: El usuario no tenía asignado ese rol.");
    }
}
