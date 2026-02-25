package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.RolDTO;
import com.jorge.sprintdef.services.RolService;
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

class RolServiceTest {
    @Mock
    private RolRepository rolRep;

    @InjectMocks
    private RolService rolService;

    @Test
     void getAllRoles(){
        Rol rol1=new Rol();
        rol1.setName("administrador");
        rol1.setActivo(false);
        rol1.setIdRol(4L);

        given(rolRep.findRolsByActivoIs(true)).willReturn(List.of(rol1));

        List <Rol> rolList=rolService.listarRoles();

        assertFalse(rolList.isEmpty());
        assertEquals(1,rolList.size());
        verify(rolRep).findRolsByActivoIs(true);
        verifyNoMoreInteractions(rolRep);
    }

    @Test
    void getAllRolesVacio(){
        given(rolRep.findRolsByActivoIs(true)).willReturn(List.of());

        List <Rol> rolList=rolService.listarRoles();

        assertTrue(rolList.isEmpty());
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

        Rol rolSearch=rolService.rolPorId(rol1.getIdRol()).orElse(null);

        assertNotNull(rolSearch);
        assertEquals(rol1.getIdRol(),rolSearch.getIdRol());
        verify(rolRep).findById(rol1.getIdRol());
        verifyNoMoreInteractions(rolRep);
    }

    @Test
    void getRolIdNull(){
        given(rolRep.findById(5L)).willReturn(Optional.empty());

        Optional<Rol> rolSearch=rolService.rolPorId(5L);

        assertEquals(Optional.empty(), rolSearch);
        verify(rolRep).findById(5L);
        verifyNoMoreInteractions(rolRep);
    }

    @Test
     void addRol(){
        RolDTO rol=new RolDTO();
        Rol rol1= rolService.mappingARol(rol);
        given(rolRep.save(any(Rol.class))).willReturn(rol1);

        String correcto=rolService.newRol(rol);

        assertNotNull(correcto);
        assertEquals(("rol añadido con exito"),correcto);
        verify(rolRep).save(any(Rol.class));
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

        String correcto=rolService.actualizarRol(4L,rol2);

        assertNotNull(correcto);
        assertEquals(("Rol editado correctamente"),correcto);
        assertEquals(("administrador2"),rol1.getName());
        verify(rolRep).save(rol1); //comprobar que en la base de datos se guarda y se actualiza
    }

    @Test
    void updateRolNull(){

        Rol rol2=new Rol();
        rol2.setName("administrador2");
        rol2.setActivo(true);
        rol2.setIdRol(5L);

        given(rolRep.findById(99L)).willReturn(Optional.empty());

        String fallo=rolService.actualizarRol(99L,rol2);

        assertNotNull(fallo);
        assertEquals(("Error: Rol no encontrado"),fallo);
    }

    @Test
     void deleteRol(){
        Rol rol=new Rol();
        rol.setIdRol(6L);
        rol.setActivo(true);// no necesito más

        given(rolRep.findById(6L)).willReturn(Optional.of(rol));
        //when
        String borrado = rolService.desactivarRol(6L);

        //asserts
        assertFalse(rol.getActivo());
        assertNotNull(borrado);
        assertEquals("rol borrado con exito", borrado);
        verify(rolRep).save(rol);
    }

    @Test
    void deleteRolNull() {
        // Obligamos a Mockito a devolver vacío
        given(rolRep.findById(99L)).willReturn(Optional.empty());

        // WHEN
        String borrado = rolService.desactivarRol(99L);

        //devuelve el mensaje, pero la base de datos nunca guardó nada
        assertNotNull(borrado);
        assertEquals("rol borrado con exito", borrado);

        //asegurarme de que nunca se haya usado el metodo "save" para ninguna clase "Rol"
        verify(rolRep, never()).save(any(Rol.class));

    }
}
