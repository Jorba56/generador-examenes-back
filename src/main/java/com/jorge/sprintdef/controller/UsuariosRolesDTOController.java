package com.jorge.sprintdef.controller;

import com.jorge.sprintdef.dto.UsuarioRolesDTO;
import com.jorge.sprintdef.services.UsuariosRolesDTOService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/usuarios_roles")
@Tag(name = "Relación Usuarios-Roles", description = "Endpoints para consultar la información combinada de los usuarios y sus roles de forma plana.")
public class UsuariosRolesDTOController {

    private final UsuariosRolesDTOService urService;

    public UsuariosRolesDTOController(UsuariosRolesDTOService urService) {
        this.urService = urService;
    }

    @Operation(summary = "Listar usuarios con sus roles", description = "Devuelve una lista que combina los datos básicos de los usuarios con los detalles de los roles que tienen asignados en el sistema.")
    @GetMapping
    public List<UsuarioRolesDTO> getAllRoles() {
        return urService.listaUsuariosRoles();
    }
}
