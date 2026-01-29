package com.example.weightloss;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etAge, etWeight, etHeight;
    private RadioGroup rgGender;
    private Button btnCalculate, btnExercise, btnGraph;
    private MaterialButton btnAddSmall, btnAddLarge;
    private TextView tvResult;
    private static final String PREFS_NAME = "UserHealthData";
    private static final String KEY_WEIGHT = "last_weight";
    private static final String KEY_HEIGHT = "last_height";
    private static final String KEY_AGE = "last_age";
    private static final String KEY_DATE = "last_update_date";
    private static final String KEY_RESULT = "last_bmi_result";
    //Water
    private ProgressBar waterProgressBar;
    private TextView tvWaterProgress;
    private int currentWater = 0;
    private int dailyGoal = 2500; // Default 2.5L

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        rgGender = findViewById(R.id.rgGender);
        btnCalculate = findViewById(R.id.btnCalculate);
        tvResult = findViewById(R.id.tvResult);
        btnExercise = findViewById(R.id.btnExercise);
        btnGraph = findViewById(R.id.btnGraph);
        waterProgressBar = findViewById(R.id.waterProgressBar);
        tvWaterProgress = findViewById(R.id.tvWaterProgress);
        btnAddSmall = findViewById(R.id.btnAddSmall);
        btnAddLarge = findViewById(R.id.btnAddLarge);

        // This now loads the inputs AND the result text
        loadSavedData();
        checkMidnightReset();

        btnAddSmall.setOnClickListener(v -> {
            currentWater += 250;
            updateWaterUI();
        });

        btnAddLarge.setOnClickListener(v -> {
            currentWater += 500;
            updateWaterUI();
        });

        btnCalculate.setOnClickListener(v -> {
            processHealthData();
        });

        btnExercise.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);
            startActivity(intent);
        });

        btnGraph.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GraphActivity.class);
            startActivity(intent);
        });

        // Load saved goal and current intake
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        dailyGoal = prefs.getInt("water_goal", 2500);
        currentWater = prefs.getInt("water_current", 0);
        updateWaterUI();

        // CLICK LISTENER ON THE PROGRESS BAR
        waterProgressBar.setOnClickListener(v -> showWaterMenu());
    }

    private void processHealthData() {
        if (isAlreadyUpdatedToday()) {
            Toast.makeText(this, "Warning: Update tomorrow!", Toast.LENGTH_LONG).show();
            return;
        }

        String ageStr = etAge.getText().toString();
        String weightStr = etWeight.getText().toString();
        String heightStr = etHeight.getText().toString();

        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);
        float weight = Float.parseFloat(weightStr);
        float heightCm = Float.parseFloat(heightStr);
        float heightM = heightCm / 100;

        float bmi = weight / (heightM * heightM);

        int selectedId = rgGender.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        String gender = (radioButton != null) ? radioButton.getText().toString() : "Male";

        double bmr;
        if (gender.equalsIgnoreCase("Male")) {
            bmr = (10 * weight) + (6.25 * heightCm) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25 * heightCm) - (5 * age) - 161;
        }

        String status;
        if (bmi < 18.5) status = "Underweight";
        else if (bmi < 25) status = "Healthy";
        else if (bmi < 30) status = "Overweight";
        else status = "Obese";

        String resultDisplay = String.format(Locale.getDefault(),
                "Gender: %s\nBMI: %.1f (%s)\nDaily Calorie Goal: %.0f kcal",
                gender, bmi, status, bmr);

        tvResult.setText(resultDisplay);

        // Now we save the result string too
        saveData(weight, heightCm, age, resultDisplay, bmr);
    }

    private boolean isAlreadyUpdatedToday() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = prefs.getString(KEY_DATE, "");
        return today.equals(lastDate);
    }

    private void saveData(float weight, float height, int age, String resultText, double bmr) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        editor.putFloat(KEY_WEIGHT, weight);
        editor.putFloat(KEY_HEIGHT, height);
        editor.putInt(KEY_AGE, age);
        editor.putString(KEY_DATE, today);
        editor.putString(KEY_RESULT, resultText);
        editor.putFloat("last_bmr", (float) bmr);
        editor.apply();

        Toast.makeText(this, "Data Saved Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void loadSavedData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        float weight = prefs.getFloat(KEY_WEIGHT, -1);
        float height = prefs.getFloat(KEY_HEIGHT, -1);
        int age = prefs.getInt(KEY_AGE, -1);
        String savedResult = prefs.getString(KEY_RESULT, "Enter details to see results");

        if (weight != -1) etWeight.setText(String.valueOf(weight));
        if (height != -1) etHeight.setText(String.valueOf(height));
        if (age != -1) etAge.setText(String.valueOf(age));

        // This line ensures the BMI result stays visible
        tvResult.setText(savedResult);
    }

    private void showWaterMenu() {
        String[] options = {"Add 250ml Glass", "Edit Daily Goal", "Reset Today"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Water Options");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // Add Glass
                currentWater += 250;
                saveWaterData();
            } else if (which == 1) { // Edit Goal
                showEditGoalDialog();
            } else if (which == 2) { // Reset
                currentWater = 0;
                saveWaterData();
            }
        });
        builder.show();
    }

    private void showEditGoalDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter goal in Liters (e.g. 3.0)");

        new AlertDialog.Builder(this)
                .setTitle("Set Daily Goal")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        float liters = Float.parseFloat(val);
                        dailyGoal = (int) (liters * 1000); // Convert L to ml
                        saveWaterData();
                    }
                }).show();
    }

    private void saveWaterData() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt("water_current", currentWater);
        editor.putInt("water_goal", dailyGoal);
        editor.apply();
        updateWaterUI();
    }

    private void updateWaterUI() {
        // 1. Set the Max (Goal)
        waterProgressBar.setMax(dailyGoal);

        // 2. Calculate the animation
        // We animate from the "old" progress (where the bar is now)
        // to the "new" currentWater value
        int previousProgress = waterProgressBar.getProgress();

        ObjectAnimator animation = ObjectAnimator.ofInt(
                waterProgressBar,
                "progress",
                previousProgress,
                currentWater
        );

        // 3. Set the "Extraordinary" Feel
        animation.setDuration(800); // 800 milliseconds for a smooth flow
        animation.setInterpolator(new DecelerateInterpolator()); // Starts fast, ends smooth

        // 4. Update the text while animating (Optional but looks great)
        animation.addUpdateListener(animator -> {
            int animatedValue = (int) animator.getAnimatedValue();
            tvWaterProgress.setText(animatedValue + " ml");
        });

        animation.start();
    }

    private void checkMidnightReset() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get today's date as a String (e.g., "2026-01-29")
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Get the last date water was recorded
        String lastResetDate = prefs.getString("last_water_reset_date", "");

        // If today is NOT the same as the last recorded date, it's a new day!
        if (!todayDate.equals(lastResetDate)) {
            currentWater = 0; // Reset count

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("water_current", 0);
            editor.putString("last_water_reset_date", todayDate); // Save today as the new last reset date
            editor.apply();

            updateWaterUI();
        }
    }
}