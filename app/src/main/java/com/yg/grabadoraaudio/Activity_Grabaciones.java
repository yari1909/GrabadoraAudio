package com.yg.grabadoraaudio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.yg.grabadoraaudio.grabadora.grabar;
import com.yg.grabadoraaudio.grabadoraadapter.adaptador_grabadora;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Activity_Grabaciones extends AppCompatActivity {

    RecyclerView rcGrabaciones;
    adaptador_grabadora adapter;
    ArrayList<grabar> listaGrabaciones = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "GrabacionesPrefs";
    private static final String KEY_GRABACIONES = "grabaciones_list";

    private boolean modoSeleccion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabaciones);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        rcGrabaciones = findViewById(R.id.rcGrabaciones);
        MaterialToolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        cargarGrabaciones();
        adapter = new adaptador_grabadora(listaGrabaciones, this);
        rcGrabaciones.setLayoutManager(new LinearLayoutManager(this));
        rcGrabaciones.setAdapter(adapter);
    }

    private void cargarGrabaciones() {
        listaGrabaciones.clear();
        Set<String> grabacionesSet = sharedPreferences.getStringSet(KEY_GRABACIONES, new HashSet<>());
        for (String info : grabacionesSet) {
            String[] partes = info.split("\\|");
            if (partes.length == 4) {
                grabar g = new grabar(partes[3], partes[0], partes[1], partes[2]);
                listaGrabaciones.add(g);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grabacionestoolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_borrar) {
            if (modoSeleccion) {
                eliminarSeleccionados();
            } else {
                mostrarDialogoOpcionesBorrar();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoOpcionesBorrar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar grabaciones")
                .setItems(new CharSequence[]{"Eliminar todas", "Seleccionar cuáles eliminar"}, (dialog, which) -> {
                    if (which == 0) {
                        confirmarEliminarTodas();
                    } else if (which == 1) {
                        activarModoSeleccion();
                    }
                })
                .show();
    }

    private void confirmarEliminarTodas() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar")
                .setMessage("¿Deseas eliminar todas las grabaciones?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarTodas())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTodas() {
        sharedPreferences.edit().remove(KEY_GRABACIONES).apply();
        listaGrabaciones.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Todas las grabaciones han sido eliminadas", Toast.LENGTH_SHORT).show();
    }

    private void activarModoSeleccion() {
        modoSeleccion = true;
        adapter.activarSeleccion(true);
        adapter.setOnEliminarSeleccionados(() -> eliminarSeleccionados());
    }

    private void eliminarSeleccionados() {
        Set<String> grabacionesSet = new HashSet<>(sharedPreferences.getStringSet(KEY_GRABACIONES, new HashSet<>()));
        List<grabar> seleccionados = adapter.obtenerSeleccionados();

        for (Iterator<String> iterator = grabacionesSet.iterator(); iterator.hasNext(); ) {
            String grabacionStr = iterator.next();
            for (grabar g : seleccionados) {
                if (grabacionStr.contains(g.getRutaArchivo())) {
                    iterator.remove();
                    break;
                }
            }
        }

        sharedPreferences.edit().putStringSet(KEY_GRABACIONES, grabacionesSet).apply();
        cargarGrabaciones();
        adapter.setLista(listaGrabaciones);
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Grabaciones seleccionadas eliminadas", Toast.LENGTH_SHORT).show();
        modoSeleccion = false;
        adapter.activarSeleccion(false);
    }
}
