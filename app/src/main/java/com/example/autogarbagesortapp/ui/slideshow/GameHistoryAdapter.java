package com.example.autogarbagesortapp.ui.slideshow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autogarbagesortapp.R;

import java.util.List;

public class GameHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<GameRecord> gameRecords;

    public GameHistoryAdapter(List<GameRecord> gameRecords) {
        this.gameRecords = gameRecords;
    }

    public void setGameRecords(List<GameRecord> gameRecords) {
        this.gameRecords = gameRecords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View headerView = inflater.inflate(R.layout.game_record_header, parent, false);
            return new HeaderViewHolder(headerView);
        } else {
            View itemView = inflater.inflate(R.layout.game_record_item, parent, false);
            return new GameRecordViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            // No data to bind for the header
        } else if (holder instanceof GameRecordViewHolder) {
            GameRecordViewHolder itemHolder = (GameRecordViewHolder) holder;
            GameRecord currentItem = gameRecords.get(position - 1); // Subtract 1 for the header
            itemHolder.team1NameTextView.setText(currentItem.getTeam1Name());
            itemHolder.team2NameTextView.setText(currentItem.getTeam2Name());
            itemHolder.team1ScoreTextView.setText(String.valueOf(currentItem.getTeam1Score()));
            itemHolder.team2ScoreTextView.setText(String.valueOf(currentItem.getTeam2Score()));
            itemHolder.timestampTextView.setText(currentItem.getTimestamp());
            itemHolder.timerSetTextView.setText(currentItem.getTimerSet());
        }
    }

    @Override
    public int getItemCount() {
        return gameRecords.size() + 1; // Add 1 for the header
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ITEM;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class GameRecordViewHolder extends RecyclerView.ViewHolder {
        public TextView team1NameTextView;
        public TextView team2NameTextView;
        public TextView team1ScoreTextView;
        public TextView team2ScoreTextView;
        public TextView timestampTextView;
        public TextView timerSetTextView;

        public GameRecordViewHolder(View itemView) {
            super(itemView);
            team1NameTextView = itemView.findViewById(R.id.team1NameTextView);
            team2NameTextView = itemView.findViewById(R.id.team2NameTextView);
            team1ScoreTextView = itemView.findViewById(R.id.team1ScoreTextView);
            team2ScoreTextView = itemView.findViewById(R.id.team2ScoreTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            timerSetTextView = itemView.findViewById(R.id.timerSetTextView);
        }
    }
}