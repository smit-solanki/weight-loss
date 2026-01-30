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

    // ONE Preference Name for everything
    private static final String PREFS_NAME = "UserHealthData";

    // Input Keys
    private static final String KEY_WEIGHT = "last_weight";
    private static final String KEY_HEIGHT = "last_height";
    private static final String KEY_AGE = "last_age";
    private static final String KEY_DATE = "last_update_date";
    private static final String KEY_RESULT = "last_bmi_result";

    // Water Keys (Consolidated)
    private static final String KEY_WATER_CURRENT = "water_current";
    private static final String KEY_WATER_GOAL = "water_goal";
    private static final String KEY_WATER_RESET_DATE = "last_water_reset_date";

    private ProgressBar waterProgressBar;
    private TextView tvWaterProgress;
    private int currentWater = 0;
    private int dailyGoal = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
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

        // 1. Load Data
        loadSavedData();
        checkMidnightReset(); // This loads currentWater and dailyGoal

        // 2. Button Listeners
        btnAddSmall.setOnClickListener(v -> {
            currentWater += 250;
            saveWaterData();
        });

        btnAddLarge.setOnClickListener(v -> {
            currentWater += 500;
            saveWaterData();
        });

        btnCalculate.setOnClickListener(v -> processHealthData());

        btnExercise.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ExerciseActivity.class));
        });

        btnGraph.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, GraphActivity.class));
        });

        waterProgressBar.setOnClickListener(v -> showWaterMenu());
    }

    private void updateWaterUI() {
        waterProgressBar.setMax(dailyGoal);
        int previousProgress = waterProgressBar.getProgress();

        ObjectAnimator animation = ObjectAnimator.ofInt(waterProgressBar, "progress", previousProgress, currentWater);
        animation.setDuration(800);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addUpdateListener(animator -> {
            int animatedValue = (int) animator.getAnimatedValue();
            tvWaterProgress.setText(animatedValue + " ml");
        });
        animation.start();
    }

    private void saveWaterData() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(KEY_WATER_CURRENT, currentWater);
        editor.putInt(KEY_WATER_GOAL, dailyGoal);
        editor.apply();
        updateWaterUI();
    }

    private void checkMidnightReset() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastResetDate = prefs.getString(KEY_WATER_RESET_DATE, "");

        dailyGoal = prefs.getInt(KEY_WATER_GOAL, 2500);

        if (!todayDate.equals(lastResetDate)) {
            currentWater = 0;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_WATER_CURRENT, 0);
            editor.putString(KEY_WATER_RESET_DATE, todayDate);
            editor.apply();
        } else {
            currentWater = prefs.getInt(KEY_WATER_CURRENT, 0);
        }
        updateWaterUI();
    }

    // ... (Keep your processHealthData and loadSavedData methods, just ensure they use PREFS_NAME) ...

    private void loadSavedData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        float weight = prefs.getFloat(KEY_WEIGHT, -1);
        float height = prefs.getFloat(KEY_HEIGHT, -1);
        int age = prefs.getInt(KEY_AGE, -1);
        String savedResult = prefs.getString(KEY_RESULT, "Enter details to see results");

        if (weight != -1) etWeight.setText(String.valueOf(weight));
        if (height != -1) etHeight.setText(String.valueOf(height));
        if (age != -1) etAge.setText(String.valueOf(age));
        tvResult.setText(savedResult);
    }

    private void showWaterMenu() {
        String[] options = {"Add 250ml Glass", "Edit Daily Goal", "Reset Today"};
        new AlertDialog.Builder(this)
                .setTitle("Water Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { currentWater += 250; saveWaterData(); }
                    else if (which == 1) showEditGoalDialog();
                    else if (which == 2) { currentWater = 0; saveWaterData(); }
                }).show();
    }

    private void showEditGoalDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter goal in Liters (e.g. 3.0)");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(this)
                .setTitle("Set Daily Goal")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        dailyGoal = (int) (Float.parseFloat(val) * 1000);
                        saveWaterData();
                    }
                }).show();
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

        double bmr = gender.equalsIgnoreCase("Male")
                ? (10 * weight) + (6.25 * heightCm) - (5 * age) + 5
                : (10 * weight) + (6.25 * heightCm) - (5 * age) - 161;

        String status = (bmi < 18.5) ? "Underweight" : (bmi < 25) ? "Healthy" : (bmi < 30) ? "Overweight" : "Obese";
        String resultDisplay = String.format(Locale.getDefault(), "Gender: %s\nBMI: %.1f (%s)\nDaily Calorie Goal: %.0f kcal", gender, bmi, status, bmr);

        tvResult.setText(resultDisplay);
        saveData(weight, heightCm, age, resultDisplay, bmr);
    }

    private boolean isAlreadyUpdatedToday() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return today.equals(prefs.getString(KEY_DATE, ""));
    }

    private void saveData(float weight, float height, int age, String resultText, double bmr) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat(KEY_WEIGHT, weight);
        editor.putFloat(KEY_HEIGHT, height);
        editor.putInt(KEY_AGE, age);
        editor.putString(KEY_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        editor.putString(KEY_RESULT, resultText);
        editor.apply();
        Toast.makeText(this, "Data Saved Successfully!", Toast.LENGTH_SHORT).show();
    }
}