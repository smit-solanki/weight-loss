package com.example.weightloss;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class ExerciseActivity extends AppCompatActivity {

    private TextView tvTargetInfo, tvWalkDuration, tvRunDuration, tvCycleDuration,
            tvSwimDuration, tvRopeDuration, tvYogaDuration,
            tvFootballDuration, tvBurpeeDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // Link All IDs
        tvTargetInfo = findViewById(R.id.tvTargetInfo);
        tvWalkDuration = findViewById(R.id.tvWalkDuration);
        tvRunDuration = findViewById(R.id.tvRunDuration);
        tvCycleDuration = findViewById(R.id.tvCycleDuration);
        //tvSwimDuration = findViewById(R.id.tvSwimDuration);
        tvRopeDuration = findViewById(R.id.tvRopeDuration);
        tvYogaDuration = findViewById(R.id.tvYogaDuration);
        //tvFootballDuration = findViewById(R.id.tvFootballDuration);
        tvBurpeeDuration = findViewById(R.id.tvBurpeeDuration);

        calculateExercises();
    }

    private void calculateExercises() {
        SharedPreferences prefs = getSharedPreferences("UserHealthData", MODE_PRIVATE);
        float weight = prefs.getFloat("last_weight", 70.0f);

        // Target: Let's burn 400 kcal today
        float targetKcal = 400.0f;
        tvTargetInfo.setText(String.format(Locale.getDefault(), "Today's Exercise Target: %.0f kcal", targetKcal));

        // MET Values for calculation
        tvWalkDuration.setText(calculateMins(targetKcal, 3.5, weight) + " mins");
        tvRunDuration.setText(calculateMins(targetKcal, 10.0, weight) + " mins");
        tvCycleDuration.setText(calculateMins(targetKcal, 8.0, weight) + " mins");
        //tvSwimDuration.setText(calculateMins(targetKcal, 7.0, weight) + " mins");
        tvRopeDuration.setText(calculateMins(targetKcal, 12.0, weight) + " mins"); // High Intensity
        tvYogaDuration.setText(calculateMins(targetKcal, 2.5, weight) + " mins"); // Low Intensity
        //tvFootballDuration.setText(calculateMins(targetKcal, 9.0, weight) + " mins"); // High Intensity
        tvBurpeeDuration.setText(calculateMins(targetKcal, 8.5, weight) + " mins"); // Intense Bodyweight
    }

    private String calculateMins(float kcal, double met, float weight) {
        // Formula: Duration (mins) = (Kcal * 200) / (MET * 3.5 * weight)
        double minutes = (kcal * 200) / (met * 3.5 * weight);
        return String.valueOf((int) Math.ceil(minutes));
    }
}