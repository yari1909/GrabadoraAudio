package com.yg.grabadoraaudio.grabadoraadapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yg.grabadoraaudio.R;
import com.yg.grabadoraaudio.grabadora.grabar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class adaptador_grabadora extends RecyclerView.Adapter<adaptador_grabadora.ViewHolder> {

    private ArrayList<grabar> grabaciones;
    private Context context;
    private MediaPlayer mediaPlayer;
    private int posicionReproduciendo = -1;

    private boolean estaPausado = false;
    private int posicionPausada = 0;

    private boolean modoSeleccion = false;
    private Set<Integer> seleccionados = new HashSet<>();
    private Runnable onEliminarSeleccionados;

    public adaptador_grabadora(ArrayList<grabar> grabaciones, Context context) {
        this.grabaciones = grabaciones;
        this.context = context;
    }

    public void setLista(ArrayList<grabar> nuevaLista) {
        this.grabaciones = nuevaLista;
        seleccionados.clear();
        notifyDataSetChanged();
    }

    public void activarSeleccion(boolean activar) {
        modoSeleccion = activar;
        if (!activar) seleccionados.clear();
        notifyDataSetChanged();
    }

    public void setOnEliminarSeleccionados(Runnable callback) {
        this.onEliminarSeleccionados = callback;
    }

    public List<grabar> obtenerSeleccionados() {
        List<grabar> lista = new ArrayList<>();
        for (Integer pos : seleccionados) {
            if (pos >= 0 && pos < grabaciones.size()) {
                lista.add(grabaciones.get(pos));
            }
        }
        return lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grabacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        grabar grabacion = grabaciones.get(position);

        holder.txtNombre.setText(grabacion.getNombrePersonalizado());
        holder.txtDuracion.setText(grabacion.getDuracion());
        holder.txtFecha.setText(grabacion.getFechaCreacion());

        if (modoSeleccion) {
            holder.checkboxSeleccion.setVisibility(View.VISIBLE);
            holder.btnReproducir.setVisibility(View.GONE);
            holder.checkboxSeleccion.setChecked(seleccionados.contains(position));

            holder.checkboxSeleccion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) seleccionados.add(position);
                else seleccionados.remove(position);
            });

        } else {
            holder.checkboxSeleccion.setVisibility(View.GONE);
            holder.btnReproducir.setVisibility(View.VISIBLE);

            if (position == posicionReproduciendo && mediaPlayer != null) {
                holder.btnReproducir.setText(estaPausado ? "REANUDAR" : "PAUSAR");
            } else {
                holder.btnReproducir.setText("REPRODUCIR");
            }

            holder.btnReproducir.setOnClickListener(v -> {
                if (position == posicionReproduciendo) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        pausarAudio();
                        notifyItemChanged(position);
                    } else if (estaPausado) {
                        reanudarAudio();
                        notifyItemChanged(position);
                    } else {
                        reproducirAudio(grabacion.getRutaArchivo(), position);
                    }
                } else {
                    reproducirAudio(grabacion.getRutaArchivo(), position);
                }
            });
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (!modoSeleccion && longClickListener != null) {
                longClickListener.onItemLongClick(position, grabacion);
                return true;
            }
            return false;
        });
    }

    private void reproducirAudio(String rutaArchivo, int position) {
        detenerReproduccion();

        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                Toast.makeText(context, "Archivo no encontrado:\n" + rutaArchivo, Toast.LENGTH_LONG).show();
                return;
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(rutaArchivo);
            mediaPlayer.prepare();
            mediaPlayer.start();

            posicionReproduciendo = position;
            estaPausado = false;
            posicionPausada = 0;

            notifyItemChanged(position);

            mediaPlayer.setOnCompletionListener(mp -> {
                detenerReproduccion();
                notifyItemChanged(position);
            });

            Toast.makeText(context, "Reproduciendo...", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al reproducir audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void pausarAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            posicionPausada = mediaPlayer.getCurrentPosition();
            estaPausado = true;
            Toast.makeText(context, "Pausado", Toast.LENGTH_SHORT).show();
        }
    }

    private void reanudarAudio() {
        if (mediaPlayer != null && estaPausado) {
            mediaPlayer.seekTo(posicionPausada);
            mediaPlayer.start();
            estaPausado = false;
            Toast.makeText(context, "Reanudando...", Toast.LENGTH_SHORT).show();
        }
    }

    private void detenerReproduccion() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying() || estaPausado) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        posicionReproduciendo = -1;
        estaPausado = false;
        posicionPausada = 0;
    }

    public void limpiarRecursos() {
        detenerReproduccion();
    }

    @Override
    public int getItemCount() {
        return grabaciones.size();
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, grabar grabacion);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtDuracion, txtFecha;
        Button btnReproducir;
        CheckBox checkboxSeleccion;

        public ViewHolder(View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreGrabacion);
            txtDuracion = itemView.findViewById(R.id.txtDuracionGrabacion);
            txtFecha = itemView.findViewById(R.id.txtFechaGrabacion);
            btnReproducir = itemView.findViewById(R.id.btnReproducir);
            checkboxSeleccion = itemView.findViewById(R.id.checkboxSeleccion);
        }
    }
}

