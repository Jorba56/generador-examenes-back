package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.RolDTO;
import com.jorge.sprintdef.dto.RolPutDTO;
import com.jorge.sprintdef.dto.UserByRol;
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
class RolControllerTest {

    @Mock
    private RolService rolService;

    @InjectMocks
    private RolController rolController;

    @Test
    void getAllRoles() {
        Rol rol1 = new Rol();
        rol1.setName("administrador");
        rol1.setActivo(true);
        rol1.setIdRol(4L);

        given(rolService.listarRoles()).willReturn(List.of(rol1));

        List<Rol> rolList = rolController.getAllRoles();

        assertFalse(rolList.isEmpty());
        assertEquals(1, rolList.size());
        verify(rolService).listarRoles();
        verifyNoMoreInteractions(rolService);
    }

    @Test
    void getRolId() {
        Rol rol1 = new Rol();
        rol1.setName("administrador");
        rol1.setActivo(true);
        rol1.setIdRol(4L);

        given(rolService.rolPorId(4L)).willReturn(Optional.of(rol1));

        //when
        Optional<Rol> rolSearch = rolController.getRolId(4L);

        assertTrue(rolSearch.isPresent());
        assertEquals(rol1.getIdRol(), rolSearch.get().getIdRol());
        verify(rolService).rolPorId(4L);
        verifyNoMoreInteractions(rolService);
    }

    @Test
    void addRol() {
        RolDTO rolDTO = new RolDTO();
        rolDTO.setName("nuevo_rol");

        given(rolService.newRol(rolDTO)).willReturn("rol añadido con exito");

        String correcto = rolController.addRol(rolDTO);

        assertNotNull(correcto);
        assertEquals("rol añadido con exito", correcto);
        verify(rolService).newRol(rolDTO);
        verifyNoMoreInteractions(rolService);
    }

    @Test
    void updateRol() {
        RolPutDTO rolNuevo = new RolPutDTO();
        rolNuevo.setName("administrador2");
        rolNuevo.setActivo(true);

        given(rolService.actualizarRol(4L, rolNuevo)).willReturn("Rol editado correctamente");

        String correcto = rolController.updateRol(4L, rolNuevo);

        assertNotNull(correcto);
        assertEquals("Rol editado correctamente", correcto);
        verify(rolService).actualizarRol(4L, rolNuevo);
        verifyNoMoreInteractions(rolService);
    }

    @Test
    void deleteRol() {
        given(rolService.desactivarRol(6L)).willReturn("rol borrado con éxito");

        //when
        String borrado = rolController.deleteRol(6L);

        //asserts
        assertNotNull(borrado);
        assertEquals("rol borrado con éxito", borrado);
        verify(rolService).desactivarRol(6L);
        verifyNoMoreInteractions(rolService);
    }

    @Test
    void getRolIdNull() {
        given(rolService.rolPorId(99L)).willReturn(Optional.empty());

        Optional<Rol> rolSearch = rolController.getRolId(99L);

        assertTrue(rolSearch.isEmpty());
        verify(rolService).rolPorId(99L);
    }

    @Test
    void userRol() {
        User usuario = new User();
        usuario.setIdUser(1L);
        usuario.setNombreUsuario("jorge");

        UserByRol usuario2 = new UserByRol();
        usuario2.setNombreUsuario("jorge");

        given(rolService.userPorRol(2L)).willReturn(List.of(usuario2));

        List<UserByRol> usuarios = rolController.userRol(2L);
        UserByRol user1= usuarios.getFirst();

        assertFalse(usuarios.isEmpty());
        assertEquals("jorge", user1.getNombreUsuario());
        verify(rolService).userPorRol(2L);
    }
}