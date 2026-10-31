package com.example.conexionrealtimedatabase;

import com.google.firebase.database.Exclude;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Paciente {
    private String nombre;
    private String apellidos;
    private String grupoSanguineo;
    private String nuhsa;
    //AMPLIACIÓN: fecha de nacimiento en formato dd/MM/yyyy
    private String fechaNacimiento;

    //Creamos constructor vacío (importante), constructor, getters y toString
    public Paciente() {
    }

    public Paciente(String nombre, String apellidos, String grupoSanguineo, String nuhsa) {
        this(nombre, apellidos, grupoSanguineo, nuhsa, null);
    }

    //AMPLIACIÓN: constructor que incluye la fecha de nacimiento
    public Paciente(String nombre, String apellidos, String grupoSanguineo, String nuhsa, String fechaNacimiento) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.grupoSanguineo = grupoSanguineo;
        this.nuhsa = nuhsa;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getGrupoSanguineo() {
        return grupoSanguineo;
    }

    public String getNuhsa() {
        return nuhsa;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    //AMPLIACIÓN: setters, necesarios para poder modificar un paciente existente
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public void setGrupoSanguineo(String grupoSanguineo) {
        this.grupoSanguineo = grupoSanguineo;
    }

    public void setNuhsa(String nuhsa) {
        this.nuhsa = nuhsa;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    //AMPLIACIÓN: edad calculada a partir de la fecha de nacimiento.
    //@Exclude evita que Firebase la almacene como un campo más del paciente.
    //Devuelve -1 si el paciente no tiene fecha registrada.
    @Exclude
    public int getEdad() {
        if (fechaNacimiento == null) {
            return -1;
        }
        try {
            String[] partes = fechaNacimiento.split("/");
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int anio = Integer.parseInt(partes[2]);
            Calendar hoy = Calendar.getInstance();
            int edad = hoy.get(Calendar.YEAR) - anio;
            int mesHoy = hoy.get(Calendar.MONTH) + 1;
            int diaHoy = hoy.get(Calendar.DAY_OF_MONTH);
            //Si este año todavía no ha cumplido, restamos uno
            if (mesHoy < mes || (mesHoy == mes && diaHoy < dia)) {
                edad--;
            }
            return edad;
        } catch (RuntimeException e) {
            return -1;
        }
    }

    //AMPLIACIÓN: representación como mapa clave-valor para escrituras múltiples con updateChildren
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        datos.put("apellidos", apellidos);
        datos.put("grupoSanguineo", grupoSanguineo);
        datos.put("nuhsa", nuhsa);
        datos.put("fechaNacimiento", fechaNacimiento);
        return datos;
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", grupoSanguineo='" + grupoSanguineo + '\'' +
                ", nuhsa='" + nuhsa + '\'' +
                ", fechaNacimiento='" + fechaNacimiento + '\'' +
                '}';
    }
}
