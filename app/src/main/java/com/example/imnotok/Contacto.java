package com.example.imnotok;
// Clase para manejar los contactos del usuario de la aplicación (agregar, eliminar)
public class Contacto {

    private String nombre;
    private String numero;
    private int intento;

    public Contacto() {
        this.nombre = nombre;
        this.numero = numero;
        this.intento = intento;
    }

    public Contacto(Contacto value) {
        this.nombre=value.getNombre();
        this.numero=value.getNumero();
        this.intento=value.getIntento();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getIntento() {
        return intento;
    }

    public void setIntento(int intento) { this.intento = intento;
    }
}
