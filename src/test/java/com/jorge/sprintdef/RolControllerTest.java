package com.jorge.sprintdef;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)

class RolControllerTest {
    @Mock
    private RolRepository rolRep;

    @InjectMocks
    private RolController rolController;

    @Test
     void getAllRoles(){
        Rol rol1=new Rol();
        rol1.setName("administrador");
        rol1.setActivo(false);
        rol1.setIdRol(4L);

        given(rolRep.findRolsByActivoIs(true)).willReturn(List.of(rol1));

        List <Rol> rolList=rolController.getAllRoles();

        assertFalse(rolList.isEmpty());
        assertEquals(1,rolList.size());
        verify(rolRep).findRolsByActivoIs(true);
        verifyNoMoreInteractions(rolRep);
    }

    @Test
     void getRolId(){
        Rol rol1=new Rol();
        rol1.setName("administrador");
        rol1.setActivo(false);
        rol1.setIdRol(4L);

        given(rolRep.findById(rol1.getIdRol())).willReturn(Optional.of(rol1));

        Rol rolSearch=rolController.getRolId(rol1.getIdRol()).orElse(null);

        assertNotNull(rolSearch);
        assertEquals(rol1.getIdRol(),rolSearch.getIdRol());
        verify(rolRep).findById(rol1.getIdRol());
        verifyNoMoreInteractions(rolRep);
    }

    @Test
     void addRol(){
        Rol rol1=new Rol();
        rol1.setName("administrador");
        rol1.setActivo(false);
        rol1.setIdRol(4L);

        given(rolRep.save(rol1)).willReturn(rol1);

        String correcto=rolController.addRol(rol1);

        assertNotNull(correcto);
        assertEquals(("rol añadido con exito"),correcto);
        verify(rolRep).save(rol1);
        verifyNoMoreInteractions(rolRep);
    }

    @Test
     void updateRol(){
        Rol rol1=new Rol();
        rol1.setName("administrador");
        rol1.setActivo(false);
        rol1.setIdRol(4L);

        Rol rol2=new Rol();
        rol2.setName("administrador2");
        rol2.setActivo(true);
        rol2.setIdRol(5L);

        given(rolRep.findById(rol1.getIdRol())).willReturn(Optional.of(rol1));

        String correcto=rolController.updateRol(4L,rol2);

        assertNotNull(correcto);
        assertEquals(("Rol editado correctamente"),correcto);
        assertEquals(("administrador2"),rol1.getName());
        verify(rolRep).save(rol1); //comprobar que en la base de datos se guarda y se actualiza
    }

    @Test
     void deleteRol(){
        Rol rol=new Rol();
        rol.setIdRol(6L); // no necesito más

        //when
        String borrado = rolController.deleteRol(6L);

        //asserts
        assertNotNull(borrado);
        assertEquals("rol borrado con exito", borrado);

    }
}
