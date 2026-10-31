package com.example.conexionrealtimedatabase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//AMPLIACIÓN: adaptador del RecyclerView. Convierte cada Paciente en una fila (item_paciente.xml)
public class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.PacienteViewHolder> {

    public interface OnPacienteClickListener {
        void onPacienteClick(Paciente paciente);
        void onPacienteLongClick(Paciente paciente);
    }

    private final List<Paciente> pacientes;
    private final OnPacienteClickListener listener;

    public PacienteAdapter(List<Paciente> pacientes, OnPacienteClickListener listener) {
        this.pacientes = pacientes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paciente, parent, false);
        return new PacienteViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder holder, int position) {
        Paciente paciente = pacientes.get(position);
        holder.tvNombreCompleto.setText(paciente.getApellidos() + ", " + paciente.getNombre());
        String detalle = paciente.getNuhsa();
        int edad = paciente.getEdad();
        if (edad >= 0) {
            detalle += "  ·  " + edad + " años";
        }
        holder.tvDetalle.setText(detalle);
        holder.tvGrupo.setText(paciente.getGrupoSanguineo());

        holder.itemView.setOnClickListener(v -> listener.onPacienteClick(paciente));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onPacienteLongClick(paciente);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return pacientes.size();
    }

    static class PacienteViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombreCompleto, tvDetalle, tvGrupo;

        PacienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCompleto = itemView.findViewById(R.id.tvNombreCompleto);
            tvDetalle = itemView.findViewById(R.id.tvDetalle);
            tvGrupo = itemView.findViewById(R.id.tvGrupo);
        }
    }
}
