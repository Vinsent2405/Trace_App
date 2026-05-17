package com.example.schoolproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schoolproject.Adapter.ListsAdapter;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class RecycleViewTouchHelper extends ItemTouchHelper.SimpleCallback {

    ListsAdapter adapter;

    //Constructor for the touch helper
    public RecycleViewTouchHelper(ListsAdapter adapter) {
        //Configures left and right swipe directions
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    //Disable move actions
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    //Handles swipe logic for list items
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        //Triggers delete dialog on right swipe
        if (direction == ItemTouchHelper.RIGHT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setTitle("Delete List");
            builder.setMessage("Are you sure you want to delete this List?");
            //Handles confirm deletion
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Second confirmation for destructive action
                    AlertDialog.Builder secondBuilder = new AlertDialog.Builder(adapter.getContext());
                    secondBuilder.setTitle("Final Warning");
                    secondBuilder.setMessage("Deleting this list will also delete all items inside it. Are you absolutely sure?");
                    //Final delete action
                    secondBuilder.setPositiveButton("Delete Everything", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface secondDialog, int secondWhich) {
                            adapter.deleteList(position);
                        }
                    });
                    //Restore item on cancel
                    secondBuilder.setNegativeButton(android.R.string.cancel, (secondDialog, secondWhich) -> adapter.notifyItemChanged(position));
                    secondBuilder.setOnCancelListener(secondDialog -> adapter.notifyItemChanged(position));
                    secondBuilder.show();
                }

            });
            //Restore item on first cancel
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> adapter.notifyItemChanged(position));
            builder.setOnCancelListener(dialog -> adapter.notifyItemChanged(position));
            AlertDialog dialog = builder.create();
            dialog.show();
        }else{
            //Triggers edit mode on left swipe
            adapter.editList(position);
        }
    }

    //Draws custom background and icons during swipe
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
