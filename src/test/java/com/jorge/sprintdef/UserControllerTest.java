package com.jorge.sprintdef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
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
    public void getAllUsers(){
        User usuario=new User();
        usuario.setNombre_usuario("jorge");
        usuario.setApellido_usuario("br");
        usuario.setContrasenha_usuario("jorge12345");
        usuario.setEmail_usuario("jobr@gmail.com");
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
    public void getUserId(){
        User usuario=new User();
        usuario.setId(1L);
        usuario.setNombre_usuario("jorge");
        usuario.setApellido_usuario("br");
        usuario.setContrasenha_usuario("jorge12345");
        usuario.setEmail_usuario("jobr@gmail.com");
        usuario.setActivo(true);

        given(userRep.findById(1L)).willReturn(Optional.of(usuario));

        //when
        User userFind = userController.getUserId(1L);

        assertNotNull(userFind);
        assertEquals(("jorge"), userFind.getNombre_usuario());
    }

    @Test
    public void addUser(){
        User usuario=new User();
        usuario.setId(1L);
        usuario.setNombre_usuario("jorge");
        usuario.setApellido_usuario("br");
        usuario.setContrasenha_usuario("jorge12345");
        usuario.setEmail_usuario("jobr@gmail.com");
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
    public void updateUser(){
            List <Integer> Roles= new ArrayList<>();
            Roles.add(1);
            Roles.add(2);
            User usuario=new User();
            usuario.setId(1L);
            usuario.setNombre_usuario("jorge");
            usuario.setApellido_usuario("br");
            usuario.setContrasenha_usuario("jorge12345");
            usuario.setEmail_usuario("jobr@gmail.com");
            usuario.setRol_id(Roles);
            usuario.setActivo(true);

            Roles.add(3);
            User usuario2=new User();
            usuario2.setId(2L);
            usuario2.setNombre_usuario("jorgete");
            usuario2.setApellido_usuario("rubio");
            usuario2.setContrasenha_usuario("jorge0987612345");
            usuario2.setEmail_usuario("jorbarri@gmail.com");
            usuario2.setRol_id(Roles);
            usuario2.setActivo(true);

            given(userRep.findById(1L)).willReturn(Optional.of(usuario));

            //when
            String correcto=userController.updateUser(1L,usuario2);

            assertNotNull(correcto);
            assertEquals(("Usuario editado correctamente"),correcto);
    }

    @Test
    public void deleteUser(){
            User usuario=new User();
            usuario.setId(1L); // no necesito más

            //when
            String borrado = userController.deleteUser(1L);

            //asserts
            assertNotNull(borrado);
            assertEquals("usuario borrado con exito", borrado);

            //verificar que realmente se ha borrado
            verify(userRep).deleteById(1L);
            verifyNoMoreInteractions(userRep); // ver si no se ejecuta mas veces
        }
    }




