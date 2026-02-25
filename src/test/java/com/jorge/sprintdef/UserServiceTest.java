package com.jorge.sprintdef;


import com.jorge.sprintdef.dto.UsersAllDTO;
import com.jorge.sprintdef.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import com.jorge.sprintdef.mapping.UserMapper;


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
}
/*
    @Test
    void getAllRolesVacio(){
        given(userRepository.findRolsByActivoIs(true)).willReturn(List.of());

        List <Rol> rolList= userService.listarRoles();

        assertTrue(rolList.isEmpty());
        verify(userRepository).findRolsByActivoIs(true);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getRolId(){
        Rol rol1=new Rol();
        rol1.setName("administrador");
        rol1.setActivo(false);
        rol1.setIdRol(4L);

        given(userRepository.findById(rol1.getIdRol())).willReturn(Optional.of(rol1));

        Rol rolSearch= userService.rolPorId(rol1.getIdRol()).orElse(null);

        assertNotNull(rolSearch);
        assertEquals(rol1.getIdRol(),rolSearch.getIdRol());
        verify(userRepository).findById(rol1.getIdRol());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getRolIdNull(){
        given(userRepository.findById(5L)).willReturn(Optional.empty());

        Optional<Rol> rolSearch= userService.rolPorId(5L);

        assertEquals(Optional.empty(), rolSearch);
        verify(userRepository).findById(5L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void addRol(){
        RolDTO rol=new RolDTO();
        Rol rol1= userService.mappingARol(rol);
        given(userRepository.save(any(Rol.class))).willReturn(rol1);

        String correcto= userService.newRol(rol);

        assertNotNull(correcto);
        assertEquals(("rol añadido con exito"),correcto);
        verify(userRepository).save(any(Rol.class));
        verifyNoMoreInteractions(userRepository);
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

        given(userRepository.findById(rol1.getIdRol())).willReturn(Optional.of(rol1));

        String correcto= userService.actualizarRol(4L,rol2);

        assertNotNull(correcto);
        assertEquals(("Rol editado correctamente"),correcto);
        assertEquals(("administrador2"),rol1.getName());
        verify(userRepository).save(rol1); //comprobar que en la base de datos se guarda y se actualiza
    }

    @Test
    void updateRolNull(){

        Rol rol2=new Rol();
        rol2.setName("administrador2");
        rol2.setActivo(true);
        rol2.setIdRol(5L);

        given(userRepository.findById(99L)).willReturn(Optional.empty());

        String fallo= userService.actualizarRol(99L,rol2);

        assertNotNull(fallo);
        assertEquals(("Error: Rol no encontrado"),fallo);
    }

    @Test
    void deleteRol(){
        Rol rol=new Rol();
        rol.setIdRol(6L);
        rol.setActivo(true);// no necesito más

        given(userRepository.findById(6L)).willReturn(Optional.of(rol));
        //when
        String borrado = userService.desactivarRol(6L);

        //asserts
        assertFalse(rol.getActivo());
        assertNotNull(borrado);
        assertEquals("rol borrado con exito", borrado);
        verify(userRepository).save(rol);
    }

    @Test
    void deleteRolNull() {
        // Obligamos a Mockito a devolver vacío
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // WHEN
        String borrado = userService.desactivarRol(99L);

        //devuelve el mensaje, pero la base de datos nunca guardó nada
        assertNotNull(borrado);
        assertEquals("rol borrado con exito", borrado);

        //asegurarme de que nunca se haya usado el metodo "save" para ninguna clase "Rol"
        verify(userRepository, never()).save(any(Rol.class));

    }
} */
