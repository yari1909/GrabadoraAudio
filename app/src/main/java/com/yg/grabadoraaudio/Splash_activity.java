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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Configurar animación Lottie
        LottieAnimationView animationView = findViewById(R.id.imageView);

        // Timer de seguridad - siempre va a MainActivity después de 5 segundos máximo
        new Handler().postDelayed(this::goToMainActivity, 5000);

        if (animationView != null) {
            try {
                animationView.setAnimation("splashanimation.json");

                animationView.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Ir a MainActivity cuando termine la animación
                        goToMainActivity();
                    }
                });

                animationView.playAnimation();
            } catch (Exception e) {
                // Si hay error con la animación, ir directo a MainActivity
                new Handler().postDelayed(this::goToMainActivity, 2000);
            }
        } else {
            // Si no hay animación, usar delay fijo
            new Handler().postDelayed(this::goToMainActivity, 3000);
        }
    }

    private void goToMainActivity() {

            Intent intent = new Intent(Splash_activity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
