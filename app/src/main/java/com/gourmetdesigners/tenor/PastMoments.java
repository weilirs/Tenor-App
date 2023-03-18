package com.gourmetdesigners.tenor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PastMoments extends AppCompatActivity {

    BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_moments);
        System.out.println("##########Entered onCreate!!! ##############");

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.past_moments_menu_item);
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
                    return true;
                case R.id.highlights_menu_item:
                    startActivity(new Intent(getApplicationContext(), Highlights.class));
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });
        try {
            RecyclerView pastMomentsView = findViewById(R.id.past_moments_recycler_view);

            File directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File file = directory.listFiles()[0];
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String captures_json = null;
            try {
                FileChannel file_channel = stream.getChannel();
                MappedByteBuffer byte_buffer = file_channel.map(FileChannel.MapMode.READ_ONLY, 0, file_channel.size());
                captures_json = Charset.defaultCharset().decode(byte_buffer).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            JSONObject jsonObj = null;
            try {
                jsonObj = new JSONObject(captures_json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject data = null;
            try {
                data = jsonObj.getJSONObject("map");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray keys = data.names();
            Log.d("map", "keys:" + data.length());

            List<String> json_objects = new ArrayList<>();
            for (int i = 0; i < keys .length(); i++) {
                try {
                    json_objects.add(keys .getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("########################");
            System.out.println(keys.length());
            System.out.println("########################");

            Collections.sort(json_objects, Collections.reverseOrder());
            JSONArray sorted_keys = new JSONArray(json_objects);

            ArrayList<String> imageArray = new ArrayList<String>();
            ArrayList<String> textArray = new ArrayList<String>();
            ArrayList<String> keyArray = new ArrayList<String>();
            ArrayList<String> emojiArray = new ArrayList<String>();

            // Looping over all the nodes in sorted_keys JSONArray
            for (int i = 0; i < sorted_keys.length(); i++) {
                String key = null;
                String imagePath = null;
                try {
                    key = sorted_keys.getString(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject inner = null;
                try {
                    inner = data.getJSONObject(sorted_keys.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    imagePath = inner.getString("imagePath");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                long epoch = Long.parseLong(key);
                Date date = new Date(epoch * 1000);
                String formatted_date = new SimpleDateFormat("MMM dd, E, hh:mm a").format(date).replace("am", "AM").replace("pm","PM");
                imageArray.add(imagePath);
                textArray.add(formatted_date);
                keyArray.add(key);
                String emoji = inner.getString("emojiRating");
                emojiArray.add(emoji);
            }
            CardAdapter cardAdapter = new CardAdapter(this, imageArray, textArray, keyArray, emojiArray);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PastMoments.this, LinearLayoutManager.VERTICAL, false);
            pastMomentsView.setLayoutManager(linearLayoutManager);
            pastMomentsView.setAdapter(cardAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder>{

        private final PastMoments context;
        private final ArrayList<String> imageArray;
        private final ArrayList<String> textArray;
        private final ArrayList<String> keyArray;
        private final ArrayList<String> emojiArray;


        public CardAdapter(PastMoments context, ArrayList<String> imageArray, ArrayList<String> textArray, ArrayList<String> keyArray, ArrayList<String> emojiArray) {
            this.context = context;
            this.textArray = textArray;
            this.imageArray = imageArray;
            this.keyArray = keyArray;
            this.emojiArray = emojiArray;
        }

        @NonNull
        @Override
        public CardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CardAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.past_moments_card_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CardAdapter.ViewHolder holder, int index) {
            String imageString = imageArray.get(index);
            Bitmap imgBitmap = BitmapFactory.decodeFile(imageString);
            holder.imageView.setImageBitmap(imgBitmap);
            holder.imageView.getLayoutParams().height = 1000;

            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) holder.imageView.getLayoutParams();
            marginParams.setMargins(0, 0, 0, 10);

            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(PastMoments.this, SeeHighlight.class);
                    Bundle see_highlight_bundle = new Bundle();
                    see_highlight_bundle.putString("key", keyArray.get(index));
                    intent.putExtras(see_highlight_bundle);
                    startActivity(intent);
                });
            
            holder.textView.setText(textArray.get(index));
            switch (emojiArray.get(index)) {
                case "-2":
                    holder.emojiView.setImageResource(R.drawable.awful);
                    break;
                case "-1":
                    holder.emojiView.setImageResource(R.drawable.bad);
                    break;
                case "0":
                    holder.emojiView.setImageResource(R.drawable.okay);
                    break;
                case "1":
                    holder.emojiView.setImageResource(R.drawable.good);
                    break;
                case "2":
                    holder.emojiView.setImageResource(R.drawable.great);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return imageArray.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;
            private final ImageView imageView;
            private final ImageView emojiView;

            public ViewHolder(@NonNull View view) {
                super(view);
                imageView = view.findViewById(R.id.cardImage);
                textView = view.findViewById(R.id.cardText);
                emojiView = view.findViewById(R.id.cardEmoji);
            }
        }
    }
}