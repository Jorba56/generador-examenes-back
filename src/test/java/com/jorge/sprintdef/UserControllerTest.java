package com.jorge.sprintdef;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)

class UserControllerTest {

    @Mock
    private UserRepository userRep;

    @InjectMocks
    private UserController userController;

    @Test
     void getAllUsers(){
        User usuario=new User();
        usuario.setNombreUsuario("jorge");
        usuario.setApellidoUsuario("br");
        usuario.setContrasenhaUsuario("jorge12345");
        usuario.setEmailUsuario("jobr@gmail.com");
        usuario.setActivo(true);

        given(userRep.findAll()).willReturn(List.of(usuario));

        //when
        List<User> userList=userController.getAllUsers();

        assertFalse(userList.isEmpty());
        assertEquals((1), userList.size());
        verify(userRep).findAll();
        verifyNoMoreInteractions(userRep); // ver si no se ejecuta mas veces
    }

    @Test
     void getUserId(){
        User usuario=new User();
        usuario.setIdUser(1L);
        usuario.setNombreUsuario("jorge");
        usuario.setApellidoUsuario("br");
        usuario.setContrasenhaUsuario("jorge12345");
        usuario.setEmailUsuario("jobr@gmail.com");
        usuario.setActivo(true);

        given(userRep.findById(1L)).willReturn(Optional.of(usuario));

        //when
        User userFind = userController.getUserId(1L);

        assertNotNull(userFind);
        assertEquals(("jorge"), userFind.getNombreUsuario());
    }

    @Test
     void addUser(){
        User usuario=new User();
        usuario.setIdUser(1L);
        usuario.setNombreUsuario("jorge");
        usuario.setApellidoUsuario("br");
        usuario.setContrasenhaUsuario("jorge12345");
        usuario.setEmailUsuario("jobr@gmail.com");
        usuario.setActivo(true);

        given(userRep.save(usuario)).willReturn(usuario);

        //when
        String correcto=userController.addUser(usuario);

        assertNotNull(correcto);
        assertEquals(("usuario añadido con exito"),correcto);
        verify(userRep).save(usuario);
        verifyNoMoreInteractions(userRep); // ver si no se ejecuta mas veces
    }

    @Test
     void updateUser(){
            List <Long> rolesLista= new ArrayList<>();
            rolesLista.add(1L);
            rolesLista.add(2L);
            User usuario=new User();
            usuario.setIdUser(1L);
            usuario.setNombreUsuario("jorge");
            usuario.setApellidoUsuario("br");
            usuario.setContrasenhaUsuario("jorge12345");
            usuario.setEmailUsuario("jobr@gmail.com");
            usuario.setRolId(rolesLista);
            usuario.setActivo(true);

            rolesLista.add(3L);
            User usuario2=new User();
            usuario2.setIdUser(2L);
            usuario2.setNombreUsuario("jorgete");
            usuario2.setApellidoUsuario("rubio");
            usuario2.setContrasenhaUsuario("jorge0987612345");
            usuario2.setEmailUsuario("jorbarri@gmail.com");
            usuario2.setRolId(rolesLista);
            usuario2.setActivo(true);

            given(userRep.findById(1L)).willReturn(Optional.of(usuario));

            //when
            String correcto=userController.updateUser(1L,usuario2);

            assertNotNull(correcto);
            assertEquals(("Usuario editado correctamente"),correcto);
    }

    @Test
     void deleteUser(){
            User usuario=new User();
            usuario.setIdUser(1L); // no necesito más

            //when
            String borrado = userController.deleteUser(1L);

            //asserts
            assertNotNull(borrado);
            assertEquals("usuario borrado con exito", borrado);

            //verificar que realmente se ha borrado de la base de datos
            verify(userRep).deleteById(1L);
            verifyNoMoreInteractions(userRep); // ver si no se ejecuta mas veces
        }
    }