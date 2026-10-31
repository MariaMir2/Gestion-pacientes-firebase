package com.example.conexionrealtimedatabase;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Locale;

//AMPLIACIÓN: formulario de alta manual (Create) y de edición (Update) de pacientes.
//Si recibe un NUHSA en el Intent funciona en modo edición; si no, en modo alta.
public class FormularioActivity extends AppCompatActivity {

    EditText etNuhsa, etNombre, etApellidos;
    Spinner spGrupo;
    TextView tvFecha;
    String fechaSeleccionada;   //dd/MM/yyyy
    String grupoAnterior;       //solo en modo edición
    boolean modoEdicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        etNuhsa = findViewById(R.id.etNuhsa);
        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        spGrupo = findViewById(R.id.spGrupo);
        tvFecha = findViewById(R.id.tvFecha);
        TextView tvTitulo = findViewById(R.id.tvTituloFormulario);

        //Spinner con los grupos sanguíneos definidos en Constantes
        ArrayAdapter<String> adaptadorGrupo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Constantes.grupo);
        adaptadorGrupo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGrupo.setAdapter(adaptadorGrupo);

        String nuhsa = getIntent().getStringExtra(Constantes.nuhsa);
        modoEdicion = (nuhsa != null);

        if (modoEdicion) {
            setTitle(R.string.pac_titulo_editar);
            tvTitulo.setText(R.string.pac_titulo_editar);
            //El NUHSA es la clave del paciente en Firebase, por eso no se puede modificar
            etNuhsa.setText(nuhsa);
            etNuhsa.setEnabled(false);
            cargarPaciente(nuhsa);
        } else {
            setTitle(R.string.pac_titulo_nuevo);
            tvTitulo.setText(R.string.pac_titulo_nuevo);
        }
        mostrarFecha();
    }

    //Lectura única (no en tiempo real) para rellenar el formulario con los datos actuales
    private void cargarPaciente(String nuhsa) {
        FirebaseDatabase.getInstance().getReference(Constantes.pacientes).child(nuhsa)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Paciente paciente = snapshot.getValue(Paciente.class);
                        if (paciente == null) {
                            Toast.makeText(FormularioActivity.this, "Paciente no encontrado", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        etNombre.setText(paciente.getNombre());
                        etApellidos.setText(paciente.getApellidos());
                        grupoAnterior = paciente.getGrupoSanguineo();
                        int posicion = Arrays.asList(Constantes.grupo).indexOf(grupoAnterior);
                        if (posicion >= 0) {
                            spGrupo.setSelection(posicion);
                        }
                        fechaSeleccionada = paciente.getFechaNacimiento();
                        mostrarFecha();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FormularioActivity.this, "Error de lectura: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Selector de fecha (DatePickerDialog). OnClick asignado desde el Layout
    public void seleccionarFecha(View view) {
        int dia = 1, mes = 0, anio = 1980;
        if (fechaSeleccionada != null) {
            try {
                String[] partes = fechaSeleccionada.split("/");
                dia = Integer.parseInt(partes[0]);
                mes = Integer.parseInt(partes[1]) - 1;
                anio = Integer.parseInt(partes[2]);
            } catch (RuntimeException e) {
                //Si la fecha guardada no tiene el formato esperado se usa la fecha por defecto
            }
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {
            fechaSeleccionada = String.format(Locale.ROOT, "%02d/%02d/%04d", day, month + 1, year);
            mostrarFecha();
        }, anio, mes, dia);
        //No se permiten fechas de nacimiento futuras
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void mostrarFecha() {
        if (fechaSeleccionada == null) {
            tvFecha.setText(R.string.pac_sin_fecha);
        } else {
            tvFecha.setText(fechaSeleccionada);
        }
    }

    //Validación y guardado. OnClick asignado desde el Layout
    public void guardarPaciente(View view) {
        String nuhsa = etNuhsa.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String grupo = (String) spGrupo.getSelectedItem();

        boolean correcto = true;
        if (!RepositorioPacientes.esNuhsaValido(nuhsa)) {
            etNuhsa.setError("Solo letras y números");
            correcto = false;
        }
        if (nombre.isEmpty()) {
            etNombre.setError("Campo obligatorio");
            correcto = false;
        }
        if (apellidos.isEmpty()) {
            etApellidos.setError("Campo obligatorio");
            correcto = false;
        }
        if (fechaSeleccionada == null) {
            Toast.makeText(this, "Seleccione la fecha de nacimiento", Toast.LENGTH_SHORT).show();
            correcto = false;
        }
        if (!correcto) {
            return;
        }

        final Paciente paciente = new Paciente(nombre, apellidos, grupo, nuhsa, fechaSeleccionada);

        if (modoEdicion) {
            escribir(paciente);
        } else {
            //En el alta se comprueba antes que el NUHSA no exista, para no sobrescribir a otro paciente
            FirebaseDatabase.getInstance().getReference(Constantes.pacientes).child(nuhsa)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                etNuhsa.setError("Ya existe un paciente con este NUHSA");
                            } else {
                                escribir(paciente);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(FormularioActivity.this, "Error de lectura: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void escribir(Paciente paciente) {
        RepositorioPacientes.guardar(paciente, grupoAnterior, (error, referencia) -> {
            if (error == null) {
                Toast.makeText(getApplicationContext(),
                        modoEdicion ? "Paciente actualizado" : "Paciente creado",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Error al guardar: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
