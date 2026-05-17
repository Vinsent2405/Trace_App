package com.example.schoolproject.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolproject.AddNewShow;
import com.example.schoolproject.Model.ShowModel;
import com.example.schoolproject.R;
import com.example.schoolproject.RankActivity;
import com.example.schoolproject.ShowActivity;
import com.example.schoolproject.ShowExpandedFragment;
import com.example.schoolproject.Utils.DataBaseHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.util.ArrayList;
import java.util.List;

public class ShowsAdapter extends RecyclerView.Adapter<ShowsAdapter.ViewHolder> {

    private List<ShowModel> showModels = new ArrayList<>();
    private List<ShowModel> showModelsFull = new ArrayList<>();
    private final RankActivity rankActivity;
    private final DataBaseHelper dataBaseHelper;


    //Standard constructor for the adapter
    public ShowsAdapter(DataBaseHelper dataBaseHelper,  RankActivity rankActivity) {
        this.dataBaseHelper = dataBaseHelper;
        this.rankActivity = rankActivity;
    }


    //Inflates the show item layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_layout, parent, false);
        return new ViewHolder(view);
    }

    //Binds show data to views and applies dynamic theme
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Retrieves the show model and sets basic info
        final ShowModel showModel = showModels.get(position);
        holder.textViewTitle.setText(showModel.getName());
        // Map 0-10 grade to 0-5 stars with float precision
        float rating = (float) showModel.getGrade() / 2f;
        holder.ratingBar.setRating(rating);

        // Retrieves colors from shared preferences and applies theme
        SharedPreferences prefs = rankActivity.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        int secondaryColor = prefs.getInt("secondary_color", Color.parseColor("#3B2F3B"));
        int userTextColor = prefs.getInt("text_color", Color.WHITE);
        
        //Styles card and title text
        holder.cardView.setCardBackgroundColor(secondaryColor);
        holder.textViewTitle.setTextColor(userTextColor);
        
        // Adjusts rating bar colors
        holder.ratingBar.setFillColor(userTextColor);
        holder.ratingBar.setBorderColor(userTextColor);

        //Sets listener to open the expanded fragment view
        holder.relativeLayout.setOnClickListener(v -> {
            ShowExpandedFragment fragment = ShowExpandedFragment.newInstance(showModel.getId());
            fragment.show(rankActivity.getSupportFragmentManager(), ShowExpandedFragment.TAG);
        });

        //Handles dynamic tag generation as chips
        holder.tagGroup.removeAllViews();
        String tags = showModel.getTags();
        if (tags != null && !tags.isEmpty()) {
            String[] tagArray = tags.split(",");
            boolean hasTags = false;
            for (String tag : tagArray) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    hasTags = true;
                    Chip chip = new Chip(getContext(), null, com.google.android.material.R.attr.chipStyle);
                    chip.setText(trimmedTag);
                    chip.setEnsureMinTouchTargetSize(false);
                    chip.setTextSize(12f); // Slightly bigger text
                    chip.setChipStartPadding(10f);
                    chip.setChipEndPadding(10f);
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    
                    // Determines contrast color
                    int oppositeColor = isColorDark(secondaryColor) ? Color.WHITE : Color.BLACK;

                    // Styles the chip
                    chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(secondaryColor));
                    chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(oppositeColor));
                    chip.setChipStrokeWidth(2f);
                    chip.setTextColor(oppositeColor);
                    
                    holder.tagGroup.addView(chip);
                }
            }
            holder.tagGroup.setVisibility(hasTags ? View.VISIBLE : View.GONE);
        } else {
            holder.tagGroup.setVisibility(View.GONE);
        }
    }

    //Helper method to detect dark colors
    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    //Returns the rank activity context
    public Context getContext() {
        return rankActivity;
    }

    //Updates the dataset and refreshes the adapter
    public void setShowModels(List<ShowModel> showModels) {
        this.showModels = showModels;
        this.showModelsFull = new ArrayList<>(showModels);
        notifyDataSetChanged();
    }

    //Filters the dataset based on name, grade range, or tags
    public void filter(String text) {
        showModels.clear();
        String input = text.trim().toLowerCase();

        //Resets to full list if input is empty
        if (input.isEmpty()) {
            showModels.addAll(showModelsFull);
            notifyDataSetChanged();
            return;
        }

        // 1. Extracts Grade Range using regex
        Double minGrade = null;
        Double maxGrade = null;
        String remainingText = input;

        java.util.regex.Pattern gradePattern = java.util.regex.Pattern.compile("(\\d+(\\.\\d+)?)-(\\d+(\\.\\d+)?)");
        java.util.regex.Matcher matcher = gradePattern.matcher(input);
        if (matcher.find()) {
            try {
                String group1 = matcher.group(1);
                String group3 = matcher.group(3);
                String group0 = matcher.group(0);
                
                if (group1 != null && group3 != null && group0 != null) {
                    minGrade = Double.parseDouble(group1);
                    maxGrade = Double.parseDouble(group3);
                    // Removes the grade part from the query text
                    remainingText = input.replace(group0, "").trim();
                }
            } catch (NumberFormatException ignored) {}
        }

        // 2. Extracts Tag/Keyword keywords
        String[] keywords = null;
        if (!remainingText.isEmpty()) {
            keywords = remainingText.split(",");
            for (int i = 0; i < keywords.length; i++) {
                keywords[i] = keywords[i].trim();
            }
        }

        // 3. Iterates and applies combined filters
        for (ShowModel item : showModelsFull) {
            boolean matchesGrade = true;
            boolean matchesKeywords = true;

            // Checks grade range constraints
            if (minGrade != null && maxGrade != null) {
                matchesGrade = (item.getGrade() >= minGrade && item.getGrade() <= maxGrade);
            }

            // Checks keyword matches with fuzzy logic
            if (keywords != null) {
                String name = item.getName().toLowerCase();
                String itemTags = item.getTags() != null ? item.getTags().toLowerCase() : "";
                
                // Normalizes strings for matching
                String normalizedName = name.replaceAll("[^a-z0-9]", "");
                String normalizedItemTags = itemTags.replaceAll("[^a-z0-9]", "");

                for (String kw : keywords) {
                    if (kw.isEmpty()) continue;
                    String normalizedKw = kw.replaceAll("[^a-z0-9]", "");

                    // Substring and fuzzy matching
                    boolean found = name.contains(kw) || itemTags.contains(kw) 
                                    || normalizedName.contains(normalizedKw) || normalizedItemTags.contains(normalizedKw);
                    
                    if (!found) {
                        found = isFuzzyMatch(kw, name) || isFuzzyMatch(kw, itemTags);
                    }

                    if (!found) {
                        matchesKeywords = false;
                        break;
                    }
                }
            }

            //Adds item if all criteria are met
            if (matchesGrade && matchesKeywords) {
                showModels.add(item);
            }
        }
        notifyDataSetChanged();
    }

    //Performs fuzzy matching on words using edit distance
    private boolean isFuzzyMatch(String query, String target) {
        if (query.length() < 3) return false; // Too short for fuzzy matching
        
        // Checks individual words within the target
        String[] targetWords = target.toLowerCase().split("\\s+");
        for (String word : targetWords) {
            if (word.length() < 3) continue;
            
            int distance = getLevenshteinDistance(query, word);
            // Dynamic threshold for mismatch allowance
            int threshold = (query.length() > 5) ? 2 : 1;
            
            if (distance <= threshold) return true;
        }
        return false;
    }

    //Calculates the edit distance between two strings
    private int getLevenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    //Returns total item count
    @Override
    public int getItemCount() {
        return showModels.size();
    }

    //Deletes a show from the database and updates the recycler view
    public void deleteShow(int position) {
        ShowModel showModel = showModels.get(position);
        dataBaseHelper.deleteShow(showModel.getId());

        showModelsFull.remove(showModel);
        showModels.remove(position);
        notifyItemRemoved(position);
    }

    //Navigates to the full show activity for a specific entry
    public void openShowActivity(int position) {
        ShowModel showModel = showModels.get(position);
        Intent intent = new Intent(rankActivity, ShowActivity.class);
        intent.putExtra("id", showModel.getId());
        intent.putExtra("name", showModel.getName());
        intent.putExtra("grade", showModel.getGrade());
        intent.putExtra("description", showModel.getDescription());
        rankActivity.startActivity(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        SimpleRatingBar ratingBar;
        ChipGroup tagGroup;
        RelativeLayout relativeLayout;
        CardView cardView;

        public ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.User_Name);
            ratingBar = view.findViewById(R.id.itemRatingBar);
            tagGroup = view.findViewById(R.id.tagGroup);
            relativeLayout = view.findViewById(R.id.rank_layout);
            cardView = view.findViewById(R.id.cardView);
        }
    }
}