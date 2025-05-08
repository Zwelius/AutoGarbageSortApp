package com.example.autogarbagesortapp.ui.slideshow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autogarbagesortapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class GameHistoryFragment extends Fragment {

    private RecyclerView gameHistoryRecyclerView;
    private GameHistoryAdapter gameHistoryAdapter;
    private ArrayList<GameRecord> gameHistory;

    // Firestore
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public GameHistoryFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_game_history, container, false);

        gameHistoryRecyclerView = root.findViewById(R.id.gameHistoryRecyclerView);
        gameHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        gameHistory = new ArrayList<>();
        gameHistoryAdapter = new GameHistoryAdapter(gameHistory);
        gameHistoryRecyclerView.setAdapter(gameHistoryAdapter);

        loadGameHistory();

        return root;
    }

    private void loadGameHistory() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            CollectionReference gameHistoryRef = db.collection("users").document(userId).collection("gameHistory");

            gameHistoryRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        gameHistory.clear(); // Clear existing data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            GameRecord gameRecord = document.toObject(GameRecord.class);
                            gameHistory.add(gameRecord);
                        }
                        gameHistoryAdapter.notifyDataSetChanged(); // Update the RecyclerView
                    } else {
                        Log.e("GameHistoryFragment", "Error getting game history", task.getException());
                        Toast.makeText(getContext(), "Failed to load game history", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}