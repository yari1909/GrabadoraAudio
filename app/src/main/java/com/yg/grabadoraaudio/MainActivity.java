package com.yg.grabadoraaudio;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.yg.grabadoraaudio.grabadora.grabar;
import com.yg.grabadoraaudio.grabadoraadapter.adaptador_grabadora;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Button btnGrabar, btnPausar, btnParar;
    TextView txtTime;
    RecyclerView rcGrabaciones;

    adaptador_grabadora adapter;
    ArrayList<grabar> grabacionesArrayList = new ArrayList<>();

    ImageView imgmicro1;

    //CONSTANTES PARA PERMISOS
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private long startTime = 0;         // Momento en que inicia o reanuda la grabación
    private long pauseStartTime = 0;    // Momento en que se pausó
    private long pausedDuration = 0;    // Tiempo total acumulado en pausa
    private boolean isRecording = false;
    private boolean isPaused = false;

    // Variables para grabacion
    private MediaRecorder grabacion;
    private String rutaArchivo;

    // Variables para el cronómetro
    private Handler handler = new Handler();

    private Runnable timerRunnable;

    // Variable para guardar el tiempo exacto de la grabación (con pausas consideradas)
    private long tiempoFinalGrabacion = 0;

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "GrabacionesPrefs";
    private static final String KEY_GRABACIONES = "grabaciones_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        btnGrabar = findViewById(R.id.btnrecord);
        btnParar = findViewById(R.id.btnstop);
        txtTime = findViewById(R.id.txtTime);
        btnPausar = findViewById(R.id.btnpause);
        rcGrabaciones = findViewById(R.id.rcGrabaciones);
        imgmicro1 = findViewById(R.id.imgmicro);
        MaterialToolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // Verificar permisos al iniciar
        verificarPermisos();

        // Configurar componentes
        configurarCronometro();
        configurarRecyclerView();
// cargarGrabaciones();

        btnPausar.setEnabled(false);
        btnParar.setEnabled(false);

        btnGrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manejarBotonGrabar();
            }
        });

        btnParar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detenerGrabacion();
            }
        });

        btnPausar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausarReanudarGrabacion();
            }
        });
    }

    private void configurarRecyclerView() {
        // USAR TU ADAPTADOR
        adapter = new adaptador_grabadora(grabacionesArrayList, this);
        rcGrabaciones.setLayoutManager(new LinearLayoutManager(this));
        rcGrabaciones.setAdapter(adapter);
    }


    private void guardarGrabacion(String nombre, String ruta, String duracion, String fecha) {
        Set<String> grabacionesSet = new HashSet<>(sharedPreferences.getStringSet(KEY_GRABACIONES, new HashSet<>()));

        // Guarda en SharedPreferences normalmente (puedes mantener todas guardadas)
        String grabacionInfo = ruta + "|" + duracion + "|" + fecha + "|" + nombre;
        grabacionesSet.add(grabacionInfo);
        sharedPreferences.edit().putStringSet(KEY_GRABACIONES, grabacionesSet).apply();

        // Limpiar la lista para mostrar solo la última grabación
        grabacionesArrayList.clear();

        // Agregar solo la nueva grabación a la lista
        grabar nueva = new grabar(nombre, ruta, duracion, fecha);
        grabacionesArrayList.add(nueva);

        // Notificar al adaptador para actualizar la vista
        adapter.notifyDataSetChanged();
    }

    /**
     * Verifica si tenemos permisos de grabación, si no los solicita
     */
    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Solicitar permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    /**
     * Maneja la respuesta del usuario a la solicitud de permisos
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de grabación concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de grabación denegado. La app no funcionará correctamente.",
                        Toast.LENGTH_LONG).show();
                btnGrabar.setEnabled(false); // Deshabilitar botón si no hay permisos
            }
        }
    }

    /**
     * Verifica si tenemos permisos antes de grabar
     */
    private boolean tienePermisosGrabacion() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    //Configuracion de cronometro que se actualiza cada 100 miliseg
    private void configurarCronometro() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    long elapsedTime;
                    if (isPaused) {
                        elapsedTime = pauseStartTime - startTime - pausedDuration;
                    } else {
                        elapsedTime = System.currentTimeMillis() - startTime - pausedDuration;
                    }

                    tiempoFinalGrabacion = elapsedTime;  // Aquí guardamos el tiempo real exacto

                    actualizarPantallaTiempo(elapsedTime);
                    if (!isPaused) {
                        handler.postDelayed(this, 100);
                    }
                }
            }
        };
    }

    //Actualiza el txtview de tiempo
    private void actualizarPantallaTiempo(long tiempoEnMilisegundos) {
        int segundosTotales = (int) (tiempoEnMilisegundos / 1000);
        int minutos = segundosTotales / 60;
        int segundos = segundosTotales % 60;
        int centesimas = (int) ((tiempoEnMilisegundos % 1000) / 10);

        String tiempoFormateado = String.format(Locale.getDefault(), "%02d:%02d.%02d", minutos, segundos, centesimas);
        txtTime.setText(tiempoFormateado);
    }

    private void manejarBotonGrabar() {
        if (!isRecording) {
            // No hay grabación activa - iniciar nueva grabacion
            iniciarNuevaGrabacion();
        } else if (isPaused) {
            // Hay grabación pausada - reanudarla
            reanudarGrabacion();
        }
    }

    private void iniciarNuevaGrabacion() {
        try {
            // Crear nombre único con fecha y hora
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String nombreArchivo = "Grabacion_" + timestamp + ".3gp";

            //Crear directorio si no existe
            File directorioGrabaciones = new File(getExternalFilesDir(null), "Grabaciones");
            if (!directorioGrabaciones.exists()) {
                directorioGrabaciones.mkdirs();
            }


            // Ruta completa del archivo
            rutaArchivo = new File(directorioGrabaciones, nombreArchivo).getAbsolutePath();

            // CONFIGURACION MEDIARECORDER
            grabacion = new MediaRecorder();
            grabacion.setAudioSource(MediaRecorder.AudioSource.MIC);
            grabacion.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // Formato 3GP
            grabacion.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // Codificador de audio
            grabacion.setOutputFile(rutaArchivo); // Archivo donde guardar

            grabacion.prepare();
            grabacion.start();

            // INICIAR EL CRONOMETRO

            startTime = System.currentTimeMillis();
            pausedDuration = 0;
            pauseStartTime = 0;
            isRecording = true;
            isPaused = false;
            handler.post(timerRunnable);

            btnGrabar.setBackgroundResource(R.drawable.icnpresionado);
            btnGrabar.setEnabled(false); // Deshabilitar mientras graba
            btnPausar.setEnabled(true);
            btnParar.setEnabled(true);

            //Animación
            animacion grabar1 = new animacion();
            grabar1.startRecording(MainActivity.this, imgmicro1);

            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al iniciar grabación", Toast.LENGTH_SHORT).show();
            resetearEstadoGrabacion();
        }
    }

    // Pausa la grabación actual o la reanuda si ya está pausada
    private void pausarReanudarGrabacion() {
        if (!isRecording) return;

        if (!isPaused) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    grabacion.pause();

                    pauseStartTime = System.currentTimeMillis();
                    isPaused = true;

                    handler.removeCallbacks(timerRunnable); // Pausa cronómetro

                    // Actualizar UI y animación
                    btnGrabar.setBackgroundResource(R.drawable.grabaricon);
                    btnGrabar.setEnabled(true);
                    animacion grabar1 = new animacion();
                    grabar1.stopRecording(imgmicro1);

                    Toast.makeText(this, "Grabación pausada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Pausa no disponible en esta versión de Android", Toast.LENGTH_SHORT).show();
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al pausar", Toast.LENGTH_SHORT).show();
            }
        } else {
            reanudarGrabacion();
        }
    }

    //Reanuda una grabación pausada
    private void reanudarGrabacion() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                grabacion.resume();

                long pauseEndTime = System.currentTimeMillis();
                pausedDuration += (pauseEndTime - pauseStartTime);

                isPaused = false;
                handler.post(timerRunnable);

                btnGrabar.setBackgroundResource(R.drawable.icnpresionado);
                btnGrabar.setEnabled(false);

                animacion grabar1 = new animacion();
                grabar1.startRecording(MainActivity.this, imgmicro1);

                Toast.makeText(this, "Grabación reanudada", Toast.LENGTH_SHORT).show();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al reanudar", Toast.LENGTH_SHORT).show();
        }
    }

    // Detiene completamente la grabación y guarda el archivo
    private void detenerGrabacion() {
        if (!isRecording || grabacion == null) return;

        try {
            // DETENER MEDIARECORDER
            grabacion.stop();
            grabacion.release();

            // Usar el tiempo calculado en el cronómetro para guardar duración
            String duracionFormateada = formatearDuracion(tiempoFinalGrabacion);

            // ================= FECHA Y NOMBRE =================

            // Fecha del día para numeración (solo dd/MM/yyyy)
            String fechaSolo = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            // Contar cuántas grabaciones se han hecho hoy
            int numeroTotal = contarTodasGrabaciones() + 1;

            // Crear nombre personalizado como "Grabación 01"
            String nombrePersonalizado = "Grabación " + String.format("%02d", numeroTotal);

            // Fecha y hora completa para guardar
            String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            // ================= GUARDAR Y MOSTRAR =================

            guardarGrabacion(nombrePersonalizado, rutaArchivo, duracionFormateada, timestamp);

            Toast.makeText(this,
                    "Grabación guardada\nNombre: " + nombrePersonalizado +
                            "\nDuración: " + duracionFormateada +
                            "\nFecha: " + timestamp,
                    Toast.LENGTH_LONG).show();

        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al detener grabación", Toast.LENGTH_SHORT).show();
        }

        resetearEstadoGrabacion();
    }


    // metodo contar grabaciones
    private int contarTodasGrabaciones() {
        Set<String> grabacionesSet = sharedPreferences.getStringSet(KEY_GRABACIONES, new HashSet<>());
        return grabacionesSet.size();
    }
    // Formatea la duración en milisegundos a formato MM:SS
    private String formatearDuracion(long duracionMs) {
        int minutos = (int) (duracionMs / 60000);
        int segundos = (int) ((duracionMs % 60000) / 1000);
        int centesimas = (int) ((duracionMs % 1000) / 10);
        return String.format(Locale.getDefault(), "%02d:%02d.%02d", minutos, segundos, centesimas);
    }
    private void mostrarDialogoBorrarTodo() {
        if (grabacionesArrayList.isEmpty()) {
            Toast.makeText(this, "No hay grabaciones para borrar", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Borrar todas las grabaciones")
                .setMessage("¿Estás segura de que deseas eliminar todas las grabaciones?")
                .setPositiveButton("Sí", (dialog, which) -> borrarTodasGrabaciones())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void borrarTodasGrabaciones() {
        // Eliminar archivos
        for (grabar g : grabacionesArrayList) {
            File archivo = new File(g.getRutaArchivo());
            if (archivo.exists()) {
                archivo.delete();
            }
        }

        // Limpiar SharedPreferences
        sharedPreferences.edit().remove(KEY_GRABACIONES).apply();

        // Limpiar la lista y actualizar
        grabacionesArrayList.clear();
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Todas las grabaciones han sido eliminadas", Toast.LENGTH_SHORT).show();
    }

    // Resetea todas las variables y UI al estado inicial
    private void resetearEstadoGrabacion() {
        // ========== LIMPIAR VARIABLES ==========
        grabacion = null;
        isRecording = false;
        isPaused = false;
        startTime = 0;
        pausedDuration = 0;
        pauseStartTime = 0;
        tiempoFinalGrabacion = 0;

        // DETENER CRONÓMETRO
        handler.removeCallbacks(timerRunnable);

        // RESETEAR UI
        btnGrabar.setBackgroundResource(R.drawable.grabaricon);
        btnGrabar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnParar.setEnabled(false);
        txtTime.setText("00:00.00");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ========== LIMPIAR RECURSOS ==========
        handler.removeCallbacks(timerRunnable); // Detener cronómetro
        if (grabacion != null) {
            grabacion.release(); // Liberar MediaRecorder
        }
        if (adapter != null) {
            adapter.limpiarRecursos(); // Liberar MediaPlayer del adapter
        }
    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_borrar) {
            mostrarDialogoBorrarTodo(); // Aquí llamamos al AlertDialog
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
