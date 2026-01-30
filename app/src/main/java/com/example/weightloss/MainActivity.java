package com.example.weightloss;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

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

    private static final String KEY_WATER_CURRENT = "water_current";
    private static final String KEY_WATER_GOAL = "water_goal";
    private static final String KEY_WATER_RESET_DATE = "last_water_reset_date";

    private ProgressBar waterProgressBar;
    private TextView tvWaterProgress;
    private int currentWater = 0;
    private int dailyGoal = 2500;

    // Step Counter Variables
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private TextView tvSteps;
    private boolean isSensorPresent = false;
    private int stepsAtStart = 0;
    private static final String KEY_STEPS_BOOT = "steps_at_boot";

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
        tvSteps = findViewById(R.id.tvSteps);

        // Request Activity Permission for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 100);
            }
        }

        // Initialize Sensor Manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorPresent = true;
        } else {
            tvSteps.setText("Sensor not found");
            isSensorPresent = false;
        }

        loadSavedData();
        checkMidnightReset();

        btnAddSmall.setOnClickListener(v -> { currentWater += 250; saveWaterData(); });
        btnAddLarge.setOnClickListener(v -> { currentWater += 500; saveWaterData(); });
        btnCalculate.setOnClickListener(v -> processHealthData());
        btnExercise.setOnClickListener(v -> startActivity(new Intent(this, ExerciseActivity.class)));
        btnGraph.setOnClickListener(v -> startActivity(new Intent(this, GraphActivity.class)));
        waterProgressBar.setOnClickListener(v -> showWaterMenu());
    }

    // SENSOR METHODS (Corrected)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (stepsAtStart < 0) {
                stepsAtStart = (int) event.values[0];
            }
            int currentSteps = (int) event.values[0] - stepsAtStart;
            tvSteps.setText(String.valueOf(currentSteps));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorPresent) {
            sensorManager.unregisterListener(this);
        }
    }

    // WATER LOGIC
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
            Toast.makeText(this, "Already updated today!", Toast.LENGTH_SHORT).show();
            return;
        }

        String ageStr = etAge.getText().toString();
        String weightStr = etWeight.getText().toString();
        String heightStr = etHeight.getText().toString();

        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);
        float weight = Float.parseFloat(weightStr);
        float heightCm = Float.parseFloat(heightStr);
        float heightM = heightCm / 100;
        float bmi = weight / (heightM * heightM);

        int selectedId = rgGender.getCheckedRadioButtonId();
        RadioButton rb = findViewById(selectedId);
        String gender = (rb != null) ? rb.getText().toString() : "Male";

        double bmr = gender.equalsIgnoreCase("Male")
                ? (10 * weight) + (6.25 * heightCm) - (5 * age) + 5
                : (10 * weight) + (6.25 * heightCm) - (5 * age) - 161;

        String status = (bmi < 18.5) ? "Underweight" : (bmi < 25) ? "Healthy" : (bmi < 30) ? "Overweight" : "Obese";
        String res = String.format(Locale.getDefault(), "BMI: %.1f (%s)\nBase Burn: %.0f kcal", bmi, status, bmr);

        tvResult.setText(res);
        saveData(weight, heightCm, age, res);
    }

    private boolean isAlreadyUpdatedToday() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return today.equals(getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_DATE, ""));
    }

    private void saveData(float weight, float height, int age, String res) {
        SharedPreferences.Editor ed = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        ed.putFloat(KEY_WEIGHT, weight);
        ed.putFloat(KEY_HEIGHT, height);
        ed.putInt(KEY_AGE, age);
        ed.putString(KEY_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        ed.putString(KEY_RESULT, res);
        ed.apply();
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
    }
}