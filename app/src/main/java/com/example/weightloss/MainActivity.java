package com.example.weightloss;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etAge, etWeight, etHeight;
    private RadioGroup rgGender;
    private Button btnCalculate, btnExercise, btnGraph;
    private TextView tvResult;

    private static final String PREFS_NAME = "UserHealthData";
    private static final String KEY_WEIGHT = "last_weight";
    private static final String KEY_HEIGHT = "last_height";
    private static final String KEY_AGE = "last_age";
    private static final String KEY_DATE = "last_update_date";
    private static final String KEY_RESULT = "last_bmi_result"; // New Key

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

        // This now loads the inputs AND the result text
        loadSavedData();

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
}