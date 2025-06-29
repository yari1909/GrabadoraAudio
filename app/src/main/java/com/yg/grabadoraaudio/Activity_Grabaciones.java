package com.yg.grabadoraaudio;

import android.content.SharedPreferences;
import android.os.Bundle;
import com.yg.grabadoraaudio.grabadora.grabar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.yg.grabadoraaudio.grabadoraadapter.adaptador_grabadora;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Activity_Grabaciones extends AppCompatActivity {
    RecyclerView recyclerView;
    adaptador_grabadora adapter;
    ArrayList<grabar> grabacionesArrayList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "GrabacionesPrefs";
    private static final String KEY_GRABACIONES = "grabaciones_list";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabaciones);

        recyclerView = findViewById(R.id.rcGrabaciones);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        cargarGrabaciones();
    }

    private void cargarGrabaciones() {
        Set<String> grabacionesSet = sharedPreferences.getStringSet(KEY_GRABACIONES, new HashSet<>());
        grabacionesArrayList.clear();

        for (String item : grabacionesSet) {
            String[] partes = item.split("\\|");
            if (partes.length == 4) {
                String ruta = partes[0];
                String duracion = partes[1];
                String fecha = partes[2];
                String nombre = partes[3];
                grabacionesArrayList.add(new grabar(nombre, ruta, duracion, fecha));
            }
        }

        adapter = new adaptador_grabadora(grabacionesArrayList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
