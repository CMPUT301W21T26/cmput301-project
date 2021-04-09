package com.example.crowdtest.ui;

import android.content.Intent;
import android.icu.util.Measure;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.crowdtest.ExperimentManager;
import com.example.crowdtest.R;
import com.example.crowdtest.experiments.Binomial;
import com.example.crowdtest.experiments.Count;
import com.example.crowdtest.experiments.Measurement;
import com.example.crowdtest.experiments.NonNegative;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Value input experiment activity class. Value input experiments are experiments that require a number for their trials. Double for measurement trials and positive int for Non negative trials
 */
public class ValueInputActivity extends ExperimentActivity {
    private Button addButton;
    private EditText valueEditText;
    private Button detailsButton;
    private ImageButton participantsButton;
    private ParticipantsHelper participantsHelper;
    private ExperimentManager experimentManager = new ExperimentManager();


    /**
     * To determine whether the experiment is a measurement experiment or a non negative integer experiment
     */
    boolean isMeasurement;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle experimentBundle = getIntent().getExtras();
        if (experimentBundle.getSerializable("experiment") instanceof Measurement) {
            experiment = (Measurement) experimentBundle.getSerializable("experiment");
            isMeasurement = true;
        } else {
            experiment = (NonNegative) experimentBundle.getSerializable("experiment");
            isMeasurement = false;
        }

        currentUser = (String) getIntent().getStringExtra("username");

        setContentView(R.layout.activity_value_input);
        setValues();

        addButton = findViewById(R.id.value_input_add_button);
        valueEditText = findViewById(R.id.value_input_trial_input_editText);

        // Allows user to end an experiment if they are the owner
        endExperiment = findViewById(R.id.experiment_end_experiment_button);
        if (experiment.getOwner().equals(currentUser)) {
            endExperiment.setVisibility(View.VISIBLE);
            endExperiment.setOnClickListener(v -> {
                if (endExperiment.getText().equals("End Experiment")) {
                    experiment.setStatus("closed");
                    endExperiment.setText("Reopen Experiment");
                    addButton.setVisibility(View.INVISIBLE);
                    valueEditText.setVisibility(View.INVISIBLE);
                    toolbar.setTitleTextColor(0xFFE91E63);
                    toolbar.setTitle(experiment.getTitle() + " (Closed)");
                } else if (endExperiment.getText().equals("Reopen Experiment")) {
                    experiment.setStatus("open");
                    endExperiment.setText("End Experiment");
                    addButton.setVisibility(View.VISIBLE);
                    valueEditText.setVisibility(View.VISIBLE);
                    toolbar.setTitleTextColor(0xFF000000);
                    toolbar.setTitle(experiment.getTitle());
                }
            });
        } else {
            endExperiment.setVisibility(View.INVISIBLE);
        }

//        if (experiment.getSubscribers().contains(currentUser) && !experiment.getBlackListedUsers().contains(currentUser)) {
        if (experiment.getSubscribers().contains(currentUser)) {
            if (experiment.isGeolocationEnabled()) {
                addButton.setOnClickListener(v -> {
                    if (isMeasurement) {
                        double trialInput = Double.parseDouble(valueEditText.getText().toString());
                        ((Measurement) experiment).addTrial(trialInput, currentLocation);
                        valueEditText.setText("");
                    } else {
                        int trialInput = Integer.parseInt(valueEditText.getText().toString());
                        valueEditText.setText("");
                        try {
                            ((NonNegative) experiment).addTrial(trialInput, currentLocation);
                            Snackbar.make(v, "Trial added successfully", Snackbar.LENGTH_SHORT);
                        } catch (Exception e) {
                            Snackbar.make(v, "Please enter a non negative integer", Snackbar.LENGTH_SHORT);
                        }
                    }
                });
            } else {
                addButton.setOnClickListener(v -> {
                    if (isMeasurement) {
                        double trialInput = Double.parseDouble(valueEditText.getText().toString());
                        ((Measurement) experiment).addTrial(trialInput, null);
                        valueEditText.setText("");
                    } else {
                        int trialInput = Integer.parseInt(valueEditText.getText().toString());
                        valueEditText.setText("");
                        try {
                            ((NonNegative) experiment).addTrial(trialInput, null);
                            Snackbar.make(v, "Trial added successfully", Snackbar.LENGTH_SHORT);
                        } catch (Exception e) {
                            Snackbar.make(v, "Please enter a non negative integer", Snackbar.LENGTH_SHORT);
                        }
                    }
                });
            }
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("Experiments").document(experiment.getExperimentID()).collection("trials");
        collectionReference.addSnapshotListener((value, error) -> {
            if (experiment.getStatus().toLowerCase().equals("closed")) {
                endExperiment.setText("Reopen Experiment");
                addButton.setVisibility(View.INVISIBLE);
                valueEditText.setVisibility(View.INVISIBLE);
                toolbar.setTitleTextColor(0xFFE91E63);
                toolbar.setTitle(experiment.getTitle() + " (Closed)");
            } else {
                endExperiment.setText("End Experiment");
//                if (experiment.getSubscribers().contains(currentUser) && !experiment.getBlackListedUsers().contains(currentUser)) {
                if (experiment.getSubscribers().contains(currentUser)) {
                    addButton.setVisibility(View.VISIBLE);
                    valueEditText.setVisibility(View.VISIBLE);
                } else {
                    addButton.setVisibility(View.INVISIBLE);
                    valueEditText.setVisibility(View.INVISIBLE);
                }
                toolbar.setTitleTextColor(0xFF000000);
                toolbar.setTitle(experiment.getTitle());
            }
        });

        detailsButton = findViewById(R.id.experiment_details_button);

        detailsButton.setOnClickListener(view -> {

            Intent intent = new Intent(view.getContext(), ExpStatisticsActivity.class);

            intent.putExtra("EXP", experiment);

            startActivity(intent);

        });

        participantsButton = findViewById(R.id.exp_value_participants_button);

        participantsButton.setOnClickListener(view -> {

            participantsHelper = new ParticipantsHelper(this, experimentManager, experiment, currentUser);
            participantsHelper.displayParticipantList("Participants","Back");
        });
    }
}
