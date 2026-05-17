package com.example.schoolproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolproject.Adapter.ShowsAdapter;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ShowsTouchHelper extends ItemTouchHelper.SimpleCallback {

    private ShowsAdapter adapter;

    //Constructor for the shows touch helper
    public ShowsTouchHelper(ShowsAdapter adapter) {
        //Enables left and right swipe interactions
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    //Disable move interactions
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    //Handles swipe logic for show items (delete or edit)
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        //Right swipe triggers deletion
        if (direction == ItemTouchHelper.RIGHT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setTitle("Delete Show");
            builder.setMessage("Are you sure you want to delete this Show?");
            //Handles confirm delete click
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // Second confirmation for destructive action
                AlertDialog.Builder secondBuilder = new AlertDialog.Builder(adapter.getContext());
                secondBuilder.setTitle("Pretty sure?");
                secondBuilder.setMessage("This action cannot be undone. Are you absolutely sure?");
                //Final delete handler
                secondBuilder.setPositiveButton("Delete Forever", (secondDialog, secondWhich) -> adapter.deleteShow(position));
                //Restores item on second cancel
                secondBuilder.setNegativeButton("Hell nah", (secondDialog, secondWhich) -> adapter.notifyItemChanged(position));
                secondBuilder.setOnCancelListener(secondDialog -> adapter.notifyItemChanged(position));
                secondBuilder.show();
            });
            //Restores item on first cancel
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> adapter.notifyItemChanged(position));
            builder.setOnCancelListener(dialog -> adapter.notifyItemChanged(position));
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            // Swipe Left triggers full edit activity
            adapter.openShowActivity(position);
        }
    }

    //Draws custom background colors and icons during swipe
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        //Uses swipe decorator for visual feedback
        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeRightBackgroundColor(ContextCompat.getColor(adapter.getContext(), R.color.red))
                .addSwipeRightActionIcon(R.drawable.ic_delete)
                .addSwipeLeftBackgroundColor(ContextCompat.getColor(adapter.getContext(), R.color.green))
                .addSwipeLeftActionIcon(R.drawable.ic_edit)
                .create()
                .decorate();

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
