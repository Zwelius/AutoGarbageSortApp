package com.example.autogarbagesortapp.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
 import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autogarbagesortapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    private ProgressBar mPlasticProgressBar, mMetalProgressBar, mPaperProgressBar;
    private TextView mPlasticPercentage, mMetalPercentage, mPaperPercentage;
    private TextView mPlasticLastUpdated, mMetalLastUpdated, mPaperLastUpdated;
    private ImageView mPlasticWarning, mMetalWarning, mPaperWarning;
    private final DatabaseReference binsRef = FirebaseDatabase.getInstance("https://autogarbagesortapp-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("bins");
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    private LineChart plasticChart, metalChart, paperChart;
    private RadioGroup timeRangeRadioGroup;
    private BarChart weeklyBarChart;
    private RecyclerView maintenanceRecyclerView;
    private MaintenanceAdapter maintenanceAdapter;
    private List<MaintenanceLog> maintenanceLogs = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // Initialize UI elements
        mPlasticProgressBar = view.findViewById(R.id.plasticProgressBar);
        mMetalProgressBar = view.findViewById(R.id.metalProgressBar);
        mPaperProgressBar = view.findViewById(R.id.paperProgressBar);

        mPlasticPercentage = view.findViewById(R.id.plasticPercentageTextView);
        mMetalPercentage = view.findViewById(R.id.metalPercentageTextView);
        mPaperPercentage = view.findViewById(R.id.paperPercentageTextView);

        mPlasticLastUpdated = view.findViewById(R.id.plasticLastUpdatedTextView);
        mMetalLastUpdated = view.findViewById(R.id.metalLastUpdatedTextView);
        mPaperLastUpdated = view.findViewById(R.id.paperLastUpdatedTextView);

        mPlasticWarning = view.findViewById(R.id.plasticWarningIcon);
        mMetalWarning = view.findViewById(R.id.metalWarningIcon);
        mPaperWarning = view.findViewById(R.id.paperWarningIcon);

        // Set up listeners
        fetchBinData("plastic", mPlasticProgressBar, mPlasticPercentage, mPlasticLastUpdated, mPlasticWarning);
        fetchBinData("metal", mMetalProgressBar, mMetalPercentage, mMetalLastUpdated, mMetalWarning);
        fetchBinData("paper", mPaperProgressBar, mPaperPercentage, mPaperLastUpdated, mPaperWarning);

        plasticChart = view.findViewById(R.id.plasticLineChart);
        metalChart = view.findViewById(R.id.metalLineChart);
        paperChart = view.findViewById(R.id.paperLineChart);

        fetchChartData("plastic", plasticChart);
        fetchChartData("metal", metalChart);
        fetchChartData("paper", paperChart);

        timeRangeRadioGroup = view.findViewById(R.id.timeRangeRadioGroup);
        weeklyBarChart = view.findViewById(R.id.weeklyBarChart);
        fetchAverageFillStats(7); // Show weekly by default

        timeRangeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.weeklyRadioButton) {
                fetchAverageFillStats(7);
            } else if (checkedId == R.id.monthlyRadioButton) {
                fetchAverageFillStats(30);
            }
        });

        view.findViewById(R.id.emptyPlasticButton).setOnClickListener(v -> {
            recordMaintenance("plastic");
        });
        view.findViewById(R.id.emptyPaperButton).setOnClickListener(v -> {
            recordMaintenance("paper");
        });
        view.findViewById(R.id.emptyMetalButton).setOnClickListener(v -> {
            recordMaintenance("metal");
        });

        maintenanceRecyclerView = view.findViewById(R.id.maintenanceRecyclerView);
        maintenanceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        maintenanceAdapter = new MaintenanceAdapter(maintenanceLogs);
        maintenanceRecyclerView.setAdapter(maintenanceAdapter);

        fetchMaintenanceHistory("plastic");
        fetchMaintenanceHistory("paper");
        fetchMaintenanceHistory("metal");

        return view;
    }

    private void fetchBinData(String binKey, ProgressBar progressBar, TextView percentText, TextView lastUpdatedText, ImageView warningIcon) {
        binsRef.child(binKey).child("level").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int level = 0;
                if (snapshot.exists()) {
                    Object value = snapshot.getValue();
                    if (value instanceof Long) {
                        level = ((Long) value).intValue();
                    } else if (value instanceof Double) {
                        level = (int) Math.round((Double) value);
                    } else if (value instanceof Integer) {
                        level = (Integer) value;
                    }
                }

                updateBinUI(binKey, level, progressBar, percentText, lastUpdatedText, warningIcon);
            }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            // Optional: log or show error
        }
    });
}

    private void updateBinUI(String binKey, int level, ProgressBar progressBar, TextView percentText, TextView lastUpdatedText, ImageView warningIcon) {
        if (level < 50) {
            progressBar.getProgressDrawable().setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (level < 80) {
            progressBar.getProgressDrawable().setColorFilter(Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            progressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        percentText.setText(level + "%");

        if (level >= 80) {
            sendOverflowNotification(binKey, level);
        }

        if (level >= 90) {
            warningIcon.setVisibility(View.VISIBLE);
            percentText.setTextColor(Color.RED);
        } else {
            warningIcon.setVisibility(View.GONE);
            percentText.setTextColor(Color.BLACK);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a - MMM dd, yyyy", Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
        String timestamp = sdf.format(new Date());
        lastUpdatedText.setText("Last Updated: " + timestamp);
    }
    private void sendOverflowNotification(String binType, int level) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionRationale();
            } else {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "bin_alerts")
                .setSmallIcon(R.drawable.notif_bell)
                .setContentTitle("Bin Overflow Alert")
                .setContentText(binType + " bin is " + level + "% full.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM) // This helps heads-up behavior
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
                .setAutoCancel(true); // Dismiss when tapped

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(binType.hashCode(), builder.build());
    }



    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted – show a confirmation notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "bin_alerts")
                        .setSmallIcon(R.drawable.notif_bell)
                        .setContentTitle("Permission Granted")
                        .setContentText("Notifications have been enabled.")
                        .setPriority(NotificationCompat.PRIORITY_LOW);

                NotificationManagerCompat.from(requireContext()).notify(1002, builder.build());
            } else {
                // Permission denied – show a denied notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "bin_alerts")
                        .setSmallIcon(R.drawable.notif_bell)
                        .setContentTitle("Permission Denied")
                        .setContentText("Notifications remain disabled.")
                        .setPriority(NotificationCompat.PRIORITY_LOW);

                NotificationManagerCompat.from(requireContext()).notify(1003, builder.build());
            }
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Notification Permission Required")
                .setMessage("To receive bin overflow alerts, please allow notification permissions.")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
                    }
                })
                .setNegativeButton("Deny", null)
                .create()
                .show();
    }
    private void fetchChartData(String material, LineChart chart) {
        DatabaseReference logRef = FirebaseDatabase.getInstance("https://autogarbagesortapp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("logs");

        logRef.orderByKey().limitToLast(100)
                .addValueEventListener(new ValueEventListener() {
                    long lastUpdate = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long now = System.currentTimeMillis();
                if (now - lastUpdate < 10000) return; // throttle to 1 update every 5 seconds
                lastUpdate = now;
                List<Entry> entries = new ArrayList<>();

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey(); // e.g. "2025-04-29"
                    DataSnapshot materialSnapshot = dateSnapshot.child(material);

                    for (DataSnapshot timeSnapshot : materialSnapshot.getChildren()) {
                        String timeKey = timeSnapshot.getKey(); // e.g. "16:51:43"
                        Long value = timeSnapshot.getValue(Long.class);

                        if (value != null) {
                            String fullDateTime = dateKey + " " + timeKey;
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
                                Date date = sdf.parse(fullDateTime);
                                if (date != null) {
                                    long timestamp = date.getTime();
                                    entries.add(new Entry(timestamp, value));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                if (entries.isEmpty()) {
                    Log.d("ChartData", "No data available for " + material);
                    return;
                }

                LineDataSet dataSet = new LineDataSet(entries, material + " levels");
                dataSet.setColor(Color.BLUE);
                dataSet.setDrawCircles(true);
                dataSet.setDrawValues(false);

                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);

                // Format X-axis
                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f); // only show 1 label per "step"
                //xAxis.setLabelRotationAngle(-45f); // rotate for readability
                xAxis.setLabelCount(4, true); // force fewer labels

                xAxis.setValueFormatter(new ValueFormatter() {
                    private final SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

                    @Override
                    public String getFormattedValue(float value) {
                        return format.format(new Date((long) value));
                    }
                });

                //chart.setVisibleXRangeMaximum(10);
                chart.getAxisRight().setEnabled(false); // hide right axis
                chart.getDescription().setEnabled(false); // remove description
                chart.getLegend().setTextSize(12f); // resize legend if needed
                chart.setExtraBottomOffset(10f); // prevent label cutoff
                chart.getDescription().setText(material + " Bin Fill Levels Over Time");
                chart.invalidate();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChartData", "Error fetching " + material + " data: " + error.getMessage());
            }
        });
    }

    private void fetchAverageFillStats(int daysBack) {
        DatabaseReference logRef = FirebaseDatabase.getInstance("https://autogarbagesortapp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("logs");

        logRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> materialTotal = new HashMap<>();
                Map<String, Integer> materialCount = new HashMap<>();
                String[] materials = {"plastic", "metal", "paper"};

                for (String mat : materials) {
                    materialTotal.put(mat, 0);
                    materialCount.put(mat, 0);
                }

                long now = System.currentTimeMillis();
                long cutoff = now - (daysBack * 24L * 60 * 60 * 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    try {
                        Date date = sdf.parse(dateSnapshot.getKey());
                        if (date != null && date.getTime() >= cutoff) {
                            for (String material : materials) {
                                DataSnapshot matSnap = dateSnapshot.child(material);
                                for (DataSnapshot timeSnap : matSnap.getChildren()) {
                                    Long value = timeSnap.getValue(Long.class);
                                    if (value != null) {
                                        materialTotal.put(material, materialTotal.get(material) + value.intValue());
                                        materialCount.put(material, materialCount.get(material) + 1);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                List<BarEntry> entries = new ArrayList<>();
                final List<String> labels = new ArrayList<>();
                int index = 0;

                for (String mat : materials) {
                    int total = materialTotal.get(mat);
                    int count = materialCount.get(mat);
                    float avg = count > 0 ? (float) total / count : 0;
                    entries.add(new BarEntry(index, avg));
                    labels.add(mat.substring(0, 1).toUpperCase() + mat.substring(1));
                    index++;
                }

                BarDataSet dataSet = new BarDataSet(entries, "Average Fill % (Last " + daysBack + " Days)");
                dataSet.setColors(new int[]{Color.BLUE, Color.GRAY, Color.GREEN});
                dataSet.setValueTextSize(12f);

                BarData data = new BarData(dataSet);
                data.setBarWidth(0.9f);

                weeklyBarChart.setData(data);
                weeklyBarChart.setFitBars(true);
                weeklyBarChart.getDescription().setEnabled(false);
                weeklyBarChart.getAxisRight().setEnabled(false);
                weeklyBarChart.getXAxis().setDrawGridLines(false);
                weeklyBarChart.getAxisLeft().setAxisMinimum(0);
                weeklyBarChart.getAxisLeft().setAxisMaximum(100);
                weeklyBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                weeklyBarChart.getXAxis().setGranularity(1f);
                weeklyBarChart.getXAxis().setLabelCount(labels.size());
                weeklyBarChart.getXAxis().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if (value >= 0 && value < labels.size()) {
                            return labels.get((int) value);
                        } else {
                            return "";
                        }
                    }
                });

                weeklyBarChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("BarChart", "Error: " + error.getMessage());
            }
        });
    }

    private void recordMaintenance(String binType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");

                        DatabaseReference maintenanceRef = FirebaseDatabase.getInstance("https://autogarbagesortapp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .getReference("maintenance");

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
        String timestamp = sdf.format(new Date());

                        Map<String, String> log = new HashMap<>();
                        log.put("bin", binType);
                        log.put("emptiedBy", fullName.trim());
                        log.put("timestamp", timestamp);

        maintenanceRef.child(timestamp).setValue(log)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), binType + " bin logged as emptied.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to log maintenance.", Toast.LENGTH_SHORT).show());
                }
    });
}

    private void fetchMaintenanceHistory(String binType) {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://autogarbagesortapp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("maintenance");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                maintenanceLogs.clear();
                for (DataSnapshot entry : snapshot.getChildren()) {
                    String bin = entry.child("bin").getValue(String.class);
                    String emptiedBy = entry.child("emptiedBy").getValue(String.class);
                    String timestamp = entry.child("timestamp").getValue(String.class);

                    if (bin != null && emptiedBy != null && timestamp != null) {
                        String note = bin + " bin emptied by " + emptiedBy;
                        maintenanceLogs.add(0, new MaintenanceLog(timestamp, note));
                    }
                }
                maintenanceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle errors
            }
        });

    }


}
