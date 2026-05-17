package com.example.schoolproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolproject.Adapter.TagsAdapter;
import com.example.schoolproject.Model.TagModel;
import com.example.schoolproject.Utils.DataBaseHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TagsActivity extends AppCompatActivity implements TagsAdapter.OnTagClickListener {

    private RecyclerView recyclerView;
    private TagsAdapter adapter;
    private DataBaseHelper dataBaseHelper;
    private List<TagModel> tagList;
    private ExtendedFloatingActionButton addTagButton;

    //Initializes the activity and sets up the tag management UI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Enables edge to edge system bars
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tags);

        //Sets up the custom toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Tags");
        }

        //Handles window insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, 0, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            findViewById(R.id.toolbar).setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        //Initializes database helper
        dataBaseHelper = new DataBaseHelper(this);

        //Configures the recycler view for tags
        recyclerView = findViewById(R.id.recycle_view_tags);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TagsAdapter(this, this);
        recyclerView.setAdapter(adapter);

        //Initializes and sets click listener for adding tags
        addTagButton = findViewById(R.id.addTagButton);
        addTagButton.setOnClickListener(v -> showAddTagDialog());

        //Loads initial tags and applies theme
        loadTags();
        applyTheme();
    }

    //Applies the user's selected theme colors to the UI components
    private void applyTheme() {
        //Retrieves theme colors from shared preferences
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int primaryColor = prefs.getInt("primary_color", Color.parseColor("#241E24"));
        int secondaryColor = prefs.getInt("secondary_color", Color.parseColor("#3B2F3B"));
        int userTextColor = prefs.getInt("text_color", Color.WHITE);

        //Sets background and toolbar colors
        findViewById(R.id.main).setBackgroundColor(primaryColor);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(secondaryColor);
        toolbar.setTitleTextColor(userTextColor);
        
        //Styles the add tag button
        addTagButton.setBackgroundColor(secondaryColor);
        addTagButton.setTextColor(userTextColor);
        addTagButton.setIconTint(android.content.res.ColorStateList.valueOf(userTextColor));

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

    //Helper method to determine if a color is perceived as dark
    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    //Refreshes the activity theme on resume
    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }

    //Loads all tags from the database and updates the adapter
    private void loadTags() {
        tagList = dataBaseHelper.getAllTags();
        adapter.setTagList(tagList);
    }

    //Displays a dialog for entering a new tag name
    private void showAddTagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Tag");

        //Creates an input field for the tag name
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        //Handles the add button click in the dialog
        builder.setPositiveButton("Add", (dialog, which) -> {
            String tagName = input.getText().toString().trim();
            //Inserts the tag if not empty
            if (!tagName.isEmpty()) {
                dataBaseHelper.insertTag(tagName);
                loadTags();
            }
        });
        //Handles the cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    //Callback for single tag click (placeholder)
    @Override
    public void onTagClick(TagModel tag) {
        // No longer needed for selection, maybe useful for editing in future
    }

    //Handles long clicks on tags to show a deletion confirmation
    @Override
    public void onTagLongClick(TagModel tag) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tag")
                .setMessage("Are you sure you want to delete this tag?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    //Deletes the tag and refreshes list
                    dataBaseHelper.deleteTag(tag.getId());
                    loadTags();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //Handles back button navigation in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
