package com.jorge.examenes.dto;

/**
 * Objeto de Transferencia de Datos (DTO) de salida que encapsula el resultado
 * inmediato de la corrección automática de un examen.
 * Devuelve al alumno su nota final calculada sobre 10, junto con el desglose
 * exacto de aciertos, fallos y respuestas dejadas en blanco.
 */
public class EvaluacionResultDTO {
    private int aciertos;
    private int fallos;
    private int enBlanco;
    private double notaFinal;

    public EvaluacionResultDTO(int aciertos, int fallos, int enBlanco, double notaFinal) {
        this.aciertos = aciertos;
        this.fallos = fallos;
        this.enBlanco = enBlanco;
        this.notaFinal = notaFinal;
    }

    public int getAciertos() { return aciertos; }
    public void setAciertos(int aciertos) { this.aciertos = aciertos; }
    public int getFallos() { return fallos; }
    public void setFallos(int fallos) { this.fallos = fallos; }
    public int getEnBlanco() { return enBlanco; }
    public void setEnBlanco(int enBlanco) { this.enBlanco = enBlanco; }
    public double getNotaFinal() { return notaFinal; }
    public void setNotaFinal(double notaFinal) { this.notaFinal = notaFinal; }
}