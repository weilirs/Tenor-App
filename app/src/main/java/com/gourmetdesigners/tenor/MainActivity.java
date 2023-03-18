package com.gourmetdesigners.tenor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.Button;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.scales.Linear;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    public void onResume(){
        super.onResume();
        bottomNav.setSelectedItemId(R.id.home_menu_item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Forces light mode.
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        Button captureMomentBtn = (Button)findViewById(R.id.captureMomentButton);
        captureMomentBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CaptureMoment.class));
            }
        });

        LinearLayout layout = findViewById(R.id.mainLayout);
        AnimationDrawable animatedBg = (AnimationDrawable)layout.getBackground();
        animatedBg.setEnterFadeDuration(2500);
        animatedBg.setExitFadeDuration(2000);
        animatedBg.start();

        // Customize the home screen based on user's mood history and the highlight picture availability
        // (1) If the user's recent mood history has been bad, show a nudge to view their mood history page
        // (2) Else if the user has more than nine images, ask them to check their picture highlight
        setNudges();

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.home_menu_item);
        bottomNav.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.home_menu_item:
                        return true;
                    case R.id.mood_history_menu_item:
                        startActivity(new Intent(getApplicationContext(), MoodHistory.class));
                        overridePendingTransition(0,0);
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

    @SuppressLint("ResourceAsColor")
    private void setNudges() {
        // Customize the home screen based on user's mood history and the highlight picture availability
        // (1) If the user's recent mood history has been bad, show a nudge to view their mood history page
        // (2) Else if the user has more than nine images, ask them to check their picture highlight
        ArrayList<Entry> recent_moods = new ArrayList<>();
        String jsonFilePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/data.json";
        File jsonFile = new File(jsonFilePath);
        final ArrayList<String> xAxisStrings = new ArrayList<>();
        final ArrayList<String> yAxisStrings = new ArrayList<>();
        float average_mood = 0;
        int key_count = 0;
        String imagePath = null;

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
                    if(imagePath == null) {
                        imagePath = (String) timestamp_entry.get("imagePath");
                    }
                    // We now have the timestamp and the emoji
                    timestamp_emoji_map.put(timestamp, emoji);
                }
                key_count = keys.length();
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
                    counter++;
                }
                average_mood = mood_sum / 5;

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if(key_count >= 5) {
            TextView text = findViewById(R.id.nudgeMoodText);
            if(average_mood <= 2.5) {
                // If mood has been bad, ask them to check the mood history page
                ImageView img = findViewById(R.id.nudgeMoodImage);
                if (average_mood > 1.5) {
                    img.setImageResource(R.drawable.bad);
                } else if (average_mood >= 1) {
                    img.setImageResource(R.drawable.awful);
                }
            text.setText("Check out your mood history to get insights into your recent mood drop.");
            }
            if(average_mood > 3.5) {
//                text.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.text_border));
                // If mood has been bad, ask them to check the mood history page
                ImageView img = findViewById(R.id.nudgeMoodImage);
                if (average_mood > 4.5) {
                    img.setImageResource(R.drawable.great);
                } else if (average_mood > 3.5) {
                    img.setImageResource(R.drawable.good);
                }
                text.setText("Recent trends show that you've been going well. Keep it up!");
            }
        }
        if(key_count > 9) {
            // Nudge them to check the highlight page.
            //TextView text = findViewById(R.id.nudgeHighlightsText);
            //text.setText("Looks like you have lots of memories! Check out a collage of your memorable pictures on the picture highlights tab!");
            //ImageView img = findViewById(R.id.nudgeHighlightsImage);

            BitmapFactory.Options options = new BitmapFactory.Options();
            BitmapFactory.decodeFile(imagePath, options);
            int imageSize = Math.max(options.outHeight, options.outWidth);
            BitmapFactory.Options scaled_options = new BitmapFactory.Options();
            scaled_options.inSampleSize = imageSize / 20;
            //Bitmap thumbnail = BitmapFactory.decodeFile(imagePath, scaled_options);
            //img.setImageBitmap(thumbnail);
        }
    }
}

