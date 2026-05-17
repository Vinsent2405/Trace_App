package com.example.schoolproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.schoolproject.Model.ShowModel;
import com.example.schoolproject.Model.TagModel;
import com.example.schoolproject.Utils.DataBaseHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShowActivity extends AppCompatActivity {

    private TextInputEditText editTextName, editTextGrade, editTextDescription;
    private SimpleRatingBar ratingBar;
    private Button buttonSave;
    private ImageView imageViewShow;
    private ChipGroup chipGroupTags;
    private DataBaseHelper dataBaseHelper;
    private int showId;
    private String currentImagePath;
    private final ArrayList<Integer> selectedTagIds = new ArrayList<>();

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    currentImagePath = copyImageToInternalStorage(uri);
                    if (currentImagePath != null) {
                        Glide.with(this).load(currentImagePath).into(imageViewShow);
                        findViewById(R.id.noImageText).setVisibility(View.GONE);
                        findViewById(R.id.tapToChangeText).setVisibility(View.GONE);
                    }
                }
            });

    //Initializes the activity and sets up the show editing UI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Enables edge to edge system bars
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show);

        //Handles window insets for system bars and toolbar padding
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            androidx.core.graphics.Insets ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, 0, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            findViewById(R.id.toolbar).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        //Sets up the custom toolbar with back navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //Initializes the database helper
        dataBaseHelper = new DataBaseHelper(this);
        //Updates the user level badge
        updateUserLevel();

        //Links UI components from the layout
        editTextName = findViewById(R.id.editTextShowName);
        editTextGrade = findViewById(R.id.editTextShowGrade);
        editTextDescription = findViewById(R.id.editTextShowDescription);
        chipGroupTags = findViewById(R.id.chipGroupSelectedTags);
        ratingBar = findViewById(R.id.ratingBar);
        buttonSave = findViewById(R.id.buttonSave);
        imageViewShow = findViewById(R.id.imageViewShow);

        //Sets up image picker on image card click
        findViewById(R.id.cardShowImage).setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        //Loads existing show data if provided via intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            showId = extras.getInt("id");
            
            // Re-fetch full data from DB to get Image Path and Description correctly
            ShowModel fullShow = dataBaseHelper.getShowById(showId);
            if (fullShow != null) {
                //Populates fields with show data
                editTextName.setText(fullShow.getName());
                ((TextView)findViewById(R.id.toolbarTitle)).setText(fullShow.getName());
                editTextGrade.setText(String.valueOf(fullShow.getGrade()));
                editTextDescription.setText(fullShow.getDescription());
                ratingBar.setRating((float) fullShow.getGrade() / 2f);
                currentImagePath = fullShow.getImagePath();
                
                //Loads show image using Glide
                if (currentImagePath != null && !currentImagePath.isEmpty()) {
                    Glide.with(this).load(currentImagePath).into(imageViewShow);
                    findViewById(R.id.noImageText).setVisibility(View.GONE);
                    findViewById(R.id.tapToChangeText).setVisibility(View.GONE);
                } else {
                    imageViewShow.setImageResource(R.drawable.placeholder_background);
                    findViewById(R.id.noImageText).setVisibility(View.VISIBLE);
                    findViewById(R.id.tapToChangeText).setVisibility(View.VISIBLE);
                }
            }

            //Loads tags associated with this show
            loadShowTags();
        }

        //Updates grade text field when rating bar changes
        ratingBar.setOnRatingBarChangeListener((simpleRatingBar, rating, fromUser) -> {
            if (fromUser) {
                editTextGrade.setText(String.format(Locale.US, "%.2f", rating * 2f));
            }
        });

        //Sets save button click listener
        buttonSave.setOnClickListener(v -> saveChanges());
        //Applies user theme
        applyTheme();
    }

    //Updates the user level UI badge
    private void updateUserLevel() {
        //Calculates levels based on show count
        int totalShows = dataBaseHelper.getTotalShowsCount();
        int level = dataBaseHelper.calculateLevel(totalShows);
        int showsLeft = dataBaseHelper.showsLeftToNextLevel(totalShows);

        //Sets level text and defines progress dialog behavior
        ((TextView)findViewById(R.id.tvUserLevel)).setText(String.valueOf(level));
        findViewById(R.id.levelBadgeContainer).setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Level Progress")
                    .setMessage(String.format(Locale.getDefault(), "You are Level %d!\nOnly %d more shows until Level %d.",
                            level, showsLeft, level + 1))
                    .setPositiveButton("Keep it up!", null)
                    .show();
        });
    }

    //Applies the user's selected theme colors to the UI
    private void applyTheme() {
        //Retrieves theme preferences
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int primaryColor = prefs.getInt("primary_color", Color.parseColor("#241E24"));
        int secondaryColor = prefs.getInt("secondary_color", Color.parseColor("#3B2F3B"));
        int userTextColor = prefs.getInt("text_color", Color.WHITE);

        //Sets background and toolbar colors
        findViewById(R.id.main).setBackgroundColor(primaryColor);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(secondaryColor);
        toolbar.setTitleTextColor(userTextColor);
        ((TextView)findViewById(R.id.toolbarTitle)).setTextColor(userTextColor);

        // Stylize and color the Level Badge based on user text color
        View levelBadgeContainer = findViewById(R.id.levelBadgeContainer);
        levelBadgeContainer.setBackgroundTintList(ColorStateList.valueOf(userTextColor));
        ((TextView)findViewById(R.id.tvUserLevel)).setTextColor(secondaryColor);

        //Styles the save button
        buttonSave.setBackgroundColor(secondaryColor);
        buttonSave.setTextColor(userTextColor);

        //Calculates hint color with transparency
        int hintColor = Color.argb(150, Color.red(userTextColor), Color.green(userTextColor), Color.blue(userTextColor));

        // Apply to input fields
        editTextName.setTextColor(userTextColor);
        editTextName.setHintTextColor(hintColor);
        editTextGrade.setTextColor(userTextColor);
        editTextGrade.setHintTextColor(hintColor);
        editTextDescription.setTextColor(userTextColor);
        editTextDescription.setHintTextColor(hintColor);
        
        ((TextView)findViewById(R.id.textViewTagsLabel)).setTextColor(userTextColor);

        // Adjusts rating bar colors
        ratingBar.setFillColor(userTextColor);
        ratingBar.setBorderColor(userTextColor);

        // Apply secondary color to system bars
        if (isColorDark(secondaryColor)) {
            androidx.activity.EdgeToEdge.enable(this, 
                androidx.activity.SystemBarStyle.dark(secondaryColor),
                androidx.activity.SystemBarStyle.dark(secondaryColor));
        } else {
            androidx.activity.EdgeToEdge.enable(this, 
                androidx.activity.SystemBarStyle.light(secondaryColor, secondaryColor),
                androidx.activity.SystemBarStyle.light(secondaryColor, secondaryColor));
        }
    }

    //Helper method to detect if a color is dark
    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    //Refreshes the theme when activity resumes
    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }

    //Loads all tags associated with the current show
    private void loadShowTags() {
        List<TagModel> tags = dataBaseHelper.getTagsForShow(showId);
        selectedTagIds.clear();
        for (TagModel tag : tags) {
            selectedTagIds.add(tag.getId());
        }
        updateTagsDisplay();
    }

    //Dynamically populates the chip group with all available tags
    private void updateTagsDisplay() {
        chipGroupTags.removeAllViews();
        List<TagModel> allTags = dataBaseHelper.getAllTags();
        for (TagModel tag : allTags) {
            Chip chip = new Chip(this);
            chip.setText(tag.getName());
            chip.setCheckable(true);
            chip.setChecked(selectedTagIds.contains(tag.getId()));
            
            // Toggle selection state when clicked
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedTagIds.contains(tag.getId())) {
                        selectedTagIds.add(tag.getId());
                    }
                } else {
                    selectedTagIds.remove(Integer.valueOf(tag.getId()));
                }
            });
            
            chipGroupTags.addView(chip);
        }
    }

    //Displays a snackbar with white text on black background
    private void showBlackMessage(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(Color.BLACK);
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

    //Displays a custom styled toast message
    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.custom_toast_text);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    //Saves the modified show data to the database
    private void saveChanges() {
        String name = editTextName.getText().toString().trim();
        String gradeStr = editTextGrade.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        //Validates input fields
        if (name.isEmpty() || gradeStr.isEmpty()) {
            showBlackMessage("Name and Grade cannot be empty");
            return;
        }

        try {
            //Parses grade and updates database
            double grade = Double.parseDouble(gradeStr);
            ShowModel showModel = new ShowModel();
            showModel.setId(showId);
            showModel.setName(name);
            showModel.setGrade(grade);
            showModel.setDescription(description);
            showModel.setImagePath(currentImagePath);

            dataBaseHelper.updateShow(showModel);
            dataBaseHelper.updateTagToShow(showId, selectedTagIds);
            
            showCustomToast("Changes saved");
            
            //Returns success result and closes activity
            setResult(RESULT_OK); // Notify that changes were made
            finish();
        } catch (NumberFormatException e) {
            showBlackMessage("Invalid grade format");
        }
    }

    //Copies a selected image URI to internal storage for persistent access
    private String copyImageToInternalStorage(Uri uri) {
        try {
            //Opens input stream from URI
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            //Creates destination file in internal storage
            String fileName = "show_" + showId + "_" + System.currentTimeMillis() + ".jpg";
            java.io.File file = new java.io.File(getFilesDir(), fileName);
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);

            //Buffer copies the data
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            //Closes streams
            inputStream.close();
            outputStream.close();

            return file.getAbsolutePath();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Inflates the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Handles selection of menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_manage_tags) {
            Intent intent = new Intent(this, TagsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Handles back button navigation in toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}