package com.example.imnotok;

import java.util.ArrayList;
//Clase para el control de usuarios. Cuenta, Ubicación.
public class UsuarioOK {

    private String numeroDeUsuario;
    private String nombreDeUsuario;
    private String contraseña;
    private Double latitude;
    private Double longitude;

    public UsuarioOK() {
        this.numeroDeUsuario = numeroDeUsuario;
        this.nombreDeUsuario = nombreDeUsuario;
        this.contraseña = contraseña;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNumeroDeUsuario() {
        return numeroDeUsuario;
    }

    public void setNumeroDeUsuario(String numeroDeUsuario) {
        this.numeroDeUsuario = numeroDeUsuario;
    }

    public String getNombreDeUsuario() {
        return nombreDeUsuario;
    }

    public void setNombreDeUsuario(String nombreDeUsuario) {
        this.nombreDeUsuario = nombreDeUsuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
