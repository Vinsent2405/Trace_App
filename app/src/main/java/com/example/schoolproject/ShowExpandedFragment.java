package com.example.schoolproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.schoolproject.Model.ShowModel;
import com.example.schoolproject.Utils.DataBaseHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class ShowExpandedFragment extends DialogFragment {

    public static final String TAG = "ShowExpandedFragment";
    private int showId;
    private DataBaseHelper dataBaseHelper;

    //Returns a new instance of the fragment with show ID
    public static ShowExpandedFragment newInstance(int showId) {
        ShowExpandedFragment fragment = new ShowExpandedFragment();
        Bundle args = new Bundle();
        args.putInt("show_id", showId);
        fragment.setArguments(args);
        return fragment;
    }

    //Inflates the fragment layout and configures window appearance
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            //Makes the dialog background transparent
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            //Sets the dim amount for the background
            getDialog().getWindow().setDimAmount(0.3f);
        }
        return inflater.inflate(R.layout.show_expanded_card, container, false);
    }

    //Initializes views and populates show details with theme application
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Retrieves show ID from arguments
        if (getArguments() != null) {
            showId = getArguments().getInt("show_id");
        }

        //Initializes database and fetches show details
        dataBaseHelper = new DataBaseHelper(requireContext());
        ShowModel show = dataBaseHelper.getShowById(showId);

        if (show != null) {
            // Retrieves theme preferences
            SharedPreferences prefs = requireContext().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
            int secondaryColor = prefs.getInt("secondary_color", Color.parseColor("#3B2F3B"));
            
            // Derive contrast color for elements inside secondary background
            int contrastColor = isColorDark(secondaryColor) ? Color.WHITE : Color.BLACK;

            //Sets card background color
            CardView cardRoot = (CardView) view;
            cardRoot.setCardBackgroundColor(secondaryColor);

            //Binds UI components
            ImageView imageView = view.findViewById(R.id.expandedShowImage);
            TextView nameView = view.findViewById(R.id.expandedShowName);
            TextView gradeView = view.findViewById(R.id.expandedShowGrade);
            TextView descView = view.findViewById(R.id.expandedShowDescription);
            ChipGroup tagGroup = view.findViewById(R.id.expandedTagGroup);
            TextView editHint = view.findViewById(R.id.tapToEditHint);
            
            //Displays show name and applies color
            nameView.setText(show.getName());
            nameView.setTextColor(contrastColor);
            
            //Displays formatted grade
            gradeView.setText(String.format(java.util.Locale.US, "%.2f/10.00", show.getGrade()));
            gradeView.setTextColor(contrastColor);

            //Displays description if available
            if (show.getDescription() != null && !show.getDescription().isEmpty()) {
                descView.setText(show.getDescription());
                // Muted version of contrast color
                descView.setTextColor(Color.argb(180, Color.red(contrastColor), Color.green(contrastColor), Color.blue(contrastColor)));
                descView.setVisibility(View.VISIBLE);
            } else {
                descView.setVisibility(View.GONE);
            }
            
            //Styles the edit hint
            editHint.setTextColor(contrastColor);

            //Loads show image or placeholder
            if (show.getImagePath() != null && !show.getImagePath().isEmpty()) {
                Glide.with(this).load(show.getImagePath()).into(imageView);
                view.findViewById(R.id.noImageText).setVisibility(View.GONE);
            } else {
                imageView.setImageResource(R.drawable.placeholder_background);
                TextView noImgTxt = view.findViewById(R.id.noImageText);
                noImgTxt.setVisibility(View.VISIBLE);
                noImgTxt.setTextColor(Color.WHITE);
            }

            //Dynamically generates tag chips
            String tags = show.getTags();
            if (tags != null && !tags.isEmpty()) {
                tagGroup.setVisibility(View.VISIBLE);
                String[] tagArray = tags.split(",");
                for (String tag : tagArray) {
                    String trimmed = tag.trim();
                    if (!trimmed.isEmpty()) {
                        Chip chip = new Chip(requireContext());
                        chip.setText(trimmed);
                        chip.setClickable(false);
                        chip.setCheckable(false);
                        
                        // Use contrast color for tag outline and text
                        int oppositeColor = isColorDark(secondaryColor) ? Color.WHITE : Color.BLACK;

                        //Styles the individual chip
                        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
                        chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(oppositeColor));
                        chip.setChipStrokeWidth(3f);
                        chip.setTextColor(oppositeColor);
                        tagGroup.addView(chip);
                    }
                }
            } else {
                tagGroup.setVisibility(View.GONE);
            }
            
            //Sets click listener to navigate to the full show activity
            view.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ShowActivity.class);
                intent.putExtra("id", show.getId());
                intent.putExtra("name", show.getName());
                intent.putExtra("grade", show.getGrade());
                intent.putExtra("description", show.getDescription());
                startActivity(intent);
                dismiss();
            });
        }
    }

    //Helper method to determine if a color is dark
    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    //Configures the dialog size on start
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null && getActivity() != null) {
            //Calculates dynamic width based on screen size
            android.graphics.Rect windowBounds = getActivity().getWindowManager().getCurrentWindowMetrics().getBounds();
            int screenWidth = windowBounds.width();
            int width = (int) (screenWidth * 0.85);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    //Notifies the parent activity on dismissal
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) getActivity()).onDialogClose(dialog);
        }
    }
}