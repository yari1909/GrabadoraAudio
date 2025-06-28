package com.yg.grabadoraaudio.grabadoraadapter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.OnClickAction;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.yg.grabadoraaudio.MainActivity;
import com.yg.grabadoraaudio.R;

public class Splash_activity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LottieAnimationView animationView = findViewById(R.id.imageView);
        animationView.setAnimation("splashanimation.json");
        animationView.playAnimation();

        // Usar Handler para tiempo fijo de splash (recomendado para mejor control)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splash_activity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Cerrar SplashActivity para que no vuelva con el botón atrás
            }
        }, 5000);
    }
    }
