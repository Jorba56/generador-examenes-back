package com.jorge.sprintdef;


import com.jorge.sprintdef.dto.*;

import com.jorge.sprintdef.exceptions.DuplicateException;
import com.jorge.sprintdef.exceptions.NotFoundException;
import com.jorge.sprintdef.mapping.UserMapper;
import com.jorge.sprintdef.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
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

        given(userRepository.findById(99L)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.buscarPorId(99L)
        );

        assertEquals("Usuario no encontrado con ID: "+99L, ex.getMessage());

        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserId_Inactivo() {
        User usuario = new User();
        usuario.setIdUser(1L);
        usuario.setActivo(false); // Simulamos que está inactivo

        given(userRepository.findById(1L)).willReturn(Optional.of(usuario));

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.buscarPorId(1L)
        );

        assertEquals("El usuario con ID 1 está desactivado y no se puede mostrar.", ex.getMessage());
    }

    @Test
    void addUser() {
        // 1. Preparar DTO de entrada (CON LOS DATOS YA PUESTOS DESDE EL PRINCIPIO)
        UserAddDTO userdto = new UserAddDTO();
        userdto.setEmailUsuario("mbappe09@gmail.com");
        userdto.setContrasenhaUsuario("123abc");
        userdto.setNombreUsuario("Kylian");

        // 2. Preparar el Usuario "real" que creará el mapper
        User usuarioU = new User();
        usuarioU.setNombreUsuario("Kylian");

        // 3. Preparar el Rol que sacaremos de la BD
        Rol alumno = new Rol();
        alumno.setIdRol(1L);
        alumno.setName("alumno");

        // 4. Preparar el DTO final que se devuelve al cliente
        UsersAllDTO userdto2 = new UsersAllDTO();
        userdto2.setNombreUsuario("Kylian");

        // 5. Educar a los mocks en el orden correcto
        given(userRepository.findUserByEmailUsuario(userdto.getEmailUsuario())).willReturn(null);
        given(userMap.userAddDTO(userdto)).willReturn(usuarioU);
        given(passwordEncoder.encode(userdto.getContrasenhaUsuario())).willReturn("claveEncriptada");
        given(rolRep.findByName("alumno")).willReturn(Optional.of(alumno));
        given(userRepository.save(usuarioU)).willReturn(usuarioU);
        given(userMap.mappingADTO(usuarioU)).willReturn(userdto2);

        // 6. Ejecutamos el método del servicio
        UsersAllDTO respuesta = userService.addUsuario(userdto);

        // 7. Comprobaciones (Aserciones)
        assertNotNull(respuesta);
        assertEquals("Kylian", respuesta.getNombreUsuario());

        // ¡Comprobamos que las líneas que te faltaban hacen su trabajo!
        assertFalse(usuarioU.getRoles().isEmpty());
        assertEquals("alumno", usuarioU.getRoles().getFirst().getName());
        assertEquals("claveEncriptada", usuarioU.getContrasenhaUsuario());

        // Verificamos que se llamó al guardado de la BD
        verify(userRepository).save(usuarioU);
    }

    @Test
    void addUser_EmailDuplicado() {
        UserAddDTO userdto = new UserAddDTO();
        userdto.setEmailUsuario("duplicado@gmail.com");

        // Simulamos que el repositorio ya encuentra a alguien con ese correo
        given(userRepository.findUserByEmailUsuario("duplicado@gmail.com")).willReturn(new User());

        DuplicateException ex = assertThrows(
                DuplicateException.class,
                () -> userService.addUsuario(userdto)
        );

        assertEquals("El correo electrónico ya está en uso.", ex.getMessage());
        verify(userRepository, never()).save(any()); // Comprobamos que no se guardó
    }

    @Test
    void addUser_RolAlumnoNoExiste() {
        UserAddDTO userdto = new UserAddDTO();
        userdto.setEmailUsuario("nuevo@gmail.com");
        userdto.setContrasenhaUsuario("123");

        User usuarioMapeado = new User();

        given(userRepository.findUserByEmailUsuario(anyString())).willReturn(null);
        given(userMap.userAddDTO(userdto)).willReturn(usuarioMapeado);
        given(passwordEncoder.encode(anyString())).willReturn("encriptada");

        // Simulamos que el rol "alumno" no existe en la BD
        given(rolRep.findByName("alumno")).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.addUsuario(userdto)
        );

        assertEquals("El rol introducido no existe en el sistema.", ex.getMessage());
    }
/*
    @Test
    void updateUser() {
        User user1 = new User();
        user1.setNombreUsuario("administrador");
        user1.setActivo(true);
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
    void updateUserInactivo() {
        User user1 = new User();
        user1.setNombreUsuario("administrador");
        user1.setActivo(false);
        user1.setIdUser(4L);

        User user2 = new User();
        user2.setNombreUsuario("administrador2");
        user2.setActivo(true);
        user2.setIdUser(5L);

        given(userRepository.findById(user1.getIdUser())).willReturn(Optional.of(user1));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> userService.actualizarUsuario("admin", 4L, user2)
        );

        assertEquals("No se puede actualizar un usuario desactivado.", ex.getMessage());
    }

    @Test
    void actualizarUsuarioNoPermitido() {
        // GIVEN: Un usuario con datos inventados
        User usuario = new User();
        usuario.setIdUser(4L);

        given(userRepository.findById(4L)).willReturn(Optional.of(usuario));

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> userService.actualizarUsuario("alumno", 4L, usuario)
        );

        // THEN: Comprobamos que entra por el "else" y da el mensaje de error correcto
        assertEquals("Rol de editor no válido.", ex.getMessage());
    }

    @Test
    void updateUserNull() {

        User user2 = new User();
        user2.setNombreUsuario("administrador2");
        user2.setActivo(true);
        user2.setIdUser(5L);

        given(userRepository.findById(99L)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.actualizarUsuario("admin",99L, user2)
        );

        assertEquals("El usuario introducido no existe en el sistema.", ex.getMessage());
    }
*/
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
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.desactivarUsuario(99L)
        );

        assertEquals("El usuario introducido no existe en el sistema.", ex.getMessage());
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

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.rolesUser(99L)
        );

        assertEquals("El usuario introducido no existe en el sistema.", ex.getMessage());
        // THEN: Devuelve una lista vacía y no da error
        verify(userRepository).findById(99L);
    }

    @Test
    void addRolUser_UserNotFound() {
        RolPostUser rolPost = new RolPostUser();
        rolPost.setIdRol(2L);

        //El usuario 99 no existe
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.addRolUser(99L, rolPost)
        );

        // THEN: Comprobamos el mensaje de error
        assertEquals("El usuario introducido no existe en el sistema.", ex.getMessage());

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

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.addRolUser(1L, rolDto)
        );

        // Verifica el mensaje de la excepción
        assertEquals("El rol introducido no existe en el sistema.", ex.getMessage());
        verify(userRepository , never()).save(any(User.class));
    }

    @Test
    void deleteRolUser_UserNotFound() {
        // GIVEN: El usuario no existe
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.deleteRolUser(99L, 2L)
        );

        // Verifica el mensaje de la excepción
        assertEquals("El usuario introducido no existe en el sistema.", ex.getMessage());
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

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.deleteRolUser(1L, 99L)
        );

        // Verifica el mensaje de la excepción
        assertEquals("El rol introducido no existe en el sistema.", ex.getMessage());
        // Comprobamos que nunca hace "save" por error
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteRolUser_RolNotAssigned() {
        // GIVEN: El usuario existe y tiene un rol, pero es DISTINTO al que queremos borrar
        User usuario = new User();
        usuario.setIdUser(1L);

        Rol rolDistinto = new Rol();
        rolDistinto.setIdRol(8L); // Diferente a 2L

        List<Rol> roles = new ArrayList<>();
        roles.add(rolDistinto);
        usuario.setRoles(roles);

        // El rol que queremos borrar
        Rol rolAEliminar = new Rol();
        rolAEliminar.setIdRol(2L);

        given(userRepository.findById(1L)).willReturn(Optional.of(usuario));
        given(rolRep.findById(2L)).willReturn(Optional.of(rolAEliminar));

        // WHEN
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userService.deleteRolUser(1L, 2L)
        );

        // THEN
        assertEquals("Error: El usuario no tenía asignado ese rol.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addRolUser_Exito_ListaVacia() {
        // ESCENARIO 1: Usuario totalmente nuevo sin roles (El bucle FOR se salta)
        User usuario = new User();
        usuario.setIdUser(1L);
        usuario.setRoles(new ArrayList<>()); // Lista vacía

        Rol rolNuevo = new Rol();
        rolNuevo.setIdRol(2L);
        rolNuevo.setName("rol");

        RolPostUser rolPost = new RolPostUser();
        rolPost.setIdRol(2L);

        given(userRepository.findById(1L)).willReturn(Optional.of(usuario));
        given(rolRep.findById(2L)).willReturn(Optional.of(rolNuevo));

        String resultado = userService.addRolUser(1L, rolPost);

        assertEquals("Rol con id 2 añdadido correctamente a usuario con id 1", resultado);
        assertEquals(1, usuario.getRoles().size());
    }

    @Test
    void addRolUser_Exito_ConRolDistinto() {
        // ESCENARIO 2: Usuario ya tiene un rol, pero es distinto (El IF da FALSE)
        User usuario = new User();
        usuario.setIdUser(1L);

        Rol rolPrevio = new Rol();
        rolPrevio.setIdRol(8L); // ID distinto (8 vs 2)

        List<Rol> listaRoles = new ArrayList<>();
        listaRoles.add(rolPrevio);
        usuario.setRoles(listaRoles);

        Rol rolNuevo = new Rol();
        rolNuevo.setIdRol(2L);

        RolPostUser rolPost = new RolPostUser();
        rolPost.setIdRol(2L);

        given(userRepository.findById(1L)).willReturn(Optional.of(usuario));
        given(rolRep.findById(2L)).willReturn(Optional.of(rolNuevo));

        String resultado = userService.addRolUser(1L, rolPost);

        assertEquals("Rol con id 2 añdadido correctamente a usuario con id 1", resultado);
        assertEquals(2, usuario.getRoles().size()); // Ahora tiene 2 roles
    }

    @Test
    void addRolUser_Error_RolYaAsignado() {
        // ESCENARIO 3: El usuario ya tiene ese mismo rol exacto (El IF da TRUE)
        User usuario = new User();
        usuario.setIdUser(1L);

        Rol rolPrevio = new Rol();
        rolPrevio.setIdRol(2L); // ID idéntico (2 vs 2)

        List<Rol> rolesActuales = new ArrayList<>();
        rolesActuales.add(rolPrevio);
        usuario.setRoles(rolesActuales);

        Rol rolEnBaseDeDatos = new Rol();
        rolEnBaseDeDatos.setIdRol(2L);

        RolPostUser idRolEntrada = new RolPostUser();
        idRolEntrada.setIdRol(2L);

        given(userRepository.findById(1L)).willReturn(Optional.of(usuario));
        given(rolRep.findById(2L)).willReturn(Optional.of(rolEnBaseDeDatos));

        DuplicateException ex = assertThrows(
                DuplicateException.class,
                () -> userService.addRolUser(1L, idRolEntrada)
        );

        assertEquals("Error: El usuario ya tiene ese rol.", ex.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Verificamos que no guardó nada
    }
}