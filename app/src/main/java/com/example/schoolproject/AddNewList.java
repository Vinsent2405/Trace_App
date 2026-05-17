package com.example.schoolproject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.schoolproject.Model.ListModel;
import com.example.schoolproject.Utils.DataBaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddNewList extends BottomSheetDialogFragment {
    public static final String TAG = "AddNewList";
    private EditText mEditText;
    private Button mSaveButton;
    private DataBaseHelper dataBaseHelper;

    //Returns a new instance of the fragment
    public static AddNewList newInstance(){
        return new AddNewList();
    }

    //Inflates the layout for the bottom sheet
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_new_list, container, false);
    }

    //Initializes UI and handles list creation logic
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Binds UI components
        mEditText = view.findViewById(R.id.editText);
        mSaveButton = view.findViewById(R.id.submit);

        //Initializes database helper
        dataBaseHelper = new DataBaseHelper(getActivity());

        // Retrieves colors from shared preferences and applies theme
        SharedPreferences prefs = requireContext().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        int primaryColor = prefs.getInt("primary_color", Color.parseColor("#241E24"));
        int secondaryColor = prefs.getInt("secondary_color", Color.parseColor("#3B2F3B"));
        int userTextColor = prefs.getInt("text_color", Color.WHITE);
        
        //Calculates hint color with transparency
        int hintColor = Color.argb(150, Color.red(userTextColor), Color.green(userTextColor), Color.blue(userTextColor));
        
        //Sets background and element colors
        view.setBackgroundColor(primaryColor);
        mEditText.setTextColor(userTextColor);
        mEditText.setHintTextColor(hintColor);
        
        mSaveButton.setBackgroundColor(secondaryColor);
        mSaveButton.setTextColor(userTextColor);

        //Handles edit vs create mode
        boolean isUpdate = false;
        Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String listName = bundle.getString("name");
            mEditText.setText(listName);
            //Disables button initially if text is present
            if (listName != null && !listName.isEmpty()) {
                mSaveButton.setEnabled(false);
                mSaveButton.setAlpha(0.5f);
            }
        }

        //Monitors text changes to toggle save button state
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Checks if input is empty
                if(s.toString().isEmpty()){
                    mSaveButton.setEnabled(false);
                    mSaveButton.setAlpha(0.5f);
                }else{
                    mSaveButton.setEnabled(true);
                    mSaveButton.setAlpha(1.0f);
                }
            }
        });
        
        //Saves list data on click
        boolean finalIsUpdate = isUpdate;
        mSaveButton.setOnClickListener(v -> {
            String text = mEditText.getText().toString().trim();
            //Final validation
            if (text.isEmpty()) {
                mEditText.setError("Name cannot be empty");
                return;
            }
            ListModel listModel = new ListModel();
            listModel.setName(text);
            //Updates or inserts based on mode
            if (finalIsUpdate) {
                listModel.setId(bundle.getInt("id"));
                dataBaseHelper.updateList(listModel);
            } else {
                dataBaseHelper.insertList(listModel);
            }
            dismiss();
        });
    }

    //Notifies listener when dialog is dismissed
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }
}
