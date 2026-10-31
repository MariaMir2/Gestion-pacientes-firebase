package com.example.conexionrealtimedatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //Atributos de la clase:
    //EditText para el texto de búsqueda de paciente por Nuhsa
    EditText ETnuhsa;
    //Atributos para la base de datos de Firebase
    FirebaseDatabase database;
    //y el punto de acceso del fichero JSON
    DatabaseReference puntoAcceso;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Lo único que voy a hacer es recuperar de la interfaz el EditText
        ETnuhsa=findViewById(R.id.editText);
    }

    //OnClick asignado desde el Layout
    //En vez de butonBusca.setOnClickListener...
    public void buscaPaciente(View view) {
        //Recupero la cadena de texto del EditText
        String nuhsa= ETnuhsa.getText().toString().trim();
        //AMPLIACIÓN: validación previa. Un NUHSA vacío o con caracteres no permitidos
        //en una clave de Firebase (. $ # [ ] /) hacía fallar la búsqueda
        if (!RepositorioPacientes.esNuhsaValido(nuhsa)) {
            ETnuhsa.setError("Introduzca un NUHSA válido (solo letras y números)");
            return;
        }
        //Voy a lanzar la activity BuscaActivity con  el parámetro del nuhsa recuperado mediante putExtra
        Intent intent=new Intent(MainActivity.this,BuscaActivity.class);
        intent.putExtra(Constantes.nuhsa,nuhsa);
        startActivity(intent);
    }
    //OnClick asignado desde el Layout
    public void insertaPaciente(View view) {
        //Genero un paciente aleatorio
        Paciente paciente=generaPacienteAleatorio();

        //Recupero una instancia de mi base de datos
        database=FirebaseDatabase.getInstance();
        //Referencio a la raíz
        puntoAcceso =database.getReference();
        //Navego con child y establezco como valor el objeto paciente ¡directamente!
        puntoAcceso.child(Constantes.pacientes).child(paciente.getNuhsa()).setValue(paciente);
        //Ahora navego para incluir ese mismo paciente colgando de su grupo sanguíneo
        puntoAcceso.child(paciente.getGrupoSanguineo()).child(paciente.getNuhsa()).setValue(paciente);
        //Muestro un mensaje para indicar que ha ido bien
        Toast.makeText(this,paciente.toString(),Toast.LENGTH_LONG).show();
    }

    //AMPLIACIÓN: abre el listado de pacientes
    public void abrirListado(View view) {
        startActivity(new Intent(MainActivity.this, ListaActivity.class));
    }

    //AMPLIACIÓN: abre el formulario de alta manual
    public void nuevoPaciente(View view) {
        startActivity(new Intent(MainActivity.this, FormularioActivity.class));
    }

    private Paciente generaPacienteAleatorio(){
        final Random random = new Random();
        String nuhsa=Constantes.nuhsa+random.nextInt(9999);
        String grupo=Constantes.grupo[random.nextInt(8)];
        //AMPLIACIÓN: fecha de nacimiento aleatoria entre 1940 y 2020
        String fecha=String.format(Locale.ROOT,"%02d/%02d/%04d",
                1+random.nextInt(28), 1+random.nextInt(12), 1940+random.nextInt(81));
        return(new Paciente(Constantes.nombre[random.nextInt(8)],
                Constantes.apellido[random.nextInt(8)]+" "+Constantes.apellido[random.nextInt(8)],
                grupo,
                nuhsa,
                fecha));
    }

}
