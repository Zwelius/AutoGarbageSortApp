package com.example.autogarbagesortapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autogarbagesortapp.R;

import java.util.List;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.MaintenanceViewHolder> {

    private final List<MaintenanceLog> logList;

    public MaintenanceAdapter(List<MaintenanceLog> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public MaintenanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.maintenance_log_item, parent, false);
        return new MaintenanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaintenanceViewHolder holder, int position) {
        MaintenanceLog log = logList.get(position);
        holder.timestampTextView.setText(log.timestamp);
        holder.noteTextView.setText(log.note);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    static class MaintenanceViewHolder extends RecyclerView.ViewHolder {
        TextView timestampTextView, noteTextView;

        public MaintenanceViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
        }
    }
}

