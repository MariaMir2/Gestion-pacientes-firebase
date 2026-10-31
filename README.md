# Gestión de pacientes con Firebase Realtime Database (ampliación)

Aplicación Android en Java desarrollada para la asignatura Aplicaciones para Sistemas de Información Sanitaria y Dispositivos Móviles (Máster Universitario en Ingeniería Biomédica y Salud Digital, Universidad de Sevilla). Autora: María Miranda.

## Aplicación de partida

El proyecto base, proporcionado en el tema 5, permite dos operaciones sobre Firebase Realtime Database:

- Insertar un paciente generado aleatoriamente. Cada paciente se guarda en dos rutas: `Pacientes/<NUHSA>` y `<grupo sanguíneo>/<NUHSA>`.
- Buscar un paciente por su NUHSA y mostrar nombre, apellidos y grupo sanguíneo.

Es decir, implementa las operaciones Create y Read del modelo CRUD.

## Funcionalidades añadidas

1. Listado de pacientes (`ListaActivity`, `PacienteAdapter`). RecyclerView alimentado por un `ChildEventListener`, de modo que la lista se actualiza en tiempo real al añadir, modificar o borrar pacientes. Los pacientes se ordenan por apellidos y nombre.
2. Filtro por grupo sanguíneo. Un Spinner permite ver todos los pacientes o solo los de un grupo. El filtro lee directamente del nodo de cada grupo que ya crea la aplicación base.
3. Alta manual de pacientes (`FormularioActivity`). Formulario con NUHSA, nombre, apellidos, grupo sanguíneo (Spinner) y fecha de nacimiento (`DatePickerDialog`). Se valida que los campos estén completos, que el NUHSA solo tenga letras y números y que no exista ya otro paciente con el mismo NUHSA.
4. Edición de pacientes (Update). Desde la ficha del paciente se abre el mismo formulario con los datos cargados. El NUHSA no se puede modificar porque es la clave del registro. Si cambia el grupo sanguíneo, el paciente se elimina del nodo del grupo anterior.
5. Borrado de pacientes (Delete). Desde la ficha o con una pulsación larga en el listado, previa confirmación en un diálogo.
6. Fecha de nacimiento y edad. Nuevo campo `fechaNacimiento` en la clase `Paciente`. La edad se calcula en la aplicación y no se almacena (`@Exclude`). Los pacientes aleatorios también reciben una fecha de nacimiento.
7. Validación del NUHSA en la búsqueda, para evitar consultas vacías o con caracteres no admitidos en las claves de Firebase.

Las escrituras de edición y borrado se realizan con `updateChildren` sobre las dos rutas a la vez (`RepositorioPacientes`), de forma que la operación es atómica y las dos copias del paciente no quedan desincronizadas.

## Estructura

```
app/src/main/java/com/example/conexionrealtimedatabase/
    MainActivity.java          (base, ampliada)
    BuscaActivity.java         (base, ampliada: ficha con editar y borrar)
    Paciente.java              (base, ampliada: fecha de nacimiento, setters, edad)
    Constantes.java            (base, ampliada)
    ListaActivity.java         (nueva)
    PacienteAdapter.java       (nueva)
    FormularioActivity.java    (nueva)
    RepositorioPacientes.java  (nueva)
app/src/main/res/layout/
    activity_main.xml, activity_busca.xml, activity_lista.xml,
    activity_formulario.xml, item_paciente.xml
app/src/main/res/values/strings_ampliacion.xml
app/src/main/res/drawable/fondo_grupo.xml
```

## Ejecución

1. Abrir el proyecto en Android Studio.
2. Conectar la aplicación a un proyecto Firebase propio (Tools > Firebase > Realtime Database) para generar `app/google-services.json`.
3. Crear la base de datos Realtime Database y comprobar que las reglas permiten lectura y escritura.
4. Ejecutar en un emulador o dispositivo.
