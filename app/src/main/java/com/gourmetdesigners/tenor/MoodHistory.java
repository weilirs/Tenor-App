package com.gourmetdesigners.tenor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public class MoodHistory extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    public void onResume(){
        super.onResume();
        bottomNav.setSelectedItemId(R.id.mood_history_menu_item);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);
        ArrayList<Entry> recent_moods = new ArrayList<>();
        String jsonFilePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/data.json";
        File jsonFile = new File(jsonFilePath);
        final ArrayList<String> xAxisStrings = new ArrayList<>();
        final ArrayList<String> xAxisEpochs = new ArrayList<>();
        final ArrayList<String> yAxisStrings = new ArrayList<>();
        int num_entries = 0;
        float average_mood = 0;

        if (jsonFile.exists()) {
            // Read the json file to get all the mood entries
            int file_len = (int) jsonFile.length();
            byte[] bytes = new byte[file_len];
            FileInputStream input_stream = null;
            try {
                input_stream = new FileInputStream(jsonFile);
            } catch (FileNotFoundException fl) {
                fl.printStackTrace();
            }
            try {
                try {
                    input_stream.read(bytes);
                } catch (IOException io) {
                    io.printStackTrace();
                }
            } finally {
                try {
                    input_stream.close();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }

            String contents = new String(bytes);
            JSONObject content_object;
            Map<String, Integer> timestamp_emoji_map = new TreeMap<>(Collections.reverseOrder());

            try {
                content_object = new JSONObject(contents);
                JSONObject capture_history = (JSONObject) content_object.get("map");
                JSONArray keys = capture_history.names();
                for (int i = 0; i < keys.length(); i++) {
                    String timestamp = keys.getString(i);
                    String jsonString = capture_history.getString(timestamp);
                    JSONObject timestamp_entry = new JSONObject(jsonString);
                    int emoji = Integer.parseInt((String) timestamp_entry.get("emojiRating"));
                    // We now have the timestamp and the emoji
                    timestamp_emoji_map.put(timestamp, emoji);
                }
                // Get the recent mood entries
                ArrayList<Integer> most_recent_entries = new ArrayList<>();
                ArrayList<String> most_recent_entries_keys = new ArrayList<>();
                Iterator tree_map_it = timestamp_emoji_map.entrySet().iterator();
                int counter = 0;

                while(tree_map_it.hasNext() && counter < 7) {
                    Map.Entry curr_entry = (Map.Entry)tree_map_it.next();
                    most_recent_entries.add((Integer) curr_entry.getValue());
                    most_recent_entries_keys.add((String) curr_entry.getKey());
                    counter++;
                }

                counter = 1;

                xAxisStrings.add("");
                xAxisEpochs.add("");
                int mood_sum = 0;
                for (int i = most_recent_entries.size() - 1; i >= 0; i--) {
                    String timestamp = most_recent_entries_keys.get(i);
                    long epoch = Long.parseLong(timestamp);
                    Date date = new Date(epoch*1000);
                    String day = new SimpleDateFormat("dd").format(date);
                    String month = new SimpleDateFormat("MMM").format(date);
                    int current_mood = most_recent_entries.get(i);
                    int final_mood = current_mood + 3; // This is needed to convert from the -2 to 2 scale to 1 to 5 scale.
                    recent_moods.add(new Entry(counter, final_mood));
                    if (i <= 4) // Only sums last 5 mood scores (only calculates the average of 5 recent moods)
                        mood_sum += final_mood;
                    xAxisStrings.add(month + " " + day);
                    xAxisEpochs.add(timestamp);
                    counter++;
                }
                num_entries = most_recent_entries.size();

                average_mood = mood_sum / 5;
                TextView moodSuggestionTextView = findViewById(R.id.moodSuggestionText);
                if(average_mood > 4.5) {
                    moodSuggestionTextView.setText("Looks like you've been doing great! Keep up the good streak!");
                }
                else if (average_mood > 3.5) {
                    moodSuggestionTextView.setText("You have been doing good recently. Keep doing what made you happy!");
                }
                else if (average_mood > 2.5) {
                    moodSuggestionTextView.setText("Looks like you've been doing okay. You might want to reflect on what made you sad recently.");
                }
                else if (average_mood > 1.5){
                    moodSuggestionTextView.setText("Looks like you have been sad. Why don't you go for a movie tonight with your friends?");
                }
                else if (average_mood >= 1) {
                    moodSuggestionTextView.setText("Looks like you've been feeling awful recently. Why don't you go for a walk at the park to clear your head?");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        // Plotting line chart for recent mood history
        LineChart chart = findViewById(R.id.mood_line_chart);

        chart.getDescription().setEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        LineDataSet mood_dataset = new LineDataSet(recent_moods, "Your Mood Changes");
        mood_dataset.setLineWidth(2f);
        mood_dataset.setFillAlpha(120);
        mood_dataset.setDrawValues(false);
        mood_dataset.setCircleRadius(5.5f);
        mood_dataset.setCircleHoleRadius(2.5f);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(mood_dataset);
        LineData final_data = new LineData(dataSets);
        chart.setData(final_data);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xAxisStrings));
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);


        chart.getLegend().setEnabled(false);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);

        yAxisStrings.add("");
        yAxisStrings.add("Awful");
        yAxisStrings.add("Bad");
        yAxisStrings.add("Okay");
        yAxisStrings.add("Good");
        yAxisStrings.add("Great");

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setLabelCount(6, true);
        leftAxis.setAxisMaximum(5);
        leftAxis.setAxisMinimum(0);
        leftAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(yAxisStrings));
        leftAxis.setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        xAxis.setAxisMinimum(num_entries - 7);
        xAxis.setAxisMaximum(num_entries + 0.5f);

        if(recent_moods.size() < 5) {
            xAxis.setLabelCount(xAxisStrings.size());
        }
        else{
            xAxis.setLabelCount(xAxisStrings.size()-1);
        }

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight)
            {
                float x_coordinate;
                x_coordinate = entry.getX();
                String timestamp = xAxisEpochs.get((int) x_coordinate);
                Intent intent = new Intent(MoodHistory.this, SeeHighlight.class);
                Bundle seeHighlightBundle = new Bundle();
                seeHighlightBundle.putString("key", timestamp);
                intent.putExtras(seeHighlightBundle);
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {

            }

        });

        LinearLayout moodSummaryLL = findViewById(R.id.moodSummary);
        TextView averageMoodText = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = 30;
        averageMoodText.setText(String.format("Recent Average Mood:"));
        averageMoodText.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        averageMoodText.setLayoutParams(lp);
        moodSummaryLL.addView(averageMoodText);

        ImageView averageMoodEmoji = new ImageView(this);

        if (average_mood > 4.5) {
            averageMoodEmoji.setImageResource(R.drawable.great);
        } else if (average_mood > 3.5)
            averageMoodEmoji.setImageResource(R.drawable.good);
        else if (average_mood > 2.5)
            averageMoodEmoji.setImageResource(R.drawable.okay);
        else if (average_mood > 1.5)
            averageMoodEmoji.setImageResource(R.drawable.bad);
        else if (average_mood >= 1)
            averageMoodEmoji.setImageResource(R.drawable.awful);

        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        averageMoodEmoji.setLayoutParams(lp);
        moodSummaryLL.addView(averageMoodEmoji);

        if (average_mood == 0) {
            // If there has been no mood history recorded by the user, show that there is no data available.
            TextView noDataAvailableText = new TextView(this);
            averageMoodText.setText(String.format("No data available."));
            noDataAvailableText.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            noDataAvailableText.setLayoutParams(lp);
            moodSummaryLL.addView(noDataAvailableText);
        }

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.mood_history_menu_item);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.home_menu_item:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.mood_history_menu_item:
                        return true;
                    case R.id.past_moments_menu_item:
                        startActivity(new Intent(getApplicationContext(), PastMoments.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.highlights_menu_item:
                        startActivity(new Intent(getApplicationContext(), Highlights.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }
}