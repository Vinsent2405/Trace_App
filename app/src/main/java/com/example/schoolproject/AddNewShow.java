package com.example.schoolproject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.schoolproject.Model.ShowModel;
import com.example.schoolproject.Utils.DataBaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddNewShow extends BottomSheetDialogFragment {
    public static final String TAG = "AddNewShow";
    private EditText mEditTextName;
    private EditText mEditTextGrade;
    private SimpleRatingBar mRatingBar;
    private Button mSaveButton;
    private DataBaseHelper dataBaseHelper;
    private int listId = -1;

    //Returns a new instance of the fragment with list ID as argument
    public static AddNewShow newInstance(int listId) {
        AddNewShow fragment = new AddNewShow();
        Bundle args = new Bundle();
        args.putInt("list_id", listId);
        fragment.setArguments(args);
        return fragment;
    }

    //Inflates the layout for the bottom sheet dialog
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_new_show, container, false);
    }

    //Initializes views and handles theme/data setup
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Binds UI components
        mEditTextName = view.findViewById(R.id.editTextShowName);
        mEditTextGrade = view.findViewById(R.id.editTextShowGrade);
        mRatingBar = view.findViewById(R.id.ratingBar);
        mSaveButton = view.findViewById(R.id.submitShow);

        //Initializes database helper
        dataBaseHelper = new DataBaseHelper(getActivity());

        // Retrieves colors from shared preferences and applies theme
        SharedPreferences prefs = requireContext().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        int primaryColor = prefs.getInt("primary_color", Color.parseColor("#241E24"));
        int secondaryColor = prefs.getInt("secondary_color", Color.parseColor("#3B2F3B"));
        int userTextColor = prefs.getInt("text_color", Color.WHITE);
        
        //Calculates hint color with transparency
        int hintColor = Color.argb(150, Color.red(userTextColor), Color.green(userTextColor), Color.blue(userTextColor));
        
        //Sets component colors
        view.setBackgroundColor(primaryColor);
        mEditTextName.setTextColor(userTextColor);
        mEditTextName.setHintTextColor(hintColor);
        mEditTextGrade.setTextColor(userTextColor);
        mEditTextGrade.setHintTextColor(hintColor);
        
        mRatingBar.setFillColor(userTextColor);
        mRatingBar.setBorderColor(userTextColor);
        
        mSaveButton.setBackgroundColor(secondaryColor);
        mSaveButton.setTextColor(userTextColor);

        //Handles update vs insert mode
        boolean isUpdate = false;
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            listId = bundle.getInt("list_id");

            if (bundle.containsKey("id")) {
                isUpdate = true;
                //Populates fields for editing
                mEditTextName.setText(bundle.getString("name"));
                double grade = bundle.getDouble("grade");
                mEditTextGrade.setText(String.format(java.util.Locale.US, "%.2f", grade));
                mRatingBar.setRating((float) grade / 2f);
            }
        }

        //Handles input validation and rating bar sync
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = mEditTextName.getText().toString().trim();
                String gradeStr = mEditTextGrade.getText().toString().trim();

                boolean isValidGrade = false;
                if (!gradeStr.isEmpty()) {
                    try {
                        //Parses and validates grade
                        double grade = Double.parseDouble(gradeStr);
                        if (grade >= 0 && grade <= 10) {
                            mRatingBar.setRating((float) grade / 2f);
                            isValidGrade = true;
                            mEditTextGrade.setError(null);
                        } else {
                            mEditTextGrade.setError("Grade must be between 0.0 and 10.0");
                            mRatingBar.setRating(0f);
                        }
                    } catch (NumberFormatException e) {
                        mEditTextGrade.setError("Invalid number");
                        mRatingBar.setRating(0f);
                    }
                } else {
                    mRatingBar.setRating(0f);
                }

                //Updates save button state
                if (name.isEmpty() || gradeStr.isEmpty() || !isValidGrade) {
                    mSaveButton.setEnabled(false);
                    mSaveButton.setAlpha(0.5f);
                } else {
                    mSaveButton.setEnabled(true);
                    mSaveButton.setAlpha(1.0f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        //Registers text watchers
        mEditTextName.addTextChangedListener(textWatcher);
        mEditTextGrade.addTextChangedListener(textWatcher);

        //Updates grade field when rating bar is touched
        mRatingBar.setOnRatingBarChangeListener((simpleRatingBar, rating, fromUser) -> {
            if (fromUser) {
                mEditTextGrade.setText(String.format(java.util.Locale.US, "%.2f", rating * 2f));
            }
        });

        //Saves show data on button click
        boolean finalIsUpdate = isUpdate;
        mSaveButton.setOnClickListener(v -> {
            String name = mEditTextName.getText().toString().trim();
            String gradeStr = mEditTextGrade.getText().toString().trim();

            //Final validation
            if (name.isEmpty()) {
                mEditTextName.setError("Name cannot be empty");
                return;
            }
            if (gradeStr.isEmpty()) {
                mEditTextGrade.setError("Grade cannot be empty");
                return;
            }

            double grade;
            try {
                grade = Double.parseDouble(gradeStr);
            } catch (NumberFormatException e) {
                mEditTextGrade.setError("Invalid grade");
                return;
            }

            //Updates or inserts show record
            ShowModel showModel = new ShowModel();
            showModel.setName(name);
            showModel.setGrade(grade);

            if (finalIsUpdate && getArguments() != null) {
                int id = getArguments().getInt("id");
                showModel.setId(id);
                dataBaseHelper.updateShow(showModel);
                dismiss();
            } else {
                long newId = dataBaseHelper.insertShow(showModel, listId);
                
                // Open ShowActivity for the newly created show
                if (newId != -1) {
                    Intent intent = new Intent(getContext(), ShowActivity.class);
                    intent.putExtra("id", (int) newId);
                    intent.putExtra("name", name);
                    intent.putExtra("grade", grade);
                    intent.putExtra("description", ""); // New shows start with no description
                    startActivity(intent);
                }
                dismiss();
            }
        });
    }

    //Notifies parent activity when dialog is dismissed
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }
}
