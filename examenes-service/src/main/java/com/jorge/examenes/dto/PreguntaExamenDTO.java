package com.jorge.examenes.dto;

/**
 * Objeto de Transferencia de Datos (DTO) que representa una pregunta de un examen
 * enviada al cliente.
 * Se utiliza para mostrar el enunciado y las opciones al alumno durante la
 * realización de la prueba, excluyendo deliberadamente la respuesta correcta
 * por motivos de seguridad.
 */
public class PreguntaExamenDTO {
    private int numero;
    private Long id;
    private String enunciado;
    private String opcionA;
    private String opcionB;
    private String opcionC;
    private String opcionD;

    public PreguntaExamenDTO() {
    }

    public PreguntaExamenDTO(int numero, Long id, String enunciado, String opcionA, String opcionB, String opcionC, String opcionD) {
        this.numero = numero;
        this.id = id;
        this.enunciado = enunciado;
        this.opcionA = opcionA;
        this.opcionB = opcionB;
        this.opcionC = opcionC;
        this.opcionD = opcionD;
    }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getOpcionA() { return opcionA; }
    public void setOpcionA(String opcionA) { this.opcionA = opcionA; }

    public String getOpcionB() { return opcionB; }
    public void setOpcionB(String opcionB) { this.opcionB = opcionB; }

    public String getOpcionC() { return opcionC; }
    public void setOpcionC(String opcionC) { this.opcionC = opcionC; }

    public String getOpcionD() { return opcionD; }
    public void setOpcionD(String opcionD) { this.opcionD = opcionD; }
}