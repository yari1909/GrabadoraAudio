package com.yg.grabadoraaudio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.yg.grabadoraaudio.grabadora.grabar;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button btnGrabar, btnPausar, btnParar;
    TextView txtTime;
    ImageView imgmicro1;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private MediaRecorder grabacion;
    private String rutaArchivo;

    private long startTime = 0;
    private long pauseStartTime = 0;
    private long pausedDuration = 0;
    private boolean isRecording = false;
    private boolean isPaused = false;

    private Handler handler = new Handler();
    private Runnable timerRunnable;

    private long tiempoFinalGrabacion = 0;

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
        btnPausar = findViewById(R.id.btnpause);
        btnParar = findViewById(R.id.btnstop);
        txtTime = findViewById(R.id.txtTime);
        imgmicro1 = findViewById(R.id.imgmicro);

        MaterialToolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        verificarPermisos();
        configurarCronometro();

        btnPausar.setEnabled(false);
        btnParar.setEnabled(false);

        btnGrabar.setOnClickListener(v -> manejarBotonGrabar());
        btnParar.setOnClickListener(v -> detenerGrabacion());
        btnPausar.setOnClickListener(v -> pausarReanudarGrabacion());
    }

    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permiso de grabación denegado.", Toast.LENGTH_LONG).show();
                btnGrabar.setEnabled(false);
            }
        }
    }

    private void configurarCronometro() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    long elapsedTime = isPaused
                            ? pauseStartTime - startTime - pausedDuration
                            : System.currentTimeMillis() - startTime - pausedDuration;

                    tiempoFinalGrabacion = elapsedTime;
                    actualizarPantallaTiempo(elapsedTime);

                    if (!isPaused) {
                        handler.postDelayed(this, 100);
                    }
                }
            }
        };
    }

    private void actualizarPantallaTiempo(long ms) {
        int min = (int) (ms / 60000);
        int seg = (int) ((ms % 60000) / 1000);
        int cen = (int) ((ms % 1000) / 10);
        txtTime.setText(String.format(Locale.getDefault(), "%02d:%02d.%02d", min, seg, cen));
    }

    private void manejarBotonGrabar() {
        if (!isRecording) iniciarNuevaGrabacion();
        else if (isPaused) reanudarGrabacion();
    }

    private void iniciarNuevaGrabacion() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String nombreArchivo = "Grabacion_" + timestamp + ".3gp";

            File dir = new File(getExternalFilesDir(null), "Grabaciones");
            if (!dir.exists()) dir.mkdirs();

            rutaArchivo = new File(dir, nombreArchivo).getAbsolutePath();

            grabacion = new MediaRecorder();
            grabacion.setAudioSource(MediaRecorder.AudioSource.MIC);
            grabacion.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            grabacion.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            grabacion.setOutputFile(rutaArchivo);
            grabacion.prepare();
            grabacion.start();

            startTime = System.currentTimeMillis();
            pausedDuration = 0;
            pauseStartTime = 0;
            isRecording = true;
            isPaused = false;
            handler.post(timerRunnable);

            btnGrabar.setEnabled(false);
            btnPausar.setEnabled(true);
            btnParar.setEnabled(true);

            // animacion
            new animacion().startRecording(this, imgmicro1);

            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            resetearEstadoGrabacion();
            Toast.makeText(this, "Error al iniciar grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void pausarReanudarGrabacion() {
        if (!isRecording) return;

        try {
            if (!isPaused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                grabacion.pause();
                pauseStartTime = System.currentTimeMillis();
                isPaused = true;
                handler.removeCallbacks(timerRunnable);

                btnGrabar.setEnabled(true);
                new animacion().stopRecording(imgmicro1);

                Toast.makeText(this, "Grabación pausada", Toast.LENGTH_SHORT).show();
            } else {
                reanudarGrabacion();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void reanudarGrabacion() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                grabacion.resume();
                pausedDuration += (System.currentTimeMillis() - pauseStartTime);
                isPaused = false;
                handler.post(timerRunnable);

                btnGrabar.setEnabled(false);
                new animacion().startRecording(this, imgmicro1);

                Toast.makeText(this, "Grabación reanudada", Toast.LENGTH_SHORT).show();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void detenerGrabacion() {
        if (!isRecording || grabacion == null) return;

        try {
            grabacion.stop();
            grabacion.release();

            String duracion = formatearDuracion(tiempoFinalGrabacion);
            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            String nombre = "Grabación " + (contarGrabaciones() + 1);

            guardarGrabacion(nombre, rutaArchivo, duracion, fecha);

            new animacion().stopRecording(imgmicro1);
            Toast.makeText(this, "Grabación guardada", Toast.LENGTH_SHORT).show();
        } catch (RuntimeException e) {
            e.printStackTrace();
            File f = new File(rutaArchivo);
            if (f.exists()) f.delete();
        }

        resetearEstadoGrabacion();
    }

    private void resetearEstadoGrabacion() {
        grabacion = null;
        isRecording = false;
        isPaused = false;
        startTime = 0;
        pauseStartTime = 0;
        pausedDuration = 0;
        tiempoFinalGrabacion = 0;

        handler.removeCallbacks(timerRunnable);
        btnGrabar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnParar.setEnabled(false);
        txtTime.setText("00:00.00");
    }

    private void guardarGrabacion(String nombre, String ruta, String duracion, String fecha) {
        Set<String> set = new HashSet<>(sharedPreferences.getStringSet(KEY_GRABACIONES, new HashSet<>()));
        set.add(ruta + "|" + duracion + "|" + fecha + "|" + nombre);
        sharedPreferences.edit().putStringSet(KEY_GRABACIONES, set).apply();
    }

    private int contarGrabaciones() {
        return sharedPreferences.getStringSet(KEY_GRABACIONES, new HashSet<>()).size();
    }

    private String formatearDuracion(long ms) {
        int min = (int) (ms / 60000);
        int seg = (int) ((ms % 60000) / 1000);
        int cen = (int) ((ms % 1000) / 10);
        return String.format(Locale.getDefault(), "%02d:%02d.%02d", min, seg, cen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_lista) {
            startActivity(new Intent(this, Activity_Grabaciones.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(timerRunnable);
        if (grabacion != null) grabacion.release();
        super.onDestroy();
    }
}
