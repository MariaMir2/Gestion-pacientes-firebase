package com.example.conexionrealtimedatabase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

//AMPLIACIÓN: listado de pacientes con RecyclerView actualizado en tiempo real mediante ChildEventListener.
//El filtro por grupo sanguíneo aprovecha los nodos por grupo que ya crea la aplicación base.
public class ListaActivity extends AppCompatActivity {

    private final List<Paciente> listaPacientes = new ArrayList<>();
    private PacienteAdapter adaptador;
    private TextView tvContador;
    private DatabaseReference referenciaActual;
    private ChildEventListener escuchador;
    private final Collator collator = Collator.getInstance(new Locale("es", "ES"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        setTitle(R.string.pac_titulo_listado);

        tvContador = findViewById(R.id.tvContador);

        //RecyclerView: lista vertical con separadores
        RecyclerView rvPacientes = findViewById(R.id.rvPacientes);
        rvPacientes.setLayoutManager(new LinearLayoutManager(this));
        rvPacientes.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adaptador = new PacienteAdapter(listaPacientes, new PacienteAdapter.OnPacienteClickListener() {
            @Override
            public void onPacienteClick(Paciente paciente) {
                //Pulsación corta: abre la ficha del paciente (BuscaActivity de la app base)
                Intent intent = new Intent(ListaActivity.this, BuscaActivity.class);
                intent.putExtra(Constantes.nuhsa, paciente.getNuhsa());
                startActivity(intent);
            }

            @Override
            public void onPacienteLongClick(Paciente paciente) {
                //Pulsación larga: borrado con confirmación
                RepositorioPacientes.confirmarYBorrar(ListaActivity.this, paciente, null);
            }
        });
        rvPacientes.setAdapter(adaptador);

        //Spinner de filtro: "Todos" + los grupos sanguíneos
        final String[] opciones = new String[Constantes.grupo.length + 1];
        opciones[0] = Constantes.todos;
        System.arraycopy(Constantes.grupo, 0, opciones, 1, Constantes.grupo.length);
        ArrayAdapter<String> adaptadorFiltro = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opciones);
        adaptadorFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spFiltro = findViewById(R.id.spFiltro);
        spFiltro.setAdapter(adaptadorFiltro);
        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //"Todos" lee del nodo Pacientes; un grupo lee de su propio nodo (A+, B-, ...)
                escuchar(position == 0 ? Constantes.pacientes : opciones[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //Cambia el nodo escuchado: quita el listener anterior, vacía la lista y añade uno nuevo
    private void escuchar(String nodo) {
        dejarDeEscuchar();
        listaPacientes.clear();
        adaptador.notifyDataSetChanged();
        actualizarContador();

        referenciaActual = FirebaseDatabase.getInstance().getReference(nodo);
        escuchador = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Paciente paciente = snapshot.getValue(Paciente.class);
                if (paciente == null) {
                    return;
                }
                listaPacientes.add(paciente);
                ordenar();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Paciente paciente = snapshot.getValue(Paciente.class);
                int indice = buscarIndice(snapshot.getKey());
                if (paciente == null || indice < 0) {
                    return;
                }
                listaPacientes.set(indice, paciente);
                ordenar();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int indice = buscarIndice(snapshot.getKey());
                if (indice >= 0) {
                    listaPacientes.remove(indice);
                    adaptador.notifyItemRemoved(indice);
                    actualizarContador();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //No se usa: el orden lo establece la propia lista
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaActivity.this, "Error de lectura: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        referenciaActual.addChildEventListener(escuchador);
    }

    private void dejarDeEscuchar() {
        if (referenciaActual != null && escuchador != null) {
            referenciaActual.removeEventListener(escuchador);
        }
    }

    //La clave de cada hijo es el NUHSA del paciente
    private int buscarIndice(String nuhsa) {
        for (int i = 0; i < listaPacientes.size(); i++) {
            if (nuhsa != null && nuhsa.equals(listaPacientes.get(i).getNuhsa())) {
                return i;
            }
        }
        return -1;
    }

    //Orden alfabético por apellidos y nombre, respetando tildes
    private void ordenar() {
        Collections.sort(listaPacientes, (a, b) -> collator.compare(claveOrden(a), claveOrden(b)));
        adaptador.notifyDataSetChanged();
        actualizarContador();
    }

    private String claveOrden(Paciente p) {
        String apellidos = p.getApellidos() == null ? "" : p.getApellidos();
        String nombre = p.getNombre() == null ? "" : p.getNombre();
        return apellidos + " " + nombre;
    }

    private void actualizarContador() {
        tvContador.setText(getString(R.string.pac_contador, listaPacientes.size()));
    }

    //OnClick asignado desde el Layout
    public void nuevoPaciente(View view) {
        startActivity(new Intent(ListaActivity.this, FormularioActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dejarDeEscuchar();
    }
}
