package com.gourmetdesigners.tenor;


import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import static java.lang.Math.min;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


public class Highlights extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    BottomNavigationView bottomNav;
    JSONObject data;
    JSONArray keys;
    List<Integer> happy;
    List<Integer> sad;
    List<Integer> pastMonth;
    List<Integer> pastYear;
    List<Integer> allData;
    String[] filters = {"Randomized", "Happy Moments", "Sad Moments", "Past Month", "Past Year"};

    @Override
    public void onResume(){
        super.onResume();
        bottomNav.setSelectedItemId(R.id.highlights_menu_item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        switch(filters[position]) {
            case "Randomized":
                randomFunc(allData, keys, data);
                break;
            case "Happy Moments":
                happyFunc(happy,keys,data);
                break;
            case "Sad Moments":
                sadFunc(sad, keys, data);
                break;
            case "Past Month":
                pastMonthFunc(pastMonth, keys, data);
                break;
            case "Past Year":
                pastYearFunc(pastYear, keys, data);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highlights);
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, filters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        try {
            File directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File file  =  directory.listFiles()[0];
            FileInputStream stream = new FileInputStream(file);
            String capturesJson = null;
            try {
                FileChannel fileChannel = stream.getChannel();
                MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                capturesJson = Charset.defaultCharset().decode(byteBuffer).toString();
                JSONObject jsonObj = new JSONObject(capturesJson);
                data  = jsonObj.getJSONObject("map");
                keys = data.names();
            }
            catch(Exception e){
                Toast.makeText(getApplicationContext(), "You do not have enough pictures to generate a highlight!", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            finally {
                stream.close();
            }

            if(keys.length() < 9) {
                // If there are less than 9 captures, we cannot generate a highlight
                Toast.makeText(getApplicationContext(), "You do not have enough pictures to generate a highlight!", Toast.LENGTH_LONG).show();
                this.finish();
            }

            happy = new ArrayList<Integer>();
            sad = new ArrayList<Integer>();
            pastMonth = new ArrayList<Integer>();
            pastYear = new ArrayList<Integer>();
            allData = new ArrayList<Integer>();

            Long epochLong = System.currentTimeMillis() / 1000L;

            Long monthCutoff = epochLong - 2629746;
            Long yearCutoff = epochLong - 31536000;

            List<Integer> indicesList = new ArrayList<Integer>();
            for (int i = 0; i < keys.length(); i++) {
                indicesList.add(i);
            }
            Collections.shuffle(indicesList);
            HashSet<Integer> selectedIndices = new HashSet<>();
            for (int i = 0; i < keys.length() && i < 9; i++) {
                selectedIndices.add(indicesList.get(i));
            }

            int cardCounter = 0;
            // looping through All nodes
            for (int i = 0; i < keys.length(); i++) {
                String key = keys.getString(i);
                JSONObject inner =  data.getJSONObject(keys.getString(i));
                String emoji = inner.getString("emojiRating");
                if(emoji.equals("-1") || emoji.equals("-2")){
                    sad.add(i);
                }
                if(emoji.equals("1") || emoji.equals("2")){
                    happy.add(i);
                }
                if(Long.parseLong(key) >= monthCutoff) {
                    pastMonth.add(i);
                }
                if(Long.parseLong(key) >= yearCutoff) {
                    pastYear.add(i);
                }
                allData.add(i);
                String imagePath = inner.getString("imagePath");

                if(selectedIndices.contains(i)) {
                    cardClick(cardCounter + 1, key);
                    emojiDisplay(emoji, cardCounter + 1);
                    imageDisplay(imagePath, cardCounter + 1);
                    cardCounter = cardCounter + 1;
                }
            }
            //happyFunc(happy,keys,data);
            //sadFunc(sad,keys,data);
            //pastMonthFunc(pastMonth,keys,data);
            //pastYearFunc(pastYear,keys,data);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "You do not have enough pictures to generate a highlight!", Toast.LENGTH_LONG).show();
            this.finish();
            e.printStackTrace();
        }

        bottomNav = findViewById(R.id.highlightsBottomNavigationView);
        bottomNav.setSelectedItemId(R.id.highlights_menu_item);
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
                    return true;
            }
            return false;
        });
    }

    private void imageDisplay(String path, int i){
            String imageID = "highlight" + i;
            int resourceID = getResources().getIdentifier(imageID,"id", getPackageName());
            ImageView img = findViewById(resourceID);
            Bitmap imgBitmap = BitmapFactory.decodeFile(path);
            img.setImageBitmap(Bitmap.createScaledBitmap(imgBitmap, 120, 120, false));
            imgBitmap.recycle();
    }

    private void emojiDisplay(String rate, int i){
        String imageID = "emoji" + i;
        int resourceID = getResources().getIdentifier(imageID,"id", getPackageName());
        ImageView img = findViewById(resourceID);
        switch (rate){
            case "-2":
                img.setImageResource(R.drawable.awful);
                break;
            case "-1":
                img.setImageResource(R.drawable.bad);
                break;
            case "0":
                img.setImageResource(R.drawable.okay);
                break;
            case "1":
                img.setImageResource(R.drawable.good);
                break;
            case "2":
                img.setImageResource(R.drawable.great);
                break;
        }

    }

    private void cardClick(int cardNumber, String key){
        String cardID = "card" + cardNumber;
        int resourceID = getResources().getIdentifier(cardID,"id", getPackageName());
        CardView card = findViewById(resourceID);
        card.setOnClickListener(v -> {
            Intent intent = new Intent(Highlights.this, SeeHighlight.class);
            Bundle seeHighlightBundle = new Bundle();
            seeHighlightBundle.putString("key", key);
            intent.putExtras(seeHighlightBundle);
            startActivity(intent);
        });
    }

    private void happyFunc(List<Integer> happy, JSONArray keys, JSONObject data){
        //Button happyBut = (Button)findViewById(R.id.happyButton);

        //happyBut.setOnClickListener(v -> {
            System.out.println("click");
            if(happy.size() < 9){
                Toast.makeText(getApplicationContext(), "You don't have enough happy moments to generate a highlight!", Toast.LENGTH_LONG).show();
            }
            else{
                for(int i=0;i<9;i++){
                    try {
                        String key = keys.getString(happy.get(i));
                        JSONObject inner =  data.getJSONObject(keys.getString(happy.get(i)));
                        String emoji = inner.getString("emojiRating");
                        String imagePath = inner.getString("imagePath");
                        cardClick(i + 1, key);
                        emojiDisplay(emoji, i + 1);
                        imageDisplay(imagePath, i + 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        //});
    }

    private void sadFunc(List<Integer> sad, JSONArray keys, JSONObject data){
        //Button sadBut = (Button)findViewById(R.id.sadButton);

        //sadBut.setOnClickListener(v -> {
            if(sad.size() < 9){
                Toast.makeText(getApplicationContext(), "You don't have enough sad moments to generate a highlight!", Toast.LENGTH_LONG).show();
            }
            else{
                for(int i=0;i<9;i++){
                    try {
                        String key = keys.getString(sad.get(i));
                        JSONObject inner =  data.getJSONObject(keys.getString(sad.get(i)));
                        String emoji = inner.getString("emojiRating");
                        String imagePath = inner.getString("imagePath");
                        cardClick(i + 1, key);
                        emojiDisplay(emoji, i + 1);
                        imageDisplay(imagePath, i + 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        //});
    }

    private void pastMonthFunc(List<Integer> pastMonth, JSONArray keys, JSONObject data){
        //Button pastMonthBut = (Button)findViewById(R.id.pastMonthButton);

        //pastMonthBut.setOnClickListener(v -> {
            if(pastMonth.size() < 9){
                Toast.makeText(getApplicationContext(), "You don't have enough moments in the past month to generate a highlight!", Toast.LENGTH_LONG).show();
            }
            else{

                List<Integer> indicesList = new ArrayList<>();
                for (int i = 0; i < pastMonth.size(); i++) {
                    indicesList.add(pastMonth.get(i));
                }
                Collections.shuffle(indicesList);
                HashSet<Integer> selectedIndices = new HashSet<>();
                for (int i = 0; i < pastMonth.size() && i < 9; i++) {
                    selectedIndices.add(indicesList.get(i));
                }

                int cardCounter = 0;

                for(int i=0; i<pastMonth.size(); i++){
                    try {
                        String key = keys.getString(pastMonth.get(i));
                        JSONObject inner =  data.getJSONObject(keys.getString(pastMonth.get(i)));
                        String emoji = inner.getString("emojiRating");
                        String imagePath = inner.getString("imagePath");
                        if(selectedIndices.contains(pastMonth.get(i))) {
                            cardClick(cardCounter + 1, key);
                            emojiDisplay(emoji, cardCounter + 1);
                            imageDisplay(imagePath, cardCounter + 1);
                            cardCounter = cardCounter + 1;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        //});
    }

    private void pastYearFunc(List<Integer> pastYear, JSONArray keys, JSONObject data){
        //Button pastYearBut = (Button)findViewById(R.id.pastYearButton);

        //pastYearBut.setOnClickListener(v -> {
            if(pastYear.size() < 9){
                Toast.makeText(getApplicationContext(), "You don't have enough moments in the past year to generate a highlight!", Toast.LENGTH_LONG).show();
            }
            else{

                List<Integer> indicesList = new ArrayList<Integer>();
                for (int i = 0; i < pastYear.size(); i++) {
                    indicesList.add(pastYear.get(i));
                }
                Collections.shuffle(indicesList);
                HashSet<Integer> selectedIndices = new HashSet<>();
                for (int i = 0; i < pastYear.size() && i < 9; i++) {
                    selectedIndices.add(indicesList.get(i));
                }

                int cardCounter = 0;

                for(int i=0; i< pastYear.size(); i++){
                    try {
                        String key = keys.getString(pastYear.get(i));
                        JSONObject inner =  data.getJSONObject(keys.getString(pastYear.get(i)));
                        String emoji = inner.getString("emojiRating");
                        String imagePath = inner.getString("imagePath");
                        if(selectedIndices.contains(pastYear.get(i))) {
                            cardClick(cardCounter + 1, key);
                            emojiDisplay(emoji, cardCounter + 1);
                            imageDisplay(imagePath, cardCounter + 1);
                            cardCounter = cardCounter + 1;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        //});
    }

    private void randomFunc(List<Integer> allData, JSONArray keys, JSONObject data){
        if(allData.size() < 9){
            Toast.makeText(getApplicationContext(), "You don't have enough moments to generate a highlight!", Toast.LENGTH_LONG).show();
        }
        else{

            List<Integer> indicesList = new ArrayList<Integer>();
            for (int i = 0; i < allData.size(); i++) {
                indicesList.add(allData.get(i));
            }
            Collections.shuffle(indicesList);
            HashSet<Integer> selectedIndices = new HashSet<>();
            for (int i = 0; i < allData.size() && i < 9; i++) {
                selectedIndices.add(indicesList.get(i));
            }

            int cardCounter = 0;

            for(int i=0; i< allData.size(); i++){
                try {
                    String key = keys.getString(allData.get(i));
                    JSONObject inner =  data.getJSONObject(keys.getString(allData.get(i)));
                    String emoji = inner.getString("emojiRating");
                    String imagePath = inner.getString("imagePath");
                    if(selectedIndices.contains(allData.get(i))) {
                        cardClick(cardCounter + 1, key);
                        emojiDisplay(emoji, cardCounter + 1);
                        imageDisplay(imagePath, cardCounter + 1);
                        cardCounter = cardCounter + 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}