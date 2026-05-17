package com.example.schoolproject.Model;

public class ShowModel {
    private String name;
    private int id;
    private double grade;
    private String description;
    private String tags;
    private String imagePath;

    //Returns the name of the show
    public String getName() {
        return name;
    }

    //Sets the name of the show
    public void setName(String name) {
        this.name = name;
    }

    //Returns the unique ID of the show
    public int getId() {
        return id;
    }

    //Sets the unique ID of the show
    public void setId(int id) {
        this.id = id;
    }

    //Returns the grade of the show
    public double getGrade() {return grade;}

    //Sets the grade of the show
    public void setGrade(double grade) {
        this.grade = grade;
    }

    //Returns the description of the show
    public String getDescription() {
        return description;
    }

    //Sets the description of the show
    public void setDescription(String description) {
        this.description = description;
    }

    //Returns the tags of the show
    public String getTags() {
        return tags;
    }

    //Sets the tags of the show
    public void setTags(String tags) {
        this.tags = tags;
    }

    //Returns the image path of the show
    public String getImagePath() {
        return imagePath;
    }

    //Sets the image path of the show
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
