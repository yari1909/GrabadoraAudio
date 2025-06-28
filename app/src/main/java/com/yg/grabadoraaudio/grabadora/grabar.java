package com.yg.grabadoraaudio.grabadora;

import java.io.File;

public class grabar {

    private String rutaArchivo;
    private String duracion;
    private String fechaCreacion;



    public grabar() {
    }

    public grabar(String rutaArchivo, String duracion, String fechaCreacion) {

        this.rutaArchivo = rutaArchivo;
        this.duracion = duracion;
        this.fechaCreacion = fechaCreacion;
    }



    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


    // Método para convertir a String para SharedPreferences
    public String toString() {

        return "Grabación: " + getDuracion() + " - " + getFechaCreacion();
    }

    // Método para crear objeto desde String de SharedPreferences
    public static grabar fromString(String grabacionString) {
        String[] partes = grabacionString.split("\\|");
        if (partes.length == 3) {
            return new grabar (partes[0], partes[1], partes[2]);
        }
        return null;
    }

    // Método para obtener el nombre del archivo (sin extensión)
    public String getNombreArchivo() {
        if (rutaArchivo != null) {
            File archivo = new File(rutaArchivo);
            String nombreCompleto = archivo.getName();
            return nombreCompleto.replace(".3gp", "");
        }
        return "Sin nombre";
    }
}