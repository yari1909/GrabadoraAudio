
package com.yg.grabadoraaudio.grabadora;

import java.io.File;

public class grabar {

    private String nombrePersonalizado;
    private String rutaArchivo;
    private String duracion;
    private String fechaCreacion;

    public grabar() {}

    public grabar(String nombrePersonalizado, String rutaArchivo, String duracion, String fechaCreacion) {
        this.nombrePersonalizado = nombrePersonalizado;
        this.rutaArchivo = rutaArchivo;
        this.duracion = duracion;
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombrePersonalizado() {
        return nombrePersonalizado;
    }

    public void setNombrePersonalizado(String nombrePersonalizado) {
        this.nombrePersonalizado = nombrePersonalizado;
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

    // Convierte objeto a String para SharedPreferences (guardamos sin nombre personalizado)
    @Override
    public String toString() {
        return rutaArchivo + "|" + duracion + "|" + fechaCreacion;
    }

    // Crear objeto desde String de SharedPreferences (nombre personalizado se crea al cargar)
    public static grabar fromString(String grabacionString) {
        String[] partes = grabacionString.split("\\|");
        if (partes.length == 3) {
            return new grabar("", partes[0], partes[1], partes[2]); // nombre personalizado vacío aquí
        }
        return null;
    }

    // Nombre del archivo sin extensión
    public String getNombreArchivo() {
        if (rutaArchivo != null) {
            File archivo = new File(rutaArchivo);
            String nombreCompleto = archivo.getName();
            return nombreCompleto.replace(".3gp", "");
        }
        return "Sin nombre";
    }
}