package com.example.schoolproject.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolproject.Model.TagModel;
import com.example.schoolproject.R;

import java.util.ArrayList;
import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {

    private List<TagModel> tagList = new ArrayList<>();
    private OnTagClickListener listener;
    private Context context;

    //Interface for tag interaction events
    public interface OnTagClickListener {
        void onTagClick(TagModel tag);
        void onTagLongClick(TagModel tag);
    }

    //Constructor for the tags adapter
    public TagsAdapter(Context context, OnTagClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    //Inflates the tag item layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_layout, parent, false);
        return new ViewHolder(view);
    }

    //Binds tag data and sets click listeners
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Retrieves the tag for current position
        TagModel tag = tagList.get(position);
        holder.tagName.setText(tag.getName());

        // Retrieves colors from shared preferences and applies theme
        SharedPreferences prefs = context.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        int secondaryColor = prefs.getInt("secondary_color", Color.parseColor("#3B2F3B"));
        int userTextColor = prefs.getInt("text_color", Color.WHITE);
        
        //Sets background and text colors
        holder.cardView.setCardBackgroundColor(secondaryColor);
        holder.tagName.setTextColor(userTextColor);

        //Sets single click interaction
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTagClick(tag);
            }
        });

        //Sets long click interaction for deletion
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTagLongClick(tag);
            }
            return true;
        });
    }

    //Helper method to determine color darkness
    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    //Returns the total number of items in the tag list
    @Override
    public int getItemCount() {
        return tagList.size();
    }

    //Updates the tag dataset and refreshes the adapter
    public void setTagList(List<TagModel> tagList) {
        this.tagList = tagList;
        notifyDataSetChanged();
    }

    //ViewHolder class for tag layout components
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tagName;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //Binds UI components
            tagName = itemView.findViewById(R.id.tagNameText);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
