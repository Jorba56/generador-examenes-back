package com.jorge.sprintdef.services;


import com.jorge.sprintdef.*;
import com.jorge.sprintdef.dto.RolDTO;
import com.jorge.sprintdef.dto.RolPutDTO;
import com.jorge.sprintdef.dto.UserByRol;
import com.jorge.sprintdef.mapping.RolMapper;
import com.jorge.sprintdef.mapping.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final RolMapper rolMap;
    private final UserMapper userMap;

    public RolService(RolRepository rolRepository, RolMapper rolMap, UserMapper userMap) {
        this.rolMap = rolMap;
        this.userMap = userMap;
        this.rolRepository = rolRepository;
    }

    public List<Rol>listarRoles(){
        return rolRepository.findRolsByActivoIs(true);
    }

    public Optional<Rol> rolPorId (Long id){
        return rolRepository.findById(id);
    }

    public String newRol (RolDTO rol){
        Rol rol2=mappingARol(rol);
        rolRepository.save(rol2);
        return("rol añadido con exito");
    }

    public String actualizarRol(Long id, RolPutDTO rolNuevo) {
         String salida;
        // 1. Buscamos el usuario y abrimos el Optional de forma segura
        Rol rolUpdate = rolRepository.findById(id).orElse(null);

        // 2. Comprobamos que exista
        if (rolUpdate==null) {
             salida= "Error: Rol no encontrado";
        }else {
            Rol rolUpdate2= rolMap.mappingPutReverse(rolNuevo);
            rolUpdate.setName(rolUpdate2.getName());
            rolUpdate.setActivo(rolUpdate2.getActivo());
            // 5.   Guardamos en la base de datos
            rolRepository.save(rolUpdate);
            salida= "Rol editado correctamente";
        }
        return salida;

    }

    public String desactivarRol ( Long id){
        String borrado="";
        Rol rolSelect=rolRepository.findById(id).orElse(null);
        if (rolSelect!=null) {
            rolSelect.setActivo(false);
            rolRepository.save(rolSelect);
            borrado="rol borrado con éxito";
        }else{
            borrado="Error: Rol no encontrado";
        }
        return borrado;
    }

    public Rol mappingARol(RolDTO rol){
        Rol dto= new Rol();
        dto.setName(rol.getName());
        return dto;
    }

    public List<UserByRol> userPorRol (Long idRol){
        List<UserByRol> respuesta= new ArrayList<>();
        List<User> users = rolRepository.findUsuariosPorRol(idRol);
        for (int i=0; i<users.size();i++){
            userMap.mappingRoles(users.get(i));
            respuesta.add(userMap.mappingRoles(users.get(i)));
        }
        return respuesta;
    }

}
