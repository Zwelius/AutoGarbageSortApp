package com.example.autogarbagesortapp.ui.home;

public class MaintenanceLog {
    public String timestamp;
    public String note;

    public MaintenanceLog() {}  // Required for Firebase

    public MaintenanceLog(String timestamp, String note) {
        this.timestamp = timestamp;
        this.note = note;
    }
}
