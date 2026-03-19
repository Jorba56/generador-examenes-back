package com.jorge.examenes.dto;

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