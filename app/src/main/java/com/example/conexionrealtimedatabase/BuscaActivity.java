package com.example.conexionrealtimedatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BuscaActivity extends AppCompatActivity {

    //Atributos de la clase
    String nuhsa;
    TextView nombre,apellidos,grupoSanguineo;
    FirebaseDatabase database;
    DatabaseReference puntoAcceso;

    //AMPLIACIÓN: nuevos campos de la ficha y estado de la pantalla
    TextView tvNuhsa, tvFechaNacimiento, tvEdad;
    Paciente pacienteActual;
    boolean borrado = false;
    ValueEventListener escuchador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca);
        setTitle(R.string.pac_titulo_ficha);

        //Recupero el String de nuhsa que me pasó la anterior activity
        nuhsa=getIntent().getStringExtra(Constantes.nuhsa);
        if (nuhsa == null) {
            nuhsa = "";
        }

        //Busco las vistas que tengo que modificar en mi interfaz
        nombre=findViewById(R.id.tvNombre);
        apellidos=findViewById(R.id.tvApellidos);
        grupoSanguineo=findViewById(R.id.tvGrupoSanguineo);
        tvNuhsa=findViewById(R.id.tvNuhsa);
        tvFechaNacimiento=findViewById(R.id.tvFechaNacimiento);
        tvEdad=findViewById(R.id.tvEdad);

        //Instancia de mi base de datos
        database = FirebaseDatabase.getInstance();

        //Punto de acceso al nuhsa que se nos ha pasado
        puntoAcceso = database.getReference(Constantes.pacientes+"/"+nuhsa);

        //Listener que escucha si hay algún elemento colgando de ese punto de acceso.
        //Al ser un listener en tiempo real, la ficha se actualiza sola cuando el
        //paciente se edita y se cierra sola cuando se borra.
        escuchador = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!nuhsa.equals("") && dataSnapshot.getValue()!=null) {//Nuhsa encontrado
                    Paciente paciente = dataSnapshot.getValue(Paciente.class);
                    if (paciente != null) {
                        pacienteActual = paciente;
                        mostrarPaciente(paciente);
                    }
                }
                else{
                    //Si acabamos de borrarlo no mostramos el aviso de "no encontrado"
                    if (!borrado) {
                        Toast.makeText(BuscaActivity.this,"Paciente no encontrado",Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        };
        puntoAcceso.addValueEventListener(escuchador);
    }

    //AMPLIACIÓN: muestra todos los datos del paciente, incluida la edad calculada
    private void mostrarPaciente(Paciente paciente) {
        tvNuhsa.setText(paciente.getNuhsa());
        nombre.setText(paciente.getNombre());
        apellidos.setText(paciente.getApellidos());
        grupoSanguineo.setText(paciente.getGrupoSanguineo());
        if (paciente.getFechaNacimiento() != null) {
            tvFechaNacimiento.setText(paciente.getFechaNacimiento());
            tvEdad.setText(getString(R.string.pac_formato_edad, paciente.getEdad()));
        } else {
            tvFechaNacimiento.setText(R.string.pac_sin_registrar);
            tvEdad.setText(R.string.pac_sin_registrar);
        }
    }

    //AMPLIACIÓN (Update): abre el formulario en modo edición. OnClick asignado desde el Layout
    public void editarPaciente(View view) {
        if (pacienteActual == null) {
            return;
        }
        Intent intent = new Intent(BuscaActivity.this, FormularioActivity.class);
        intent.putExtra(Constantes.nuhsa, pacienteActual.getNuhsa());
        startActivity(intent);
    }

    //AMPLIACIÓN (Delete): borra el paciente tras confirmación. OnClick asignado desde el Layout
    public void borrarPaciente(View view) {
        if (pacienteActual == null) {
            return;
        }
        RepositorioPacientes.confirmarYBorrar(this, pacienteActual, () -> borrado = true);
    }

    //AMPLIACIÓN: dejamos de escuchar al cerrar la pantalla para no consumir recursos
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (escuchador != null) {
            puntoAcceso.removeEventListener(escuchador);
        }
    }
}
