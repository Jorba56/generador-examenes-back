package com.jorge.sprintdef;


import com.jorge.sprintdef.dto.*;
import com.jorge.sprintdef.mapping.UserMapper;
import com.jorge.sprintdef.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class UserServiceTest {

    @Mock // <-- ¡Añade esto si no lo tienes!
    private RolRepository rolRep;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMap;

    @InjectMocks
    private UserService userService;


    @Test
    void getAllUsers() {
        User usuario = new User();
        UsersAllDTO dtoFalso = new UsersAllDTO();

        given(userRepository.findUsersByActivoIs(true)).willReturn(List.of(usuario));
        given(userMap.mappingADTO(usuario)).willReturn(dtoFalso);

        List<UsersAllDTO> rolList = userService.listarUsuarios();

        assertFalse(rolList.isEmpty());
        assertEquals(1, rolList.size());
        verify(userMap).mappingADTO(usuario);
        verify(userRepository).findUsersByActivoIs(true);
        verifyNoMoreInteractions(userRepository);
    }


    @Test
    void getAllUsersVacio() {

        given(userRepository.findUsersByActivoIs(true)).willReturn(List.of());


        List<UsersAllDTO> rolList = userService.listarUsuarios();

        assertTrue(rolList.isEmpty());
        verify(userRepository).findUsersByActivoIs(true);
        verifyNoMoreInteractions(userRepository);
    }


    @Test
    void getUserId() {
        Long id = 1L;
        User usuario = new User();
        usuario.setIdUser(id);
        usuario.setNombreUsuario("Jorge");

        UserIdDTo dtoEsperado = new UserIdDTo();
        dtoEsperado.setNombreUsuario("Jorge");

        given(userRepository.findById(1L)).willReturn(Optional.of(usuario));
        given(userMap.userToIdDTO(usuario)).willReturn(dtoEsperado);

        //when
        UserIdDTo userFind = userService.buscarPorId(1L);

        assertNotNull(userFind);
        assertEquals(("Jorge"), userFind.getNombreUsuario());
    }


    @Test
    void getUserIdNull() {

        given(userRepository.findById(5L)).willReturn(Optional.empty());

        UserIdDTo userFind = userService.buscarPorId(5L);

        assertNull(userFind);
        verify(userRepository).findById(5L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void addUser() {
        UserAddDTO userdto = new UserAddDTO();
        UsersAllDTO userdto2= new UsersAllDTO();
        User usuarioU = userMap.userAddDTO(userdto);
        given(userRepository.save(usuarioU)).willReturn(usuarioU);
        given(userMap.userAddDTO(userdto)).willReturn(usuarioU);
        given(userMap.mappingADTO(usuarioU)).willReturn(userdto2);

        userdto2 = userService.addUsuario(userdto);

        assertNotNull(userdto2);
        assertEquals(userdto2.getNombreUsuario(), userdto.getNombreUsuario());
        verify(userRepository).save(usuarioU);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser() {
        User user1 = new User();
        user1.setNombreUsuario("administrador");
        user1.setActivo(false);
        user1.setIdUser(4L);

        User user2 = new User();
        user2.setNombreUsuario("administrador2");
        user2.setActivo(true);
        user2.setIdUser(5L);

        given(userRepository.findById(user1.getIdUser())).willReturn(Optional.of(user1));

        String correcto = userService.actualizarUsuario("admin", 4L, user2);

        String correcto2 = userService.actualizarUsuario("administrador", 4L, user2);

        assertNotNull(correcto);
        assertEquals(("Usuario con id "+4L +" editado correctamente"), correcto);
        assertEquals(("administrador2"), user1.getNombreUsuario());

        assertNotNull(correcto2);
        assertEquals(("Usuario con id "+4L +" editado correctamente"), correcto2);
        assertEquals(("administrador2"), user1.getNombreUsuario());
    }

    @Test
    void actualizarUsuarioNoPermitido() {
        // GIVEN: Un usuario con datos inventados
        User usuario = new User();

        // WHEN: Llamamos al servicio con un rol que NO es "admin" ni "administrador"
        String salida = userService.actualizarUsuario("alumno", 4L, usuario);

        // THEN: Comprobamos que entra por el "else" y da el mensaje de error correcto
        assertNotNull(salida);
        assertEquals("Rol de editor no válido. Solo el administrador puede editar usuarios.", salida);

        // Comprobamos que ni siquiera intentó buscar en la base de datos
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateUserNull() {

        User user2 = new User();
        user2.setNombreUsuario("administrador2");
        user2.setActivo(true);
        user2.setIdUser(5L);

        given(userRepository.findById(99L)).willReturn(Optional.empty());

        String fallo = userService.actualizarUsuario("admin", 99L, user2);

        assertNotNull(fallo);
        assertEquals(("Error: Usuario no encontrado"), fallo);
    }

    @Test
    void deleteUser(){
        User user=new User();
        user.setIdUser(6L);
        user.setActivo(true);// no necesito más

        given(userRepository.findById(6L)).willReturn(Optional.of(user));
        //when
        String borrado = userService.desactivarUsuario(6L);

        //asserts
        assertFalse(user.getActivo());
        assertNotNull(borrado);
        assertEquals("Usuario con id "+6L+" borrado correctamente", borrado);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUserNull() {
        // Obligamos a Mockito a devolver vacío
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // WHEN
        String borrado = userService.desactivarUsuario(99L);

        //devuelve el mensaje, pero la base de datos nunca guardó nada
        assertNotNull(borrado);
        assertEquals("No existe un usuario con el id "+99L, borrado);

        //asegurarme de que nunca se haya usado el metodo "save" para ninguna clase "Rol"
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void rolesUser() {
        User user = new User();
        user.setIdUser(6L);
        user.setActivo(true);

        Rol rolFalso = new Rol();
        rolFalso.setIdRol(1L);
        rolFalso.setName("admin");
        user.setRoles(List.of(rolFalso)); // Añadimos el rol al usuario

        given(userRepository.findById(6L)).willReturn(Optional.of(user));

        List<Rol> roles = userService.rolesUser(user.getIdUser());

        assertNotNull(roles);
        assertFalse(roles.isEmpty()); // Comprobamos que la lista NO viene vacía
        assertEquals(1, roles.size()); // Comprobamos que trae exactamente 1 rol
        assertEquals("admin", roles.getFirst().getName()); // Comprobamos que es el rol correcto

        verify(userRepository).findById(6L);
    }

    @Test
    void addRolUser() {
        //Preparamos un usuario con una lista de roles vacía
        User usuario = new User();
        usuario.setIdUser(1L);
        usuario.setRoles(new ArrayList<>()); // Inicializamos la lista para que no dé null pointer

        //Preparamos el rol que vamos a buscar en base de datos
        Rol rolNuevo = new Rol();
        rolNuevo.setIdRol(2L);
        rolNuevo.setName("rol");

        //Preparamos el DTO que entra por el controlador
        RolPostUser rolPost = new RolPostUser();
        rolPost.setIdRol(2L);

        // Educamos a los mocks
        given(userRepository.findById(1L)).willReturn(Optional.of(usuario));
        given(rolRep.findById(2L)).willReturn(Optional.of(rolNuevo));

        //Ejecutamos el servicio
        String resultado = userService.addRolUser(1L, rolPost);

        //Comprobamos el texto devuelto
        assertNotNull(resultado);
        assertEquals("Rol con id "+rolPost.getIdRol()+" añdadido correctamente a usuario con id "+1L, resultado);

        //Comprobamos que efectivamente el rol se metió en la lista del usuario
        assertFalse(usuario.getRoles().isEmpty());
        assertEquals(2L, usuario.getRoles().getFirst().getIdRol());

        //Comprobamos que se guardó en la base de datos
        verify(userRepository).save(usuario);
        verify(userRepository).findById(1L);
        verify(rolRep).findById(2L);
    }

    @Test
    void deleteRolUser() {
        //Preparamos un rol
        Rol rolAEliminar = new Rol();
        rolAEliminar.setIdRol(2L);

        //Preparamos un usuario que YA TIENE ese rol asignado
        User usuario = new User();
        usuario.setIdUser(1L);
        List<Rol> rolesDelUsuario = new ArrayList<>();
        rolesDelUsuario.add(rolAEliminar);
        usuario.setRoles(rolesDelUsuario);

        // Educamos a los mocks
        given(userRepository.findById(1L)).willReturn(Optional.of(usuario));
        given(rolRep.findById(2L)).willReturn(Optional.of(rolAEliminar));

        // Ejecutamos el servicio
        String resultado = userService.deleteRolUser(1L, 2L);

        //Comprobamos el texto
        assertNotNull(resultado);
        assertEquals("Rol con id "+2L+" eliminado correctamente del usuario con id"+1L, resultado);

        //Comprobamos que la lista del usuario ahora está vacía (se ha borrado)
        assertTrue(usuario.getRoles().isEmpty());

        //Comprobamos que los cambios se guardaron
        verify(userRepository).save(usuario);
        verify(userRepository).findById(1L);
        verify(rolRep).findById(2L);
    }

    @Test
    void rolesUser_UserNotFound() {
        //El usuario 99 no existe
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // WHEN
        List<Rol> roles = userService.rolesUser(99L);

        // THEN: Devuelve una lista vacía y no da error
        assertTrue(roles.isEmpty());
        verify(userRepository).findById(99L);
    }

    @Test
    void addRolUser_UserNotFound() {
        RolPostUser rolPost = new RolPostUser();
        rolPost.setIdRol(2L);

        //El usuario 99 no existe
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // WHEN
        String resultado = userService.addRolUser(99L, rolPost);

        // THEN: Comprobamos el mensaje de error
        assertEquals("Usuario/Rol no encontrado", resultado);

    }

    @Test
    void addRolUser_RolNotFound() {
        // GIVEN
        Long idUser = 1L;
        RolPostUser rolDto = new RolPostUser();
        rolDto.setIdRol(99L);

        User usuarioReal = new User();
        usuarioReal.setIdUser(1L);

        // Educamos a los mocks: El usuario SÍ existe, pero el rol NO existe
        given(userRepository.findById(idUser)).willReturn(Optional.of(usuarioReal));
        given(rolRep.findById(rolDto.getIdRol())).willReturn(Optional.empty());

        // WHEN
        String resultado = userService.addRolUser(idUser, rolDto);

        // THEN: Comprobamos el mensaje de error y la seguridad de la BD
        assertEquals("Usuario/Rol no encontrado", resultado);
        verify(userRepository , never()).save(any(User.class));
    }

    @Test
    void deleteRolUser_UserNotFound() {
        // GIVEN: El usuario no existe
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        String resultado = userService.deleteRolUser(99L, 2L);

        assertEquals("Usuario no encontrado", resultado);
        verifyNoInteractions(rolRep); // No llega a buscar el rol
    }

    @Test
    void deleteRolUser_RolNotFound() {
        //El usuario sí existe
        User usuario = new User();
        usuario.setIdUser(1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(usuario));
        // Pero el rol que intentamos borrar no existe en la BD
        given(rolRep.findById(99L)).willReturn(Optional.empty());

        String resultado = userService.deleteRolUser(1L, 99L);

        assertEquals("Ese rol no existe en la base de datos", resultado);
        // Comprobamos que nunca hace "save" por error
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteRolUser_RolNotAssigned() {
        // El usuario existe pero NO tiene roles (lista vacía)
        User usuario = new User();
        usuario.setIdUser(1L);
        usuario.setRoles(new ArrayList<>());

        // El rol existe
        Rol rol = new Rol();
        rol.setIdRol(2L);

        given(userRepository.findById(1L)).willReturn(Optional.of(usuario));
        given(rolRep.findById(2L)).willReturn(Optional.of(rol));

        // WHEN
        String resultado = userService.deleteRolUser(1L, 2L);

        // THEN
        assertEquals("El usuario no tenía asignado ese rol", resultado);
        verify(userRepository, never()).save(any(User.class)); // Cero mentiras en BD
    }
}