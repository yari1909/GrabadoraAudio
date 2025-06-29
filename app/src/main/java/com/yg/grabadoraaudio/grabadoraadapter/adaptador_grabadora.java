package com.yg.grabadoraaudio.grabadoraadapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yg.grabadoraaudio.R;
import com.yg.grabadoraaudio.grabadora.grabar;

import java.io.IOException;
import java.util.ArrayList;

public class adaptador_grabadora extends RecyclerView.Adapter<adaptador_grabadora.ViewHolder> {

    private ArrayList<grabar> grabaciones;
    private Context context;
    private MediaPlayer mediaPlayer;
    private int posicionReproduciendo = -1;

    // Interfaz para notificar un long click
    public interface OnItemLongClickListener {
        void onItemLongClick(int position, grabar grabacion);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public adaptador_grabadora(ArrayList<grabar> grabaciones, Context context) {
        this.grabaciones = grabaciones;
        this.context = context;
    }

    @NonNull
    @Override
    public adaptador_grabadora.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_grabaciones, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull adaptador_grabadora.ViewHolder holder, int position) {
        grabar grabacion = grabaciones.get(position);

        holder.txtNombre.setText(grabacion.getNombrePersonalizado());
        holder.txtDuracion.setText(grabacion.getDuracion());
        holder.txtFecha.setText(grabacion.getFechaCreacion());

        if (position == posicionReproduciendo && mediaPlayer != null && mediaPlayer.isPlaying()) {
            holder.btnReproducir.setText("PARAR");
        } else {
            holder.btnReproducir.setText("REPRODUCIR");
        }

        holder.btnReproducir.setOnClickListener(v -> {
            if (position == posicionReproduciendo && mediaPlayer != null && mediaPlayer.isPlaying()) {
                detenerReproduccion();
                notifyItemChanged(position);
            } else {
                reproducirAudio(grabacion.getRutaArchivo(), position);
            }
        });

        // Long click para borrar grabación individual
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position, grabacion);
                return true;
            }
            return false;
        });
    }

    private void reproducirAudio(String rutaArchivo, int position) {
        detenerReproduccion();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(rutaArchivo);
            mediaPlayer.prepare();
            mediaPlayer.start();

            posicionReproduciendo = position;
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

    private void detenerReproduccion() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        posicionReproduciendo = -1;
    }

    public void limpiarRecursos() {
        detenerReproduccion();
    }

    @Override
    public int getItemCount() {
        return grabaciones.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtDuracion, txtFecha;
        Button btnReproducir;

        public ViewHolder(View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreGrabacion);
            txtDuracion = itemView.findViewById(R.id.txtDuracionGrabacion);
            txtFecha = itemView.findViewById(R.id.txtFechaGrabacion);
            btnReproducir = itemView.findViewById(R.id.btnReproducir);
        }
    }
}