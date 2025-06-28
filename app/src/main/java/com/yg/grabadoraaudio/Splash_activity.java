package com.yg.grabadoraaudio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

public class Splash_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        Log.d("SplashDebug", "=== INICIANDO DIAGNÓSTICO ===");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Debug completo
        LottieAnimationView animationView = findViewById(R.id.imageView);

        Log.d("SplashDebug", "AnimationView encontrado: " + (animationView != null));

        if (animationView != null) {
            Log.d("SplashDebug", "Visibilidad: " + animationView.getVisibility());
            Log.d("SplashDebug", "Width: " + animationView.getWidth() + ", Height: " + animationView.getHeight());

            // Intentar cargar manualmente
            try {
                animationView.setAnimation("splashanimation.json");
                Log.d("SplashDebug", "Animación cargada desde assets");

                animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        Log.d("SplashDebug", "Animación INICIADA");
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Log.d("SplashDebug", "Animación TERMINADA");
                    }
                });

                animationView.playAnimation();
                Log.d("SplashDebug", "playAnimation() llamado");

            } catch (Exception e) {
                Log.e("SplashDebug", "Error cargando animación: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Verificar si el archivo existe
        try {
            String[] assets = getAssets().list("");
            Log.d("SplashDebug", "Archivos en assets:");
            for (String asset : assets) {
                Log.d("SplashDebug", "- " + asset);
            }
        } catch (Exception e) {
            Log.e("SplashDebug", "Error listando assets: " + e.getMessage());
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Splash_activity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 5000);
    }
}