package com.gourmetdesigners.tenor;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SeeHighlight extends AppCompatActivity  {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_highlight);
//        getSupportActionBar().setTitle("Capture Details");

        try {
            File directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File file =  directory.listFiles()[0];
            FileInputStream stream = new FileInputStream(file);
            String capturesJson = null;
            try {
                FileChannel fileChannel = stream.getChannel();
                MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                capturesJson = Charset.defaultCharset().decode(byteBuffer).toString();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                stream.close();
            }

            JSONObject jsonObj = new JSONObject(capturesJson);
            JSONObject data  = jsonObj.getJSONObject("map");

            // Obtained intent from the activity from which this activity started
            Bundle intentBundle = getIntent().getExtras();
            String epochTime = intentBundle.getString("key");

            JSONObject target = data.getJSONObject(epochTime);
            String text = target.getString("journalEntry");
            String emoji = target.getString("emojiRating");
            String imagePath = target.getString("imagePath");

            emojiDisplay(emoji);
            imageDisplay(imagePath);
            textDisplay(text);

            long unix_seconds = Long.parseLong(epochTime);
            Date date = new Date(unix_seconds * 1000L);

            // Setting current date display below journal text entry to current date
//            String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
            String currentDate = new SimpleDateFormat("MMM dd, E, hh:mm a").format(date);
            getSupportActionBar().setTitle(String.format("Entry of %s", currentDate));
//            timeDisplay(currentDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        bottomNav = findViewById(R.id.seeHighlightsBottomNavigationView);
        //bottomNav.setSelectedItemId(R.id.highlights_menu_item);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.home_menu_item:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0,0);
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
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> {
            finish();
        });
    }

    private void imageDisplay(String path){
        String imageID = "picture";
        int resourceID = getResources().getIdentifier(imageID,"id", getPackageName());
        ImageView image = findViewById(resourceID);
        Bitmap imgBitmap = BitmapFactory.decodeFile(path);
        image.setImageBitmap(imgBitmap);
    }

    private void textDisplay(String path){
        String textID = "text";
        int resourceID = getResources().getIdentifier(textID,"id", getPackageName());
        TextView txt = findViewById(resourceID);
        txt.setText(path);
    }

    private void timeDisplay(String path){
        String textID = "time";
        int resourceID  = getResources().getIdentifier(textID,"id", getPackageName());
        TextView time = findViewById(resourceID);
        time.setText(path);
    }

    private void emojiDisplay(String rate){
        String imageID = "emoji";
        int resourceID = getResources().getIdentifier(imageID,"id", getPackageName());
        ImageView image = findViewById(resourceID);
        switch (rate){
            case "-2":
                image.setImageResource(R.drawable.awful);
                break;
            case "-1":
                image.setImageResource(R.drawable.bad);
                break;
            case "0":
                image.setImageResource(R.drawable.okay);
                break;
            case "1":
                image.setImageResource(R.drawable.good);
                break;
            case "2":
                image.setImageResource(R.drawable.great);
                break;
        }
    }

}