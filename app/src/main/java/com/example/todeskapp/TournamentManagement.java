package com.example.todeskapp;

import java.util.*;

import android.content.Intent;

import android.os.Bundle;

import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import com.example.todeskapp.databinding.ConfigureTournmentBinding;
import com.example.todeskapp.databinding.CurrentBracketSacPdfBinding;
import com.example.todeskapp.databinding.TournamentManagementBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;



public class TournamentManagement extends AppCompatActivity {

    private TournamentManagementBinding binding;
    private FirebaseFirestore db;
    private String accessCode;


    /**
     * Displays Tournament Management xml layout.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TournamentManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        accessCode = getIntent().getStringExtra("ACCESS_CODE");
        initializePlayerListCollection();
        /**
         * a button function that is used to redirect from Tournament Management to Configure Tournament.
         */
        binding.configureTournamentSettings.setOnClickListener(v -> {
            accessCode = getIntent().getStringExtra("ACCESS_CODE");
            Intent intent = new Intent(TournamentManagement.this, ConfigureTournament.class);
            intent.putExtra("ACCESS_CODE", accessCode);
            startActivity(intent);
        });

        /**
         * a button function that is used to redirect from Tournament Management to Update Tournament.
         */
        binding.updateTournamentBracket.setOnClickListener(v -> {
            accessCode = getIntent().getStringExtra("ACCESS_CODE");

            bracketStyleRedirect(accessCode);

        });

    }

    private void initializePlayerListCollection() {

        CollectionReference playerListRef = db.collection("AccessCodes").document(accessCode).collection("PlayerList");

        playerListRef.limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {

                    HashMap<String, Object> initDoc = new HashMap<>();
                    initDoc.put("Init", true);

                    playerListRef.document("InitialDoc").set(initDoc)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "PlayerList collection initialized successfully.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error initializing PlayerList collection: " + e.getMessage(), Toast.LENGTH_LONG).show());
                } else {
                    // Documents already exist, no need to add the initial document
                    Toast.makeText(getApplicationContext(), "PlayerList already contains data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Failed to retrieve documents
                Toast.makeText(getApplicationContext(), "Failed to check PlayerList collection: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void bracketStyleRedirect(String accessCode) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the specific document reference
        db.collection("AccessCodes").document(accessCode)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {

                            Long bracketStyleLong = document.getLong("BracketStyle");


                            if (bracketStyleLong != null) {
                                int bracketStyle = bracketStyleLong.intValue();
                                switch (bracketStyle) {
                                    case 0:

                                        swissPlayerValidation();
                                        break;
                                    case 1:
                                        performActionBasedOnBracketStyle1();
                                        break;

                                    default:
                                        Toast.makeText(this, "Unhandled bracket style: " + bracketStyle, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "BracketStyle field is missing.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "No such document!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch document: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void swissPlayerValidation() {
        db.collection("AccessCodes").document(accessCode)
                .collection("PlayerList")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            int playerCount = task.getResult().size();

                            if (playerCount == 8 || playerCount == 16) {

                                Intent intent = new Intent(TournamentManagement.this, Swiss.class);
                                intent.putExtra("ACCESS_CODE", accessCode);
                                startActivity(intent);

                            } else {

                                Toast.makeText(TournamentManagement.this, "Swiss Pool MUST HAVE 8 or 16 players", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(TournamentManagement.this, "Failed to fetch players: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void performActionBasedOnBracketStyle1() {
        db.collection("AccessCodes").document(accessCode)
                .collection("PlayerList")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(TournamentManagement.this, CurrentBracket_SAC_PDF.class);
                            intent.putExtra("ACCESS_CODE", accessCode);
                            startActivity(intent);
                        } else {
                            Toast.makeText(TournamentManagement.this, "Failed to fetch players: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }


                });

    }
}



