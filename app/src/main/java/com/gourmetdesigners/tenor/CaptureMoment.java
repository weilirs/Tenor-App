package com.gourmetdesigners.tenor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.gson.Gson;
import com.hadi.emojiratingbar.EmojiRatingBar;
import com.hadi.emojiratingbar.RateStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CaptureMoment extends AppCompatActivity {

    BottomNavigationView bottomNav;
    private Button pickDateButton;
    private TextView selectedDateText;
    int year, month, day, hour, minute;
    String date_string;
    @SuppressLint("DefaultLocale") Date chosen_date;

    public void writeFileToInternalStorage(String fileName, String content){
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "");
        if(!dir.exists()){
            dir.mkdir();
        }
        try {
            File file = new File(dir, fileName);
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected int sizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }

    // Getting the thumbnail and displaying in ImageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String picDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String latestImageFilePath = getLatestImageFilePath(picDir);
        Bitmap imageBitmap = BitmapFactory.decodeFile(latestImageFilePath);
        // Try-catch block takes us to starting activity if user exits from camera.
        try {
            int imageSizeBytes = sizeOf(imageBitmap);
        }
        catch (Exception e) {
            finish(); // Closes current activity, goes back to next activity on stack
        }
        ImageView imageView = findViewById(R.id.captured_image_view);
        imageView.setImageBitmap(imageBitmap);
    }

    // Intent which delegates picture taking to default camera app. Ignore deprecated warning.
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("Error", "Error occurred while creating the image file!");
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.gourmetdesigners.tenor.fileprovider", photoFile);
                takePictureIntent.putExtra("photoURI", photoURI);
                takePictureIntent.putExtra("imagePath", photoFile.getAbsolutePath());
                // Line below saves image, but makes extra data from returned intent NULL, hence no way to get bitmap from the Intent in onActivityResult, have to load image from file again.
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_moment);
        dispatchTakePictureIntent();

        // Setting current date display below journal text entry to current date
        chosen_date = new Date();
        date_string = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(chosen_date);

        // Getting elements: journal entry text area, submit button, and the emoji rating bar
        EditText journalEntryTextArea = findViewById(R.id.captureMomentTextEntry);
        Button submit_button = findViewById(R.id.captureMomentSubmitButton);
        EmojiRatingBar emojiRatingBar = findViewById(R.id.emoji_rating_bar);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                                                                              2022 10 18 15 80

        pickDateButton = findViewById(R.id.pick_date_button);
        pickDateButton.setText(date_string);

        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        CaptureMoment.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                hour = c.get(Calendar.HOUR_OF_DAY);
                                minute = c.get(Calendar.MINUTE);
                                TimePickerDialog timePickerDialog = new TimePickerDialog(CaptureMoment.this,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                                try {
                                                    chosen_date = sdf.parse(String.format("%d-%02d-%02d %02d:%02d:%02d", year, month + 1, day, hour, minute, 0));
                                                    date_string = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(chosen_date);
                                                    pickDateButton.setText(date_string);
                                                } catch (ParseException ex) {
                                                    Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                    Log.v("Exception", ex.getLocalizedMessage());
                                                }
                                            }
                                        }, hour, minute, false);
                                timePickerDialog.show();
                            }
                        },
                        year, month, day);
                datePickerDialog.show();
            }
        });

        emojiRatingBar.setAwfulEmojiTitle("Awful");
        emojiRatingBar.setBadEmojiTitle("Bad");
        emojiRatingBar.setOkayEmojiTitle("Okay");
        emojiRatingBar.setGoodEmojiTitle("Good");
        emojiRatingBar.setGreatEmojiTitle("Great!");

        // When submit button is clicked, save the data to a JSON file
        submit_button.setOnClickListener(view -> {

            // Getting the journal entry, current epoch time, and emoji rating
            String journalEntry = journalEntryTextArea.getText().toString().trim();
            int rateStatus = getCurrentRateStatus(emojiRatingBar);
            System.out.println("##################");
            System.out.println(rateStatus);
            System.out.println("##################");
            if(rateStatus == 100) {
                Toast.makeText(getApplicationContext(), "Please select an emoji before submitting!", Toast.LENGTH_LONG).show();
            }
            else {
                Long epochLong = chosen_date.getTime() / 1000L;
                String epoch = epochLong.toString();

                // Creating hashmap containing entry data
                Map<String, String> entry = new HashMap<>();
                entry.put("imagePath", currentPhotoPath);
                entry.put("emojiRating", String.valueOf(rateStatus));
                entry.put("journalEntry", journalEntry);

                // If file exists, append to it with help of the GSON library. If not, create a new JSON file
                Data data;
                String jsonFilePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/data.json";
                File dataFile = new File(jsonFilePath);
                if (dataFile.exists()) {
                    int length = (int) dataFile.length();
                    byte[] bytes = new byte[length];
                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(dataFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        try {
                            in.read(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    String contents = new String(bytes);
                    Gson gson = new Gson();
                    data = gson.fromJson(contents, Data.class);
                    data.getMap().put(epoch, entry); // Add new entry to existing JSON (converted to hashmap)
                } else {
                    Map<String, Map<String, String>> map = new HashMap<>();
                    map.put(epoch, entry);
                    data = new Data(map);
                }
                Gson gson = new Gson();
                String json = gson.toJson(data);
                writeFileToInternalStorage("data.json", json);
                startActivity(new Intent(CaptureMoment.this, MainActivity.class));
                Toast.makeText(getApplicationContext(), "Journal entry successfully saved!", Toast.LENGTH_LONG).show();
            }
            });

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

    // Gets the current emoji rating score from the emoji bar. Scales from -2 to 2, for a total of 5 values.
    int getCurrentRateStatus(EmojiRatingBar emojiRatingBar) {
        RateStatus currentRateStatus = emojiRatingBar.getCurrentRateStatus();
        if (currentRateStatus == RateStatus.AWFUL) return -2;
        else if (currentRateStatus == RateStatus.BAD) return -1;
        else if (currentRateStatus == RateStatus.OKAY) return -0;
        else if (currentRateStatus == RateStatus.GOOD) return 1;
        else if (currentRateStatus == RateStatus.GREAT) return 2;
        return 100;
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    // Code for saving image from camera
    String currentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        Long epochLong = System.currentTimeMillis() / 1000L;
        String epoch = epochLong.toString();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(epoch, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Looks at the provided directory and gets the file name of the image that was last added.
    public static String getLatestImageFilePath(String directoryFilePath) {
        File directory = new File(directoryFilePath);
        File[] files = directory.listFiles(File::isFile);
        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() > lastModifiedTime) {
                    chosenFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }
        }
        return chosenFile.getAbsolutePath();
    }
}