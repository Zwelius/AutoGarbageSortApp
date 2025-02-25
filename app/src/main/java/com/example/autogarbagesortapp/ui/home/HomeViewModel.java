package com.example.autogarbagesortapp.ui.home;

import android.os.AsyncTask;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeViewModel extends ViewModel {

    private static final String ARDUINO_URL = "http://YOUR_ARDUINO_IP_ADDRESS/"; // Replace with your Arduino's IP address

    private MutableLiveData<Integer> plasticLevel = new MutableLiveData<>();
    private MutableLiveData<Integer> metalLevel = new MutableLiveData<>();
    private MutableLiveData<Integer> biodegradableLevel = new MutableLiveData<>();

    public LiveData<Integer> getPlasticLevel() {
        return plasticLevel;
    }

    public LiveData<Integer> getMetalLevel() {
        return metalLevel;
    }

    public LiveData<Integer> getBiodegradableLevel() {
        return biodegradableLevel;
    }

    public void fetchData() {
        new FetchDataTask().execute();
    }

    private class FetchDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(ARDUINO_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                connection.disconnect();

                return result.toString();
            } catch (IOException e) {
                Log.e("HomeViewModel", "Network error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    Log.d("HomeViewModel", "Received data: " + result);
                    JSONObject jsonObject = new JSONObject(result);
                    int plastic = jsonObject.getInt("plastic");
                    int metal = jsonObject.getInt("metal");
                    int biodegradable = jsonObject.getInt("biodegradable");

                    plasticLevel.setValue(plastic);
                    metalLevel.setValue(metal);
                    biodegradableLevel.setValue(biodegradable);
                } catch (JSONException e) {
                    Log.e("HomeViewModel", "JSON parsing error", e);
                }
            } else {
                Log.e("HomeViewModel", "No data received from Arduino");
            }
        }
    }
}
