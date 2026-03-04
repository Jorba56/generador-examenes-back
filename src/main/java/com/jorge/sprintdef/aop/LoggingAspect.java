package com.jorge.sprintdef.aop;

import com.jorge.sprintdef.exceptions.BadRequestException;
import com.jorge.sprintdef.exceptions.DuplicateException;
import com.jorge.sprintdef.exceptions.NotFoundException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class LoggingAspect {

    String tipoError="ERROR_SISTEMA";

    // Le decimos que apunte la carpeta de servicios
    @Pointcut("within(com.jorge.sprintdef.services..*)")
    public void vigilarServicios() {}

    @Before("vigilarServicios()")
    public void logAntesDeEntrar(JoinPoint joinPoint) {
        String mensaje = "Ejecutando: " + joinPoint.getSignature().getName();
        imprimirLogCentralizado("INFO_SISTEMA", mensaje);
    }

    @AfterReturning(pointcut = "vigilarServicios()", returning = "resultado")
    public void logDespuesDeSalir(JoinPoint joinPoint, Object resultado) {
        String nombreMetodo = joinPoint.getSignature().getName();

        // Si el metodo devuelve un String vemos si es un mensaje de fallo.
        if (resultado instanceof String) {
            String respuesta = ((String) resultado).toLowerCase();
            // Buscamos palabras clave que se usan en los servicios para los fallos
            if (respuesta.contains("error") || respuesta.contains("no encontrado") || respuesta.contains("no válido")) {
                imprimirLogCentralizado("AVISO_NEGOCIO", "Rechazado en " + nombreMetodo + " | Motivo: " + resultado);
                return; // Cortamos aquí para que NO imprima el éxito
            }
        }

        //Si no es un error (o devuelve otra cosa como un DTO), es un éxito real
        String mensaje = "Finalizado con éxito: " + nombreMetodo;
        imprimirLogCentralizado("EXITO_SISTEMA", mensaje);
    }

    @AfterThrowing(pointcut = "vigilarServicios()", throwing = "error")
    public void logEnCasoDeError(JoinPoint joinPoint, Throwable error) {
        String mensaje = "Fallo en: " + joinPoint.getSignature().getName() + " | Causa: " + error.getMessage();
        if (error instanceof NotFoundException){ tipoError="ERROR_NO_ENCONTRADO";}
        if (error instanceof DuplicateException){ tipoError="ERROR_DUPLICADO";}
        if (error instanceof BadRequestException){ tipoError="ERROR_PETICIÓN_INVÁLIDA";}

        imprimirLogCentralizado(tipoError, mensaje);
    }

    /**
     * MOTOR PROPIO DE LOGS CENTRALIZADO
     * Formatea todos los mensajes de la aplicación para que tengan estructura idéntica.
     * Sin dependencias de SLF4J o Log4j.
     */
    @SuppressWarnings("java:S106")
    private void imprimirLogCentralizado(String tipo, String descripcion) {
        // Obtenemos la fecha exacta
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Lo imprimimos por consola con una estructura clara y limpia
        System.out.println("=========================================");
        System.out.println("[LOG CENTRALIZADO]");
        System.out.println("FECHA       : " + fecha);
        System.out.println("TIPO        : " + tipo);
        System.out.println("DESCRIPCIÓN : " + descripcion);
        System.out.println("=========================================\n");
    }
}