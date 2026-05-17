package com.example.schoolproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.schoolproject.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_PRIMARY = "primary_color";
    private static final String KEY_SECONDARY = "secondary_color";
    private static final String KEY_TEXT = "text_color";

    private int primaryColor, secondaryColor, textColor;
    private ActivitySettingsBinding binding;

    private final int[] availableColors = {
            Color.parseColor("#241E24"), // Default Primary (Deep Purple)
            Color.parseColor("#3B2F3B"), // Default Secondary (Purple Toolbar)
            Color.parseColor("#121212"), // Pitch Black
            Color.parseColor("#FFFFFF"), // Pure White
            Color.parseColor("#7D3C98"), // Rich Purple
            Color.parseColor("#2E86C1"), // Soft Blue
            Color.parseColor("#17A589"), // Teal Green
            Color.parseColor("#D4AC0D"), // Muted Gold
            Color.parseColor("#CB4335"), // Muted Red
            Color.parseColor("#2C3E50"), // Navy Blue
            Color.parseColor("#5D6D7E"), // Steel Gray
            Color.parseColor("#EBEDEF")  // Off-White
    };

    //Initializes the activity and sets up the settings UI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Inflates layout and enables edge to edge
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        //Sets up the custom toolbar
        setSupportActionBar(binding.toolbar);

        //Handles window insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.settingsMain, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            // No top padding here to let toolbar cover status bar area
            v.setPadding(systemBars.left, 0, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            binding.toolbar.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        //Initializes preferences and loads current colors
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadColors();

        //Sets up the action bar with back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        //Sets click listeners for color selection
        binding.btnChangePrimary.setOnClickListener(v -> showColorPickerDialog("Primary Background", KEY_PRIMARY));
        binding.btnChangeSecondary.setOnClickListener(v -> showColorPickerDialog("Secondary Background", KEY_SECONDARY));
        binding.btnChangeText.setOnClickListener(v -> showColorPickerDialog("Text & Icon Color", KEY_TEXT));

        //Resets theme to defaults on button click
        binding.btnReset.setOnClickListener(v -> {
            sharedPreferences.edit().clear().apply();
            loadColors();
            recreate();
        });

        //Applies current theme colors
        applyCurrentTheme();
    }

    //Loads theme colors from shared preferences
    private void loadColors() {
        primaryColor = sharedPreferences.getInt(KEY_PRIMARY, Color.parseColor("#241E24"));
        secondaryColor = sharedPreferences.getInt(KEY_SECONDARY, Color.parseColor("#3B2F3B"));
        textColor = sharedPreferences.getInt(KEY_TEXT, Color.WHITE);
    }

    //Displays a custom color picker dialog
    private void showColorPickerDialog(String title, String key) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_color_picker, null);
        
        // Applies current theme to dialog background
        dialogView.setBackgroundColor(primaryColor);
        int autoTextColor = isColorDark(primaryColor) ? Color.WHITE : Color.BLACK;
        
        //Creates the dialog window
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        //Sets the dialog title text
        TextView titleView = dialogView.findViewById(R.id.dialogTitle);
        titleView.setText(title);
        titleView.setTextColor(autoTextColor);

        //Populates the color selection grid
        GridLayout gridLayout = dialogView.findViewById(R.id.colorGrid);
        for (int color : availableColors) {
            gridLayout.addView(createColorCircleForDialog(color, key, dialog));
        }

        dialog.show();
    }

    //Creates a circular view for a single color option
    private View createColorCircleForDialog(final int color, final String key, final AlertDialog dialog) {
        View view = new View(this);
        //Calculates view size based on screen density
        int size = (int) (60 * getResources().getDisplayMetrics().density);
        int margin = (int) (10 * getResources().getDisplayMetrics().density);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(params);

        //Configures the circular background shape
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        shape.setStroke(3, isColorDark(color) ? Color.WHITE : Color.BLACK);
        view.setBackground(shape);

        //Handles the color selection click event
        view.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(key, color);
            
            // Automatically sets readable text color when background changes
            if (key.equals(KEY_SECONDARY)) {
                editor.putInt(KEY_TEXT, isColorDark(color) ? Color.WHITE : Color.BLACK);
            }
            
            editor.apply();
            loadColors();
            applyCurrentTheme();
            dialog.dismiss();
            
            // Re-creates the activity to fully refresh theme application
            if (key.equals(KEY_PRIMARY) || key.equals(KEY_SECONDARY) || key.equals(KEY_TEXT)) {
                recreate();
            }
        });

        return view;
    }

    //Applies current theme color choices to the UI
    private void applyCurrentTheme() {
        //Sets background and toolbar colors
        binding.settingsMain.setBackgroundColor(primaryColor);
        binding.toolbar.setBackgroundColor(secondaryColor);
        
        //Styles secondary interaction buttons
        binding.btnChangePrimary.setBackgroundColor(secondaryColor);
        binding.btnChangeSecondary.setBackgroundColor(secondaryColor);
        binding.btnChangeText.setBackgroundColor(secondaryColor);
        
        //Sets text color for buttons and titles
        binding.btnChangePrimary.setTextColor(textColor);
        binding.btnChangeSecondary.setTextColor(textColor);
        binding.btnChangeText.setTextColor(textColor);
        
        // Updates toolbar text color
        binding.toolbar.setTitleTextColor(textColor);
        
        //Styles the reset button text
        binding.btnReset.setTextColor(textColor);

        // Apply secondary color to system bars
        if (isColorDark(secondaryColor)) {
            EdgeToEdge.enable(this, 
                SystemBarStyle.dark(secondaryColor),
                SystemBarStyle.dark(secondaryColor));
        } else {
            EdgeToEdge.enable(this, 
                SystemBarStyle.light(secondaryColor, secondaryColor),
                SystemBarStyle.light(secondaryColor, secondaryColor));
        }
    }

    //Helper method to detect color darkness for contrast selection
    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    //Handles back button navigation in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}