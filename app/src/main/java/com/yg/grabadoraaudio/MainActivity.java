package com.yg.grabadoraaudio;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.yg.grabadoraaudio.R;
import com.yg.grabadoraaudio.animacion;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button btnGrabar, btnPausar, btnParar;
    TextView txtTiempo;
    RecyclerView rcGrabaciones;
    ArrayList grabacionesArrayList = new ArrayList<>();

    ImageView imgmicro1;

    //CONSTANTES PARA PERMISOS
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    // Variables para grabacion
    private MediaRecorder grabacion;
    private String rutaArchivo;

    // Variables para el cronómetro
    private Handler handler = new Handler();
    private long startTime = 0;
    private long pausedTime = 0;
    private boolean isRecording = false;
    private boolean isPaused = false;
    private Runnable timerRunnable;

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

        btnGrabar = findViewById(R.id.btnrecord);
        btnParar = findViewById(R.id.btnstop);
        btnPausar = findViewById(R.id.btnpause);
        txtTiempo = findViewById(R.id.txtTime);
        rcGrabaciones = findViewById(R.id.rcGrabaciones);
        imgmicro1 = findViewById(R.id.imgmicro);

        configurarCronometro();

        verificarPermisos();

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
    private void configurarCronometro(){
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                //solo se actualiza si esta grabando
                if (isRecording && !isPaused){

                    long tiempoTranscurrido = System.currentTimeMillis() - startTime + pausedTime;
                    actualizarPantallaTiempo(tiempoTranscurrido);

                    handler.postDelayed(this, 100);
                }
            }
        };
    }

    //Actualiza el txtview de tiempo
    private void actualizarPantallaTiempo(long tiempoEnMilisegundos){
        int segundosTotales = (int) (tiempoEnMilisegundos / 1000);
        int minutos = segundosTotales / 60;
        int segundos = segundosTotales % 60;
        int centesimas = (int) ((tiempoEnMilisegundos % 1000) / 10);

        String tiempoFormateado = String.format(Locale.getDefault(), "%02d:%02d.%02d", minutos, segundos, centesimas);
        txtTiempo.setText(tiempoFormateado);
    }

    private void manejarBotonGrabar(){
        if (!isRecording){
            // No hay grabación activa - iniciar nueva grabacion
            iniciarNuevaGrabacion();
        } else if (isPaused){
            // Hay grabación pausada - reanudarla
            reanudarGrabacion();
        }
    }

    private void iniciarNuevaGrabacion(){
        try {
            // Crear nombre único con fecha y hora
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String nombreArchivo = "Grabacion_" + timestamp + ".3gp";

            //Crear directorio si no existe
            File directorioGrabaciones = new File(getExternalFilesDir(null), "Grabaciones");
            if (!directorioGrabaciones.exists()){
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
            startTime = System.currentTimeMillis(); // Momento actual
            pausedTime = 0; // Resetear tiempo pausado
            isRecording = true;
            isPaused = false;
            handler.post(timerRunnable); // Iniciar actualización del cronómetro

            btnGrabar.setBackgroundResource(R.drawable.icnpresionado);
            btnGrabar.setEnabled(false); // Deshabilitar mientras graba
            btnPausar.setEnabled(true);
            btnParar.setEnabled(true);

            // ANIMACION
            animacion grabar1 = new animacion();
            grabar1.startRecording(MainActivity.this, imgmicro1);

            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this, "Error al iniciar grabación", Toast.LENGTH_SHORT).show();
            resetearEstadoGrabacion();
        }
    }

    // Pausa la grabación actual o la reanuda si ya está pausada

    private void pausarReanudarGrabacion() {
        if (!isRecording) return; // No hay grabación activa

        if (!isPaused) {
            //  PAUSAR GRABACIÓN
            try {
                // Verificar que el dispositivo soporte pausa (Android N+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    grabacion.pause(); // Pausar MediaRecorder

                    //GUARDAR TIEMPO TRANSCURRIDO
                    // Acumular el tiempo que llevamos grabando antes de pausar
                    pausedTime += System.currentTimeMillis() - startTime;
                    isPaused = true;

                    // ========== ACTUALIZAR UI ==========
                    btnGrabar.setBackgroundResource(R.drawable.grabaricon);
                    btnGrabar.setEnabled(true); // Permitir reanudar desde botón grabar

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
                grabacion.resume(); // Reanudar MediaRecorder

                //REINICIAR CRONÓMETRO
                startTime = System.currentTimeMillis(); // Nuevo punto de referencia
                isPaused = false;
                handler.post(timerRunnable); // Continuar actualizando cronómetro

                // ========== ACTUALIZAR UI ==========

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
            //DETENER MEDIARECORDER
            grabacion.stop(); // Detiene la grabación
            grabacion.release(); // Liberar recursos

            //  CALCULAR DURACIÓN TOTAL
            long duracionTotal = pausedTime;
            if (!isPaused) {
                duracionTotal += System.currentTimeMillis() - startTime;
            }
            String duracionFormateada = formatearDuracion(duracionTotal);

            // DETENER ANIMACIÓN
            animacion grabar1 = new animacion();
            grabar1.stopRecording(imgmicro1);

            Toast.makeText(this, "Grabación guardada - Duración: " + duracionFormateada, Toast.LENGTH_SHORT).show();

        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al detener grabación", Toast.LENGTH_SHORT).show();
        }

        resetearEstadoGrabacion();
    }


    // Formatea la duración en milisegundos a formato MM:SS

    private String formatearDuracion(long duracionMs) {
        int segundosTotales = (int) (duracionMs / 1000);
        int minutos = segundosTotales / 60;
        int segundos = segundosTotales % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
    }

    // Resetea todas las variables y UI al estado inicial

    private void resetearEstadoGrabacion() {
        // ========== LIMPIAR VARIABLES ==========
        grabacion = null;
        isRecording = false;
        isPaused = false;
        startTime = 0;
        pausedTime = 0;

        // DETENER CRONÓMETRO
        handler.removeCallbacks(timerRunnable);

        // RESETEAR UI
        btnGrabar.setBackgroundResource(R.drawable.grabaricon);
        btnGrabar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnParar.setEnabled(false);
        txtTiempo.setText("00:00.00");
    }

    protected void onDestroy() {
        super.onDestroy();
        // ========== LIMPIAR RECURSOS ==========
        handler.removeCallbacks(timerRunnable); // Detener cronómetro
        if (grabacion != null) {
            grabacion.release(); // Liberar MediaRecorder
        }
    }


}