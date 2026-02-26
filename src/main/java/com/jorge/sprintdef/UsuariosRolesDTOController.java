package com.jorge.sprintdef;

import com.jorge.sprintdef.dto.UsuarioRolesDTO;
import com.jorge.sprintdef.services.UsuariosRolesDTOService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/usuarios_roles")
public class UsuariosRolesDTOController {

    private final UsuariosRolesDTOService urService;

    public UsuariosRolesDTOController(UsuariosRolesDTOService urService) {
        this.urService = urService;
    }

    @GetMapping
    public List<UsuarioRolesDTO> getAllRoles() {
        return urService.listaUsuariosRoles();
    }
}
