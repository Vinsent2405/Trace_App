package com.example.schoolproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.schoolproject.Adapter.ShowsAdapter;
import com.example.schoolproject.Model.ShowModel;
import com.example.schoolproject.Utils.DataBaseHelper;
import com.example.schoolproject.databinding.ActivityRankBinding;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RankActivity extends AppCompatActivity implements OnDialogCloseListener {

    private ActivityRankBinding binding;
    private DataBaseHelper dataBaseHelper;
    private List<ShowModel> showModels;
    private ShowsAdapter adapter;
    private int listId = -1;

    //Initializes the activity and sets up the UI components
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Enables edge to edge display
        EdgeToEdge.enable(this);
        //Inflates the layout using view binding
        binding = ActivityRankBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Sets up the custom toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //Enables back button navigation
            actionBar.setDisplayHomeAsUpEnabled(true);
            
            // Get the name passed from the adapter
            String listName = getIntent().getStringExtra("list_name");
            listId = getIntent().getIntExtra("list_id", -1);
            if (listName != null) {
                binding.toolbarTitle.setText(listName);
            }
        }

        //Handles window insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.rating, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, 0, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            binding.toolbar.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        //Initializes database helper and lists
        dataBaseHelper = new DataBaseHelper(this);
        showModels = new ArrayList<>();
        
        //Configures the recycler view for rank items
        binding.recycleViewRank.setLayoutManager(new LinearLayoutManager(this));
        
        //Initializes the shows adapter
        adapter = new ShowsAdapter(dataBaseHelper, this);
        binding.recycleViewRank.setAdapter(adapter);

        //Attaches touch helper for show swipe actions
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ShowsTouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(binding.recycleViewRank);

        //Sets click listener for adding a new show
        binding.addShowButton.setOnClickListener(v -> AddNewShow.newInstance(listId).show(getSupportFragmentManager(), AddNewShow.TAG));

        //Loads initial shows and applies theme
        loadShows();
        applyTheme();
    }

    //Updates the user level badge based on total shows count
    private void updateUserLevel() {
        //Calculates current level and progress
        int totalShows = dataBaseHelper.getTotalShowsCount();
        int level = dataBaseHelper.calculateLevel(totalShows);
        int showsLeft = dataBaseHelper.showsLeftToNextLevel(totalShows);

        //Updates the level UI and progress dialog logic
        binding.tvUserLevel.setText(String.valueOf(level));
        binding.levelBadgeContainer.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Level Progress")
                    .setMessage(String.format(Locale.getDefault(), "You are Level %d!\nOnly %d more shows until Level %d.",
                            level, showsLeft, level + 1))
                    .setPositiveButton("Let's go!", null)
                    .show();
        });
    }

    //Applies the user's selected theme colors
    private void applyTheme() {
        //Retrieves theme colors from shared preferences
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int primaryColor = prefs.getInt("primary_color", Color.parseColor("#241E24"));
        int secondaryColor = prefs.getInt("secondary_color", Color.parseColor("#3B2F3B"));
        int userTextColor = prefs.getInt("text_color", Color.WHITE);

        //Sets background and toolbar colors
        binding.rating.setBackgroundColor(primaryColor);
        
        binding.toolbar.setBackgroundColor(secondaryColor);
        binding.toolbar.setTitleTextColor(userTextColor);
        binding.toolbarTitle.setTextColor(userTextColor);
        
        //Styles the action buttons
        binding.addShowButton.setBackgroundColor(secondaryColor);
        binding.addShowButton.setTextColor(userTextColor);
        binding.addShowButton.setIconTint(ColorStateList.valueOf(userTextColor));

        // Stylize and color the Level Badge based on user text color
        binding.levelBadgeContainer.setBackgroundTintList(ColorStateList.valueOf(userTextColor));
        binding.tvUserLevel.setTextColor(secondaryColor); // Contrast level text with badge color

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

    //Helper method to check if a color is perceived as dark
    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    //Refreshes the activity data when it returns to focus
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list whenever we return to this activity
        loadShows();
        applyTheme();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    //Loads all shows for the current list from the database
    private void loadShows() {
        showModels = dataBaseHelper.getShowsForList(listId);
        adapter.setShowModels(showModels);
        updateUserLevel();
    }

    //Callback for when a dialog is closed
    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        loadShows();
    }

    //Creates the options menu with search functionality
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rank, menu);
        
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        
        // Theme Searchview
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int userTextColor = prefs.getInt("text_color", Color.WHITE);

        if (searchView != null) {
            searchView.setQueryHint("Search shows...");

            // Find the search text and change its color
            EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            if (searchEditText != null) {
                searchEditText.setTextColor(userTextColor);
                searchEditText.setHintTextColor(Color.argb(150, Color.red(userTextColor), Color.green(userTextColor), Color.blue(userTextColor)));
            }

            //Sets listener for search query changes
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.filter(newText);
                    return false;
                }
            });
        }

        return true;
    }

    //Handles selection of options menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Handles back button navigation
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        //Handles navigation to tag management
        if (item.getItemId() == R.id.action_manage_tags) {
            Intent intent = new Intent(this, TagsActivity.class);
            startActivity(intent);
            return true;
        }
        //Handles showing filter help dialog
        if (item.getItemId() == R.id.action_filter_info) {
            showFilterHelpDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Displays a dialog with instructions on how to filter shows
    private void showFilterHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("How to Filter")
                .setMessage("You can filter your shows in three ways:\n\n" +
                        "1. Name/Tag: Just type a word (e.g. \"Kaguya\")\n\n" +
                        "2. Grade Range: Type min-max (e.g. \"7.5-10.0\")\n\n" +
                        "3. Multi-Tag: Separate tags with commas (e.g. \"Action, Comedy\")\n\n" +
                        "Pro Tip: You can combine them! \nTry: \"Romance 8.0-10.0\"")
                .setPositiveButton("Got it", null)
                .show();
    }
}