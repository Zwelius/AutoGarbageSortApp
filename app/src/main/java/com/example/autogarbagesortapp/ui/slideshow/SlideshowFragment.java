package com.example.autogarbagesortapp.ui.slideshow;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autogarbagesortapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SlideshowFragment extends Fragment {

    private EditText team1NameEditText;
    private EditText team2NameEditText;
    private TextView team1ScoreTextView;
    private TextView team2ScoreTextView;
    private TextView timerTextView;
    private EditText timerSetEditText;
    private Button startGameButton;
    private LinearLayout scoringCategoriesLayout;
    private TextView category1TextView;
    private TextView category2TextView;
    private TextView category3TextView;
    private Button historyButton;
    private RecyclerView historyRecyclerView;
    private GameHistoryAdapter historyAdapter;

    private CountDownTimer countDownTimer;
    private long timeLeftInMilliseconds; // Time left in milliseconds
    private long initialTimeLeftInMilliseconds; // Store the initial time
    private boolean timerRunning;
    private int team1Score = 0;
    private int team2Score = 0;
    private boolean isTeam1Turn = false;
    private boolean isTeam2Turn = false;
    private boolean gameReady = false;
    private boolean team1TurnFinished = false;
    private boolean team2TurnFinished = false;

    // Scoring categories and points
    private int category1Points = 1;
    private int category2Points = 2;
    private int category3Points = 3;

    // Firestore
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Arduino
    private static final int SERVER_PORT = 8080; // Choose a port
    private ExecutorService executorService;
    private ServerSocket serverSocket;
    private Handler mainThreadHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newFixedThreadPool(1);
        mainThreadHandler = new Handler(Looper.getMainLooper());
        startServer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopServer();
    }

    private void startServer() {
        executorService.submit(() -> {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                while (!Thread.currentThread().isInterrupted()) {
                    Socket client = serverSocket.accept();
                    handleClient(client);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleClient(Socket client) {
        executorService.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Process the received data
                    processData(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void processData(String data) {
        // Example: Assuming data is in the format "team1,1" or "team2,2"
        String[] parts = data.split(",");
        if (parts.length == 2) {
            String team = parts[0];
            int points = Integer.parseInt(parts[1]);

            mainThreadHandler.post(() -> {
                // Update UI on the main thread
                if (team.equalsIgnoreCase("team1")) {
                    scoreTeam1(points);
                } else if (team.equalsIgnoreCase("team2")) {
                    scoreTeam2(points);
                }
            });
        }
    }

    private void stopServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdownNow();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        team1NameEditText = root.findViewById(R.id.team1NameEditText);
        team2NameEditText = root.findViewById(R.id.team2NameEditText);
        team1ScoreTextView = root.findViewById(R.id.team1ScoreTextView);
        team2ScoreTextView = root.findViewById(R.id.team2ScoreTextView);
        timerTextView = root.findViewById(R.id.timerTextView);
        timerSetEditText = root.findViewById(R.id.timerSetEditText);
        startGameButton = root.findViewById(R.id.startGameButton);
        scoringCategoriesLayout = root.findViewById(R.id.scoringCategoriesLayout);
        category1TextView = root.findViewById(R.id.category1TextView);
        category2TextView = root.findViewById(R.id.category2TextView);
        category3TextView = root.findViewById(R.id.category3TextView);
        historyButton = root.findViewById(R.id.historyButton);
        historyRecyclerView = root.findViewById(R.id.historyRecyclerView);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set initial state
        setInitialState();

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameReady) {
                    readyGame();
                } else {
                    stopGame();
                }
            }
        });

        team1ScoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameReady && !isTeam2Turn && !team1TurnFinished) {
                    startTeam1Turn();
                }
            }
        });

        team2ScoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameReady && !isTeam1Turn && !team2TurnFinished) {
                    startTeam2Turn();
                }
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGameHistory();
            }
        });

        return root;
    }

    private void setInitialState() {
        startGameButton.setText("Ready Game");
        gameReady = false;
        team1ScoreTextView.setEnabled(false);
        team2ScoreTextView.setEnabled(false);
        isTeam1Turn = false;
        isTeam2Turn = false;
        team1Score = 0;
        team2Score = 0;
        team1ScoreTextView.setText("0");
        team2ScoreTextView.setText("0");
        timerTextView.setText("00:00");
        team1TurnFinished = false;
        team2TurnFinished = false;
    }

    private void readyGame() {
        String timerInput = timerSetEditText.getText().toString();
        if (timerInput.isEmpty()) {
            Toast.makeText(getContext(), "Please set the timer", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long timerDuration = Long.parseLong(timerInput);
            if (timerDuration <= 0) {
                Toast.makeText(getContext(), "Please enter a valid timer duration", Toast.LENGTH_SHORT).show();
                return;
            }
            initialTimeLeftInMilliseconds = timerDuration * 1000; // Convert seconds to milliseconds
            timeLeftInMilliseconds = initialTimeLeftInMilliseconds;
            updateTimer(); // Update the timer display immediately
            startGameButton.setText("Stop Game");
            gameReady = true;
            team1ScoreTextView.setEnabled(true);
            team2ScoreTextView.setEnabled(true);
            team1TurnFinished = false;
            team2TurnFinished = false;
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid timer format", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopGame() {
        setInitialState();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void startTeam1Turn() {
        isTeam1Turn = true;
        isTeam2Turn = false;
        team2ScoreTextView.setEnabled(false);
        team1ScoreTextView.setEnabled(true);
        timeLeftInMilliseconds = initialTimeLeftInMilliseconds; // Reset timer
        updateTimer();
        startTimer();
    }

    private void startTeam2Turn() {
        isTeam2Turn = true;
        isTeam1Turn = false;
        team1ScoreTextView.setEnabled(false);
        team2ScoreTextView.setEnabled(true);
        timeLeftInMilliseconds = initialTimeLeftInMilliseconds; // Reset timer
        updateTimer();
        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMilliseconds = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                Toast.makeText(getContext(), "Time's up!", Toast.LENGTH_SHORT).show();
                if (isTeam1Turn) {
                    team1TurnFinished = true;
                    team1ScoreTextView.setEnabled(false);
                    if (!team2TurnFinished) {
                        team2ScoreTextView.setEnabled(true);
                    }
                } else {
                    team2TurnFinished = true;
                    team2ScoreTextView.setEnabled(false);
                    if (!team1TurnFinished) {
                        team1ScoreTextView.setEnabled(true);
                    }
                }
                isTeam1Turn = false;
                isTeam2Turn = false;
                if (team1TurnFinished && team2TurnFinished && gameReady) {
                    addGameToHistory();
                    stopGame();
                }
            }
        }.start();

        timerRunning = true;
    }

    private void updateTimer() {
        int minutes = (int) (timeLeftInMilliseconds / 1000) / 60;
        int seconds = (int) (timeLeftInMilliseconds / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timeLeftFormatted);
    }

    public void scoreTeam1(int points) {
        if (isTeam1Turn && timerRunning) {
            team1Score += points;
            team1ScoreTextView.setText(String.valueOf(team1Score));
        }
    }

    public void scoreTeam2(int points) {
        if (isTeam2Turn && timerRunning) {
            team2Score += points;
            team2ScoreTextView.setText(String.valueOf(team2Score));
        }
    }

    private void addGameToHistory() {
        Log.d("SlideshowFragment", "addGameToHistory() called");
        String team1Name = team1NameEditText.getText().toString().isEmpty() ? "Team 1" : team1NameEditText.getText().toString();
        String team2Name = team2NameEditText.getText().toString().isEmpty() ? "Team 2" : team2NameEditText.getText().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());

        // Create a new game record
        Map<String, Object> gameRecord = new HashMap<>();
        gameRecord.put("team1Name", team1Name);
        gameRecord.put("team2Name", team2Name);
        gameRecord.put("team1Score", team1Score);
        gameRecord.put("team2Score", team2Score);
        gameRecord.put("timestamp", timestamp);
        gameRecord.put("timerSet", timerSetEditText.getText().toString());
        Log.d("SlideshowFragment", "gameRecord: " + gameRecord.toString());

        // Add the game record to Firestore
        db.collection("gameHistory")
                .add(gameRecord)
                .addOnSuccessListener(documentReference -> {
                    Log.d("SlideshowFragment", "Game record added with ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Game added to history", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w("SlideshowFragment", "Error adding game record", e);
                    Toast.makeText(getContext(), "Error adding game to history", Toast.LENGTH_SHORT).show();
                });
    }

    private void showGameHistory() {
        Log.d("SlideshowFragment", "showGameHistory() called");
        TextView emptyHistoryTextView = getView().findViewById(R.id.emptyHistoryTextView);
        db.collection("gameHistory")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d("SlideshowFragment", "onSuccess() called");
                        List<GameRecord> gameRecords = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d("SlideshowFragment", "Document ID: " + document.getId());
                            Log.d("SlideshowFragment", "Document Data: " + document.getData().toString());
                            GameRecord gameRecord = new GameRecord(
                                    document.getString("team1Name"),
                                    document.getLong("team1Score").intValue(),
                                    document.getString("team2Name"),
                                    document.getLong("team2Score").intValue(),
                                    document.getString("timestamp"),
                                    document.getString("timerSet")
                            );
                            gameRecords.add(gameRecord);
                        }
                        if (gameRecords.isEmpty()) {
                            emptyHistoryTextView.setVisibility(View.VISIBLE);
                            historyRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyHistoryTextView.setVisibility(View.GONE);
                            historyRecyclerView.setVisibility(View.VISIBLE);

                            if (historyAdapter == null) {
                                Log.d("SlideshowFragment", "historyAdapter is null, creating new adapter");
                                historyAdapter = new GameHistoryAdapter(gameRecords);
                                historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                historyRecyclerView.setAdapter(historyAdapter);
                            } else {
                                Log.d("SlideshowFragment", "historyAdapter is not null, updating data");
                                historyAdapter.setGameRecords(gameRecords);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("SlideshowFragment", "Error getting game history", e);
                        Toast.makeText(getContext(), "Error getting game history", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}