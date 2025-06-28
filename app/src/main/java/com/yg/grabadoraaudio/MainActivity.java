package com.yg.grabadoraaudio;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.yg.grabadoraaudio.R;
import com.yg.grabadoraaudio.animacion;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button btnGrabar, btnPausar, btnParar;
    TextView txtTiempo;
    RecyclerView rcGrabaciones;
    ArrayList grabacionesArrayList = new ArrayList<>();

    ImageView imgmicro1;


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
        rcGrabaciones = findViewById(R.id.rcGrabaciones);
        imgmicro1 = findViewById(R.id.imgmicro);

        btnGrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animacion grabar1 = new animacion();
                grabar1.startRecording(MainActivity.this, imgmicro1);

                ;
            }
        });

        btnParar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animacion grabar1 = new animacion();
                grabar1.stopRecording(imgmicro1);
            }
        });

        btnPausar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animacion grabar1 = new animacion();
                grabar1.stopRecording(imgmicro1);
            }
        });
    }
}