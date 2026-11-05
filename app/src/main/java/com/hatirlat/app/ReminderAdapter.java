package com.hatirlat.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private final List<Reminder> reminderList;
    private final OnReminderClickListener listener;

    public interface OnReminderClickListener {
        void onReminderClick(Reminder reminder);
        void onCompleteClick(Reminder reminder);
    }

    public ReminderAdapter(List<Reminder> reminderList, OnReminderClickListener listener) {
        this.reminderList = reminderList;
        this.listener = listener;
    }

    // Listeyi dinamik olarak güncelleyip arayüzü yenileyen metot
    public void updateList(List<Reminder> newReminders) {
        reminderList.clear();
        reminderList.addAll(newReminders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        holder.tvTitle.setText(reminder.title);
        holder.tvDescription.setText(reminder.description);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(reminder.targetDateTime)));

        holder.cbComplete.setOnCheckedChangeListener(null);
        holder.cbComplete.setChecked(reminder.isCompleted);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReminderClick(reminder);
        });

        holder.cbComplete.setOnClickListener(v -> {
            if (listener != null) listener.onCompleteClick(reminder);
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDate;
        CheckBox cbComplete;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            cbComplete = itemView.findViewById(R.id.cbComplete);
        }
    }
}
