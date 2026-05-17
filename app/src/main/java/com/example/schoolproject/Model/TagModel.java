package com.example.schoolproject.Model;

public class TagModel {
    private int id;
    private String name;
    private boolean isSelected;

    //Default constructor for the tag model
    public TagModel() {}

    //Constructor with ID and name parameters
    public TagModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    //Returns the unique ID of the tag
    public int getId() {
        return id;
    }

    //Sets the unique ID of the tag
    public void setId(int id) {
        this.id = id;
    }

    //Returns the name of the tag
    public String getName() {
        return name;
    }

    //Sets the name of the tag
    public void setName(String name) {
        this.name = name;
    }

    //Returns whether the tag is currently selected
    public boolean isSelected() {
        return isSelected;
    }

    //Sets the selection state of the tag
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
