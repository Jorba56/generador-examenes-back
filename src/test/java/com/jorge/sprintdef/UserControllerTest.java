package com.jorge.sprintdef;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UsersAllDTO;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
     void getAllUsers(){
        UsersAllDTO usuario=new UsersAllDTO();
        usuario.setNombreUsuario("jorge");
        usuario.setApellidoUsuario("br");
        usuario.setEmailUsuario("jobr@gmail.com");

        given(userService.listarUsuarios()).willReturn(List.of(usuario));

        //when
        List<UsersAllDTO> userList=userController.getAllUsers();

        assertFalse(userList.isEmpty());
        assertEquals((1), userList.size());
        verify(userService).listarUsuarios();
        verifyNoMoreInteractions(userService); // ver si no se ejecuta mas veces
    }

    @Test
     void getUserId(){
        UserIdDTo usuario=new UserIdDTo();
        usuario.setIdUser(1L);
        usuario.setNombreUsuario("jorge");
        usuario.setApellidoUsuario("br");
        usuario.setEmailUsuario("jobr@gmail.com");
        usuario.setActivo(true);

        given(userService.buscarPorId(1L)).willReturn((usuario));

        //when
        UserIdDTo userFind = userController.getUserId(1L);

        assertNotNull(userFind);
        assertEquals(("jorge"), userFind.getNombreUsuario());
    }

    @Test
     void addUser(){

        UserAddDTO usuario=new UserAddDTO();
        usuario.setNombreUsuario("jorge");
        usuario.setApellidoUsuario("br");
        usuario.setContrasenhaUsuario("jorge12345");
        usuario.setEmailUsuario("jobr@gmail.com");

        given(userService.addUsuario(any(UserAddDTO.class))).willReturn("usuario añadido con exito");

        //when
        String correcto=userController.addUser(usuario);

        assertNotNull(correcto);
        assertEquals(("usuario añadido con exito"),correcto);
        verify(userService).addUsuario(usuario);
        verifyNoMoreInteractions(userService); // ver si no se ejecuta mas veces
    }

    @Test
     void updateUser(){

            User usuario2=new User();

            usuario2.setNombreUsuario("jorgete");
            usuario2.setApellidoUsuario("rubio");
            usuario2.setEmailUsuario("jorbarri@gmail.com");
            usuario2.setActivo(true);

            given(userService.actualizarUsuario("admin", 1L, usuario2)).willReturn(("Usuario editado correctamente"));

            //when
            String salida=(userController.updateUser("admin",1L,usuario2));

            assertNotNull(salida);
            assertEquals(("Usuario editado correctamente"),salida);
    }

    @Test
     void deleteUser(){
            User usuario=new User();
            usuario.setIdUser(1L);
            given(userService.desactivarUsuario(usuario.getIdUser())).willReturn(("usuario borrado correctamente"));
            //when
            String borrado = userController.deleteUser(usuario.getIdUser());

            //asserts
            assertNotNull(borrado);
            assertEquals("usuario borrado correctamente", borrado);
        }
    }