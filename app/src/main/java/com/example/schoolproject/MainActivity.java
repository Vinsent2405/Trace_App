package com.example.schoolproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolproject.Adapter.ListsAdapter;
import com.example.schoolproject.Model.ListModel;
import com.example.schoolproject.Utils.DataBaseHelper;
import com.example.schoolproject.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;



public class MainActivity extends AppCompatActivity implements OnDialogCloseListener {

    RecyclerView recyclerView;
    ExtendedFloatingActionButton addButton;
    DataBaseHelper dataBaseHelper;
    private List<ListModel> listModels;
    private ListsAdapter adapter;
    private ActivityMainBinding binding;

    //Initializes the activity and sets up the UI components
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Infaltes the layout using view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Enables edge to edge display
        EdgeToEdge.enable(this);

        //Sets up the custom toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //Handles window insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, 0, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            binding.toolbar.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        //Initializes database helper and UI elements
        recyclerView = binding.recycleView;
        addButton = binding.addButton;
        dataBaseHelper = new DataBaseHelper(this);
        listModels = new ArrayList<>();
        adapter = new ListsAdapter(dataBaseHelper, this);

        //Configures the recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //Loads all lists from the database
        listModels = dataBaseHelper.getAllLists();
        Collections.reverse(listModels);
        adapter.setListModels(listModels);

        //Sets click listener for adding a new list
        addButton.setOnClickListener(v -> AddNewList.newInstance().show(getSupportFragmentManager(), AddNewList.TAG));

        //Attaches touch helper for swipe actions
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecycleViewTouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //Applies the saved theme and updates user level
        applyTheme();
        updateUserLevel();
    }

    //Updates the user level badge based on total shows
    private void updateUserLevel() {
        //Gets counts and calculates levels
        int totalShows = dataBaseHelper.getTotalShowsCount();
        int level = dataBaseHelper.calculateLevel(totalShows);
        int showsLeft = dataBaseHelper.showsLeftToNextLevel(totalShows);

        //Sets the level text and click listener for progress info
        binding.tvUserLevel.setText(String.valueOf(level));
        binding.levelBadgeContainer.setOnClickListener(v -> new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Level Progress")
                .setMessage(String.format(Locale.getDefault(), "You are Level %d!\nOnly %d more shows until Level %d.",
                        level, showsLeft, level + 1))
                .setPositiveButton("Let's go!", null)
                .show());
    }

    //Applies the user's selected theme colors
    private void applyTheme() {
        //Retrieves colors from shared preferences
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int primaryColor = prefs.getInt("primary_color", Color.parseColor("#241E24"));
        int secondaryColor = prefs.getInt("secondary_color", Color.parseColor("#3B2F3B"));
        int userTextColor = prefs.getInt("text_color", Color.WHITE);

        //Sets background and toolbar colors
        binding.main.setBackgroundColor(primaryColor);
        
        binding.toolbar.setBackgroundColor(secondaryColor);
        binding.toolbar.setTitleTextColor(userTextColor);
        binding.toolbarTitle.setTextColor(userTextColor);
        
        //Styles the add button
        binding.addButton.setBackgroundColor(secondaryColor);
        binding.addButton.setTextColor(userTextColor);
        binding.addButton.setIconTint(ColorStateList.valueOf(userTextColor));

        //Stylize and color the Level Badge based on user text color
        binding.levelBadgeContainer.setBackgroundTintList(ColorStateList.valueOf(userTextColor));
        binding.tvUserLevel.setTextColor(secondaryColor); 

        //Apply secondary color to system bars
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

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data from DB
        listModels = dataBaseHelper.getAllLists();
        java.util.Collections.reverse(listModels);
        adapter.setListModels(listModels);

        applyTheme();
        updateUserLevel();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_manage_tags) {
            Intent intent = new Intent(this, TagsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogClose(DialogInterface dialog) {
        listModels = dataBaseHelper.getAllLists();
        Collections.reverse(listModels);
        adapter.setListModels(listModels);
    }
}
