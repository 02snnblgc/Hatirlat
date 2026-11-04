package com.hatirlat.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hatirlat.app.R;
import com.hatirlat.app.data.Reminder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        holder.tvTitle.setText(reminder.title);
        holder.tvCategory.setText(reminder.category);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(reminder.targetDateTime)));

        holder.cbCompleted.setChecked(reminder.isCompleted);

        holder.itemView.setOnClickListener(v -> listener.onReminderClick(reminder));
        holder.cbCompleted.setOnClickListener(v -> listener.onCompleteClick(reminder));
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDate;
        CheckBox cbCompleted;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            cbCompleted = itemView.findViewById(R.id.cbItemCompleted);
        }
    }
}
