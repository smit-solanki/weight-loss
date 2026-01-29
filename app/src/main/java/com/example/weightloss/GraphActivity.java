package com.example.weightloss;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    private LineChart weightChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        weightChart = findViewById(R.id.weightChart);

        setupChart();
    }

    private void setupChart() {
        SharedPreferences prefs = getSharedPreferences("UserHealthData", MODE_PRIVATE);
        float currentWeight = prefs.getFloat("last_weight", 0f);

        // In a real app, you'd save a List of weights.
        // For now, let's create a sample list of data points (Day, Weight)
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, currentWeight + 2)); // Day 1
        entries.add(new Entry(2, currentWeight + 1)); // Day 2
        entries.add(new Entry(3, currentWeight));     // Today

        LineDataSet dataSet = new LineDataSet(entries, "Weight (kg)");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        weightChart.setData(lineData);

        // Refresh the chart
        weightChart.invalidate();

        // Optional Styling
        weightChart.getDescription().setEnabled(false);
        weightChart.getAxisRight().setEnabled(false); // Hide right Y-axis
        weightChart.getXAxis().setGranularity(1f);    // Only show whole numbers on X-axis
    }
}