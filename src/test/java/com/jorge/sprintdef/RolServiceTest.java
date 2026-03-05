package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.RolDTO;
import com.jorge.sprintdef.dto.RolPutDTO;
import com.jorge.sprintdef.dto.UserByRol;
import com.jorge.sprintdef.exceptions.NotFoundException;
import com.jorge.sprintdef.mapping.RolMapper;
import com.jorge.sprintdef.mapping.UserMapper;
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

    @Mock
    private UserMapper userMap;

    @Mock
    private RolMapper rolMap;

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
        rol1.setActivo(true);
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

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> rolService.rolPorId(5L)
        );

        assertEquals("Rol no encontrado con ID: "+5L, ex.getMessage());
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
        assertEquals(("Rol añadido con exito"),correcto);
        verify(rolRep).save(any(Rol.class));
        verifyNoMoreInteractions(rolRep);
    }

    @Test
     void updateRol(){
        Rol rol1=new Rol();
        rol1.setName("administrador");
        rol1.setActivo(false);
        rol1.setIdRol(4L);

        RolPutDTO rol2=new RolPutDTO();
        rol2.setName("administrador2");
        rol2.setActivo(true);

        Rol rolMapeado = new Rol();
        rolMapeado.setName("administrador2");
        rolMapeado.setActivo(true);

        given(rolRep.findById(4L)).willReturn(Optional.of(rol1));
        given(rolMap.mappingPutReverse(rol2)).willReturn((rolMapeado));

        String correcto=rolService.actualizarRol(4L,rol2);

        assertNotNull(correcto);
        assertEquals(("Rol con id"+4L+" editado correctamente"),correcto);
        assertEquals(("administrador2"),rol1.getName());
        verify(rolRep).save(rol1); //comprobar que en la base de datos se guarda y se actualiza
    }

    @Test
    void updateRolNull(){

        RolPutDTO rol2=new RolPutDTO();
        rol2.setName("administrador2");
        rol2.setActivo(true);

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
        assertEquals("Rol con id "+ 6L +" borrado con éxito", borrado);
        verify(rolRep).save(rol);
    }

    @Test
    void deleteRolNull() {
        // obligamos a Mockito a devolver vacío
        given(rolRep.findById(99L)).willReturn(Optional.empty());

        // WHEN
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> rolService.desactivarRol(99L)
        );

        assertEquals("Rol no encontrado con ID: "+99L, ex.getMessage());

        //asegurarme de que nunca se haya usado el metodo "save" para ninguna clase "Rol"
        verify(rolRep, never()).save(any(Rol.class));
    }

    @Test
    void userPorRol() {
        // GIVEN: Preparamos un usuario falso
        User usuario = new User();
        usuario.setIdUser(1L);
        usuario.setNombreUsuario("jorge");

        UserByRol userRol=new UserByRol();
        userRol.setNombreUsuario("jorge");

        // le enseñamos al mock que devuelva una lista con ese usuario cuando busque el rol 2
        given(rolRep.findUsuariosPorRol(2L)).willReturn(List.of(usuario));
        given(userMap.mappingRoles(usuario)).willReturn((userRol));

        // WHEN: Ejecutamos el servicio
        List<UserByRol> usuarios = rolService.userPorRol(2L);

        // THEN: Comprobamos resultados
        assertNotNull(usuarios);
        assertFalse(usuarios.isEmpty());
        assertEquals(1, usuarios.size());
        assertEquals("jorge", usuarios.getFirst().getNombreUsuario()); // Comprobamos que es nuestro usuario

        // Verificamos que llamó a la base de datos
        verify(rolRep).findUsuariosPorRol(2L);
        verifyNoMoreInteractions(rolRep);
    }

    @Test
    void userPorRolVacio() {
        // GIVEN: Simulamos que buscamos un rol que no lo tiene nadie (devuelve lista vacía)
        given(rolRep.findUsuariosPorRol(99L)).willReturn(List.of());

        // WHEN: Ejecutamos el servicio
        List<UserByRol> usuarios = rolService.userPorRol(99L);

        // THEN: Comprobamos que devuelve la lista vacía sin fallar
        assertNotNull(usuarios);
        assertTrue(usuarios.isEmpty());

        verify(rolRep).findUsuariosPorRol(99L);
        verifyNoMoreInteractions(rolRep);
    }
}
