package com.example.schoolproject.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolproject.AddNewList;
import com.example.schoolproject.MainActivity;
import com.example.schoolproject.Model.ListModel;
import com.example.schoolproject.R;
import com.example.schoolproject.RankActivity;
import com.example.schoolproject.Utils.DataBaseHelper;

import java.util.List;

public class ListsAdapter extends RecyclerView.Adapter<ListsAdapter.ViewHolder> {

    private List<ListModel> listModels= new java.util.ArrayList<>();
    private final MainActivity mainActivity;
    private final DataBaseHelper dataBaseHelper;


    //Standard constructor for the adapter
    public ListsAdapter(DataBaseHelper dataBaseHelper,  MainActivity mainActivity) {
        this.dataBaseHelper = dataBaseHelper;
        this.mainActivity = mainActivity;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return listModels.get(position).getId();
    }


    //Inflates the item layout for individual lists
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
        return new ViewHolder(view);
    }

    //Binds data to the list items and applies theme
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Retrieves the list model for current position
        final ListModel listModel = listModels.get(position);
        holder.textViewTitle.setText(listModel.getName());

        //Calculates and displays the number of items in the list
        int count = dataBaseHelper.getShowsCountForList(listModel.getId());
        holder.textViewCount.setText(String.format(java.util.Locale.getDefault(), "%d %s", count, (count == 1 ? "item" : "items")));

        // Retrieves colors from shared preferences and applies theme
        SharedPreferences prefs = mainActivity.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        int secondaryColor = prefs.getInt("secondary_color", Color.parseColor("#3B2F3B"));
        int userTextColor = prefs.getInt("text_color", Color.WHITE);
        
        //Sets background and text colors based on theme
        holder.cardView.setCardBackgroundColor(secondaryColor);
        holder.textViewTitle.setTextColor(userTextColor);
        // Muted version of user text for count
        int mutedColor = Color.argb(180, Color.red(userTextColor), Color.green(userTextColor), Color.blue(userTextColor));
        holder.textViewCount.setTextColor(mutedColor);

        //Sets click listener to navigate to the rank activity
        holder.relativeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RankActivity.class);
            intent.putExtra("list_name", listModel.getName());
            intent.putExtra("list_id", listModel.getId());
            getContext().startActivity(intent);
        });
    }

    //Returns the context associated with main activity
    public Context getContext() {
        return mainActivity;
    }

    //Updates the list models and refreshes the adapter
    public void setListModels(List<ListModel> listModels) {
        this.listModels = listModels;
        notifyDataSetChanged();
    }

    //Returns the total number of items in the list
    @Override
    public int getItemCount() {
        return listModels.size();
    }

    //Deletes a list from the database and updates the recycler view
    public void deleteList(int position) {
        ListModel listModel = listModels.get(position);
        dataBaseHelper.deleteList(listModel.getId());

        listModels.remove(position);
        notifyItemRemoved(position);
    }

    //Opens the edit list dialog for the selected item
    public void editList(int position) {
        ListModel listModel = listModels.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", listModel.getId());
        bundle.putString("name", listModel.getName());

        AddNewList addNewList = new AddNewList();
        addNewList.setArguments(bundle);
        addNewList.show(mainActivity.getSupportFragmentManager(), addNewList.getTag());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewCount;
        RelativeLayout relativeLayout;
        CardView cardView;

        public ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.User_Name);
            textViewCount = view.findViewById(R.id.itemCount);
            relativeLayout = view.findViewById(R.id.relative_layout);
            cardView = view.findViewById(R.id.cardView);
        }
    }
}