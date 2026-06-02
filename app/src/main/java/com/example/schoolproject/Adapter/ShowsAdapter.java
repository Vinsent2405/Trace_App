package com.example.schoolproject.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolproject.Model.ShowModel;
import com.example.schoolproject.R;
import com.example.schoolproject.RankActivity;
import com.example.schoolproject.ShowActivity;
import com.example.schoolproject.ShowExpandedFragment;
import com.example.schoolproject.Utils.DataBaseHelper;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.io.File;
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
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return showModels.get(position).getId();
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

        // Simplified tag display to prevent jitter
        String tags = showModel.getTags();
        if (tags != null && !tags.isEmpty()) {
            String[] tagArray = tags.split(",");
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (String tag : tagArray) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    if (sb.length() > 0) sb.append(" | ");
                    sb.append(trimmed);
                    count++;
                }
            }
            if (count > 0) {
                holder.tagTextView.setText(sb.toString());
                holder.tagTextView.setVisibility(View.VISIBLE);
                holder.tagTextView.setTextColor(Color.argb(200, Color.red(userTextColor), Color.green(userTextColor), Color.blue(userTextColor)));
            } else {
                holder.tagTextView.setVisibility(View.GONE);
            }
        } else {
            holder.tagTextView.setVisibility(View.GONE);
        }
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
                
                // Normalizes strings for matching (Unicode aware: \p{L} is any letter, \p{N} is any number)
                String normalizedName = name.replaceAll("[^\\p{L}\\p{N}]", "");
                String normalizedItemTags = itemTags.replaceAll("[^\\p{L}\\p{N}]", "");

                for (String kw : keywords) {
                    if (kw.isEmpty()) continue;
                    String normalizedKw = kw.replaceAll("[^\\p{L}\\p{N}]", "");

                    // Substring and fuzzy matching
                    boolean found = name.contains(kw) || itemTags.contains(kw);
                    
                    if (!found && !normalizedKw.isEmpty()) {
                        found = normalizedName.contains(normalizedKw) || normalizedItemTags.contains(normalizedKw);
                    }
                    
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
        
        // Clean up the associated image file from internal storage
        String imagePath = showModel.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            // Only delete if it's in our app's internal storage to be safe
            if (imageFile.exists() && imagePath.contains(getContext().getFilesDir().getAbsolutePath())) {
                if (imageFile.delete()) {
                    android.util.Log.d("ShowsAdapter", "Deleted orphan image file: " + imagePath);
                }
            }
        }

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
        TextView tagTextView;
        View relativeLayout;
        CardView cardView;

        public ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.User_Name);
            ratingBar = view.findViewById(R.id.itemRatingBar);
            tagTextView = view.findViewById(R.id.tagTextView);
            relativeLayout = view.findViewById(R.id.rank_layout);
            cardView = view.findViewById(R.id.cardView);
        }
    }
}