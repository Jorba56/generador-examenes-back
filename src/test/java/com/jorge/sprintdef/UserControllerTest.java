package com.jorge.sprintdef;
import com.jorge.sprintdef.dto.RolPostUser;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UsersAllDTO;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.mapping.UserMapper;
import com.jorge.sprintdef.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)

class UserControllerTest {

    @Mock
    private UserMapper userMap;

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
        UserAddDTO userdto = new UserAddDTO();
        userdto.setNombreUsuario("jorge"); // Damos un nombre de entrada

        UsersAllDTO userdto2 = new UsersAllDTO();
        userdto2.setNombreUsuario("jorge");

        given(userService.addUsuario(userdto)).willReturn(userdto2);
        userdto2 = userController.addUser(userdto);

        assertNotNull(userdto2);
        assertEquals(userdto2.getNombreUsuario(), userdto.getNombreUsuario());
        verify(userService).addUsuario(userdto);
        verifyNoMoreInteractions(userService);
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

    @Test
    void getUserIdNull() {
        // GIVEN: El servicio no encuentra nada y devuelve null
        given(userService.buscarPorId(99L)).willReturn(null);

        // WHEN
        UserIdDTo userFind = userController.getUserId(99L);

        // THEN
        assertNull(userFind);
        verify(userService).buscarPorId(99L);
    }

    @Test
    void rolesUser() {
        Rol rol = new Rol();
        rol.setIdRol(1L);
        rol.setName("admin");

        given(userService.rolesUser(1L)).willReturn(List.of(rol));

        List<Rol> roles = userController.rolesUser(1L);

        assertFalse(roles.isEmpty());
        assertEquals("admin", roles.getFirst().getName());
        verify(userService).rolesUser(1L);
    }

    @Test
    void userAddRol() {
        RolPostUser rolPost = new RolPostUser();
        rolPost.setIdRol(2L);

        given(userService.addRolUser(1L, rolPost)).willReturn("rol añdadido a usuario");

        String resultado = userController.userAddRol(1L, rolPost);

        assertEquals("rol añdadido a usuario", resultado);
        verify(userService).addRolUser(1L, rolPost);
    }

    @Test
    void deleteRolUser() {
        given(userService.deleteRolUser(1L, 2L)).willReturn("Rol eliminado correctamente");

        // Fíjate en el orden de las variables según tengas tu Controller
        String resultado = userController.deleteRolUser(2L, 1L);

        assertEquals("Rol eliminado correctamente", resultado);
    }
}