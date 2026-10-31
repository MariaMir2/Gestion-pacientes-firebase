package com.example.conexionrealtimedatabase;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

//AMPLIACIÓN: clase que agrupa las operaciones de escritura sobre Firebase (Update y Delete).
//Cada paciente se guarda en dos rutas: Pacientes/<nuhsa> y <grupoSanguineo>/<nuhsa>.
//Con updateChildren ambas rutas se modifican en una única operación atómica,
//de modo que nunca queda una copia desactualizada en una de ellas.
public final class RepositorioPacientes {

    private RepositorioPacientes() {
    }

    private static DatabaseReference raiz() {
        return FirebaseDatabase.getInstance().getReference();
    }

    //Un NUHSA se usa como clave en Firebase, por lo que no puede contener . $ # [ ] /
    //Se restringe a letras y números
    public static boolean esNuhsaValido(String nuhsa) {
        return nuhsa != null && nuhsa.matches("[A-Za-z0-9]+");
    }

    //Crea o actualiza un paciente. Si se ha cambiado su grupo sanguíneo,
    //se elimina la copia que colgaba del grupo anterior.
    public static void guardar(Paciente paciente, String grupoAnterior,
                               DatabaseReference.CompletionListener listener) {
        Map<String, Object> datos = paciente.toMap();
        Map<String, Object> cambios = new HashMap<>();
        cambios.put(Constantes.pacientes + "/" + paciente.getNuhsa(), datos);
        cambios.put(paciente.getGrupoSanguineo() + "/" + paciente.getNuhsa(), datos);
        if (grupoAnterior != null && !grupoAnterior.equals(paciente.getGrupoSanguineo())) {
            cambios.put(grupoAnterior + "/" + paciente.getNuhsa(), null);
        }
        raiz().updateChildren(cambios, listener);
    }

    //Elimina el paciente de las dos rutas (asignar null a una ruta la borra)
    public static void borrar(Paciente paciente, DatabaseReference.CompletionListener listener) {
        Map<String, Object> cambios = new HashMap<>();
        cambios.put(Constantes.pacientes + "/" + paciente.getNuhsa(), null);
        cambios.put(paciente.getGrupoSanguineo() + "/" + paciente.getNuhsa(), null);
        raiz().updateChildren(cambios, listener);
    }

    //Muestra un diálogo de confirmación antes de borrar.
    //alConfirmar se ejecuta justo antes del borrado (puede ser null).
    public static void confirmarYBorrar(Context contexto, Paciente paciente, Runnable alConfirmar) {
        final Context app = contexto.getApplicationContext();
        new AlertDialog.Builder(contexto)
                .setTitle("Eliminar paciente")
                .setMessage("Se eliminará a " + paciente.getNombre() + " " + paciente.getApellidos()
                        + " (" + paciente.getNuhsa() + "). Esta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialogo, boton) -> {
                    if (alConfirmar != null) {
                        alConfirmar.run();
                    }
                    borrar(paciente, (error, referencia) -> {
                        String mensaje = (error == null)
                                ? "Paciente eliminado"
                                : "Error al eliminar: " + error.getMessage();
                        Toast.makeText(app, mensaje, Toast.LENGTH_SHORT).show();
                    });
                })
                .show();
    }
}
