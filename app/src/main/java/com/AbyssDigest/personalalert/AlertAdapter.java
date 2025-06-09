package com.AbyssDigest.personalalert;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.AbyssDigest.personalalert.database.AppDatabase;
import com.AbyssDigest.personalalert.database.Alert;

import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private List<Alert> alerts;
    private Context context;

    public AlertAdapter(List<Alert> alerts) {
        this.alerts = alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
        notifyDataSetChanged();
    }


    public void deleteItem(int position, View view) {
        Alert alertToDelete = alerts.get(position);
        alerts.remove(position);
        notifyItemRemoved(position);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(view.getContext().getApplicationContext());
            db.alertDao().delete(alertToDelete);
            for (int i = 0; i < alerts.size(); i++) {
                Alert alert = alerts.get(i);
                alert.order = i;
                db.alertDao().update(alert);
            }
        }).start();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        Alert movedAlert = alerts.remove(fromPosition);
        alerts.add(toPosition, movedAlert);
        notifyItemMoved(fromPosition, toPosition);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            for (int i = 0; i < alerts.size(); i++) {
                Alert alert = alerts.get(i);
                alert.order = i;
                db.alertDao().update(alert);
            }
        }).start();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alert_item, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alerts.get(position);
//        Log.d("AlertAdapter", "Binding view for position: " + position + ", alert name: " + alert.name);
        holder.nameTextView.setText(alert.name);
        holder.keywordsTextView.setText(alert.keywords);
        if (alert.sound != null && !alert.sound.isEmpty()) {
            Uri soundUri = Uri.parse(alert.sound);
            Ringtone ringtone = RingtoneManager.getRingtone(holder.itemView.getContext(), soundUri);
            String name = ringtone.getTitle(holder.itemView.getContext());
            if (name != null && name.toLowerCase().contains("unknown")) {
                name = soundUri.getLastPathSegment();
                if (name != null && name.contains(".")) {
                    name = name.substring(0, name.lastIndexOf('.'));
                }
            }
            holder.soundTextView.setText(name);
        }

        holder.activeSwitch.setChecked(alert.isActive);
        holder.activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alert.isActive = isChecked;
            new Thread(() -> {
                AppDatabase.getDatabase(context).alertDao().update(alert);
            }).start();
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddEditAlertActivity.class);
            intent.putExtra("alert_id", alert.id);
            v.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
//        Log.d("AlertAdapter", "getItemCount: " + alerts.size());
        return alerts.size();
    }

    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView keywordsTextView;
        TextView soundTextView;
        SwitchCompat activeSwitch;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            keywordsTextView = itemView.findViewById(R.id.keywordsTextView);
            soundTextView = itemView.findViewById(R.id.soundTextView);
            activeSwitch = itemView.findViewById(R.id.activeSwitch);
        }
    }
}
