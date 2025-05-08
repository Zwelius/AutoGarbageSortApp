package com.example.autogarbagesortapp.ui.gallery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.autogarbagesortapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private ImageView imageView;
    private TextView emailTextView;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText jobEditText;
    private Button updateButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        imageView = root.findViewById(R.id.imageView);
        emailTextView = root.findViewById(R.id.emailTextView);
        firstNameEditText = root.findViewById(R.id.firstNameEditText);
        lastNameEditText = root.findViewById(R.id.lastNameEditText);
        jobEditText = root.findViewById(R.id.jobEditText);
        updateButton = root.findViewById(R.id.updateButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {
            loadUserData(user.getUid());
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData();
            }
        });

        return root;
    }

    private void loadUserData(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String email = documentSnapshot.getString("email");
                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    String job = documentSnapshot.getString("job");

                    emailTextView.setText(email);
                    firstNameEditText.setText(firstName);
                    lastNameEditText.setText(lastName);
                    jobEditText.setText(job);
                } else {
                    // Handle the case where the document doesn't exist
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the error
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                Log.e("GalleryFragment", "Error loading user data", e);
            }
        });
    }

    private void updateUserData() {
        String userId = user.getUid();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String job = jobEditText.getText().toString();

        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("job", job);

        db.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "User data updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to update user data", Toast.LENGTH_SHORT).show();
                        Log.e("GalleryFragment", "Error updating user data", e);
                    }
                });
    }
}