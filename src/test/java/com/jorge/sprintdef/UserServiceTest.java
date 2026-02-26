package com.jorge.sprintdef;


import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;
import com.jorge.sprintdef.mapping.UserMapper;
import com.jorge.sprintdef.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class UserServiceTest {

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
        User usuarioU = userMap.userAddDTO(userdto);
        given(userRepository.save(usuarioU)).willReturn(usuarioU);
        given(userMap.userAddDTO(userdto)).willReturn(usuarioU);

        String correcto = userService.addUsuario(userdto);

        assertNotNull(correcto);
        assertEquals(("usuario añadido con exito"), correcto);
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
        assertEquals(("Usuario editado correctamente"), correcto);
        assertEquals(("administrador2"), user1.getNombreUsuario());

        assertNotNull(correcto2);
        assertEquals(("Usuario editado correctamente"), correcto2);
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
    void updateRolNull() {

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
    void deleteRol(){
        User user=new User();
        user.setIdUser(6L);
        user.setActivo(true);// no necesito más

        given(userRepository.findById(6L)).willReturn(Optional.of(user));
        //when
        String borrado = userService.desactivarUsuario(6L);

        //asserts
        assertFalse(user.getActivo());
        assertNotNull(borrado);
        assertEquals("usuario borrado correctamente", borrado);
        verify(userRepository).save(user);
    }

    @Test
    void deleteRolNull() {
        // Obligamos a Mockito a devolver vacío
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // WHEN
        String borrado = userService.desactivarUsuario(99L);

        //devuelve el mensaje, pero la base de datos nunca guardó nada
        assertNotNull(borrado);
        assertEquals("usuario borrado correctamente", borrado);

        //asegurarme de que nunca se haya usado el metodo "save" para ninguna clase "Rol"
        verify(userRepository, never()).save(any(User.class));

    }
}