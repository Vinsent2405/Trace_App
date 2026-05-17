package com.example.schoolproject.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.schoolproject.Model.ListModel;
import com.example.schoolproject.Model.ShowModel;
import com.example.schoolproject.Model.TagModel;

import java.util.ArrayList;
import java.util.List;


public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME ="LIST_DATABASE";
    private static final int DATABASE_VERSION = 6;

    // List Table
    private static final String TABLE_NAME ="LIST_TABLE";
    private static final String COL_1 ="ID";
    private static final String COL_2 ="NAME";

    // Show Table
    private static final String SHOW_TABLE ="SHOW_TABLE";
    private static final String SHOW_COL_ID ="ID";
    private static final String SHOW_COL_NAME ="NAME";
    private static final String SHOW_COL_GRADE ="GRADE";
    private static final String SHOW_COL_DESCRIPTION ="DESCRIPTION";
    private static final String SHOW_COL_TAGS ="TAGS";
    private static final String SHOW_COL_IMAGE_PATH ="IMAGE_PATH";
    private static final String SHOW_COL_LIST_ID ="LIST_ID";

    // Tag Table
    public static final String TAG_TABLE = "TAG_TABLE";
    public static final String TAG_COL_ID = "ID";
    public static final String TAG_COL_NAME = "NAME";

    // Show-Tag Junction Table
    public static final String SHOW_TAG_TABLE = "SHOW_TAG_TABLE";
    public static final String SHOW_TAG_COL_SHOW_ID = "SHOW_ID";
    public static final String SHOW_TAG_COL_TAG_ID = "TAG_ID";

    //Constructor for the database helper
    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Initializes the database tables on first creation
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creates the main list table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT)");
        //Creates the show details table with foreign key to lists
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SHOW_TABLE + " (" +
                SHOW_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SHOW_COL_NAME + " TEXT, " +
                SHOW_COL_GRADE + " REAL, " +
                SHOW_COL_DESCRIPTION + " TEXT, " +
                SHOW_COL_TAGS + " TEXT, " +
                SHOW_COL_IMAGE_PATH + " TEXT, " +
                SHOW_COL_LIST_ID + " INTEGER, " +
                "FOREIGN KEY(" + SHOW_COL_LIST_ID + ") REFERENCES " + TABLE_NAME + "(" + COL_1 + ") ON DELETE CASCADE)");

        //Creates the tag table for reusable tags
        String CREATE_TAG_TABLE = "CREATE TABLE " + TAG_TABLE + "(" +
                TAG_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TAG_COL_NAME + " TEXT UNIQUE)";
        db.execSQL(CREATE_TAG_TABLE);

        //Creates the junction table for many to many show tag relationship
        String CREATE_SHOW_TAG_TABLE = "CREATE TABLE " + SHOW_TAG_TABLE + "(" +
                SHOW_TAG_COL_SHOW_ID + " INTEGER, " +
                SHOW_TAG_COL_TAG_ID + " INTEGER, " +
                "PRIMARY KEY (" + SHOW_TAG_COL_SHOW_ID + ", " + SHOW_TAG_COL_TAG_ID + "), " +
                "FOREIGN KEY(" + SHOW_TAG_COL_SHOW_ID + ") REFERENCES " + SHOW_TABLE + "(" + SHOW_COL_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + SHOW_TAG_COL_TAG_ID + ") REFERENCES " + TAG_TABLE + "(" + TAG_COL_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_SHOW_TAG_TABLE);
    }

    //Handles database schema upgrades across versions
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Upgrade logic for version 2
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + SHOW_TABLE + " (" +
                    SHOW_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SHOW_COL_NAME + " TEXT, " +
                    SHOW_COL_GRADE + " REAL, " +
                    SHOW_COL_DESCRIPTION + " TEXT, " +
                    SHOW_COL_LIST_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SHOW_COL_LIST_ID + ") REFERENCES " + TABLE_NAME + "(" + COL_1 + ") ON DELETE CASCADE)");
        }
        //Upgrade logic for version 3
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + SHOW_TABLE + " ADD COLUMN " + SHOW_COL_DESCRIPTION + " TEXT");
        }
        //Upgrade logic for version 4
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + SHOW_TABLE + " ADD COLUMN " + SHOW_COL_TAGS + " TEXT");
        }
        //Upgrade logic for version 5
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE " + TAG_TABLE + "(" + TAG_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TAG_COL_NAME + " TEXT UNIQUE)");
            db.execSQL("CREATE TABLE " + SHOW_TAG_TABLE + "(" + SHOW_TAG_COL_SHOW_ID + " INTEGER, " + SHOW_TAG_COL_TAG_ID + " INTEGER, PRIMARY KEY (" + SHOW_TAG_COL_SHOW_ID + ", " + SHOW_TAG_COL_TAG_ID + "), FOREIGN KEY(" + SHOW_TAG_COL_SHOW_ID + ") REFERENCES " + SHOW_TABLE + "(" + SHOW_COL_ID + ") ON DELETE CASCADE, FOREIGN KEY(" + SHOW_TAG_COL_TAG_ID + ") REFERENCES " + TAG_TABLE + "(" + TAG_COL_ID + ") ON DELETE CASCADE)");
        }
        //Upgrade logic for version 6
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + SHOW_TABLE + " ADD COLUMN " + SHOW_COL_IMAGE_PATH + " TEXT");
        }
    }

    //Enables foreign key constraints when database is opened
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    //Recreates database from scratch on downgrade
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SHOW_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //Inserts a new list into the database
    public void insertList(ListModel listModel){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, listModel.getName());

        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    //Updates an existing list's name
    public void updateList(ListModel listModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, listModel.getName());

        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{String.valueOf(listModel.getId())});
        db.close();
    }

    //Deletes a list and all its associated shows
    public void deleteList(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "ID = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    //Returns the count of shows within a specific list
    public int getShowsCountForList(int listId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + SHOW_TABLE + " WHERE " + SHOW_COL_LIST_ID + " = ?", new String[]{String.valueOf(listId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    //Retrieves all lists stored in the database
    public List<ListModel> getAllLists() {
        List<ListModel> listModels = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        db.beginTransaction();
        try (Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null)) {
            //Iterates through the cursor and populates the list
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COL_1);
                int nameIndex = cursor.getColumnIndex(COL_2);
                do {
                    ListModel listModel = new ListModel();
                    if (idIndex != -1) listModel.setId(cursor.getInt(idIndex));
                    if (nameIndex != -1) listModel.setName(cursor.getString(nameIndex));
                    listModels.add(listModel);
                } while (cursor.moveToNext());
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return listModels;
    }

    //Inserts a new show entry associated with a list
    public long insertShow(ShowModel showModel, int listId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //Populates values from the model
        contentValues.put(SHOW_COL_NAME, showModel.getName());
        contentValues.put(SHOW_COL_GRADE, showModel.getGrade());
        contentValues.put(SHOW_COL_DESCRIPTION, showModel.getDescription());
        contentValues.put(SHOW_COL_TAGS, showModel.getTags());
        contentValues.put(SHOW_COL_IMAGE_PATH, showModel.getImagePath());
        contentValues.put(SHOW_COL_LIST_ID, listId);

        long id = db.insert(SHOW_TABLE, null, contentValues);
        db.close();
        return id;
    }

    //Updates the details of an existing show
    public void updateShow(ShowModel showModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //Maps model data to database columns
        contentValues.put(SHOW_COL_NAME, showModel.getName());
        contentValues.put(SHOW_COL_GRADE, showModel.getGrade());
        contentValues.put(SHOW_COL_DESCRIPTION, showModel.getDescription());
        contentValues.put(SHOW_COL_TAGS, showModel.getTags());
        contentValues.put(SHOW_COL_IMAGE_PATH, showModel.getImagePath());

        db.update(SHOW_TABLE, contentValues, SHOW_COL_ID + " = ?", new String[]{String.valueOf(showModel.getId())});
        db.close();
    }

    //Deletes a show by its unique identifier
    public void deleteShow(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SHOW_TABLE, SHOW_COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    //Retrieves a single show model by its ID
    public ShowModel getShowById(int showId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            //Queries the show table for a specific ID
            cursor = db.query(SHOW_TABLE, null, SHOW_COL_ID + " = ?", new String[]{String.valueOf(showId)}, null, null, null);
            if (cursor.moveToFirst()) {
                ShowModel showModel = new ShowModel();
                //Extracts values from the result cursor
                showModel.setId(cursor.getInt(cursor.getColumnIndexOrThrow(SHOW_COL_ID)));
                showModel.setName(cursor.getString(cursor.getColumnIndexOrThrow(SHOW_COL_NAME)));
                showModel.setGrade(cursor.getDouble(cursor.getColumnIndexOrThrow(SHOW_COL_GRADE)));
                showModel.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(SHOW_COL_DESCRIPTION)));
                showModel.setTags(cursor.getString(cursor.getColumnIndexOrThrow(SHOW_COL_TAGS)));
                showModel.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(SHOW_COL_IMAGE_PATH)));
                return showModel;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    //Retrieves all shows belonging to a list including concatenated tags
    public List<ShowModel> getShowsForList(int listId) {
        List<ShowModel> showModels = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            //Performs a complex join to aggregate tags for each show
            String query = "SELECT s.*, GROUP_CONCAT(t." + TAG_COL_NAME + ", ', ') as concatenated_tags " +
                    "FROM " + SHOW_TABLE + " s " +
                    "LEFT JOIN " + SHOW_TAG_TABLE + " st ON s." + SHOW_COL_ID + " = st." + SHOW_TAG_COL_SHOW_ID + " " +
                    "LEFT JOIN " + TAG_TABLE + " t ON st." + SHOW_TAG_COL_TAG_ID + " = t." + TAG_COL_ID + " " +
                    "WHERE s." + SHOW_COL_LIST_ID + " = ? " +
                    "GROUP BY s." + SHOW_COL_ID + " " +
                    "ORDER BY s." + SHOW_COL_GRADE + " DESC";
            
            cursor = db.rawQuery(query, new String[]{String.valueOf(listId)});
            
            //Iterates through results and constructs show models
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(SHOW_COL_ID);
                int nameIndex = cursor.getColumnIndex(SHOW_COL_NAME);
                int gradeIndex = cursor.getColumnIndex(SHOW_COL_GRADE);
                int descIndex = cursor.getColumnIndex(SHOW_COL_DESCRIPTION);
                int tagsIndex = cursor.getColumnIndex("concatenated_tags");
                int imagePathIndex = cursor.getColumnIndex(SHOW_COL_IMAGE_PATH);

                do {
                    ShowModel showModel = new ShowModel();
                    if (idIndex != -1) showModel.setId(cursor.getInt(idIndex));
                    if (nameIndex != -1) showModel.setName(cursor.getString(nameIndex));
                    if (gradeIndex != -1) showModel.setGrade(cursor.getDouble(gradeIndex));
                    if (descIndex != -1) showModel.setDescription(cursor.getString(descIndex));
                    if (tagsIndex != -1) showModel.setTags(cursor.getString(tagsIndex));
                    if (imagePathIndex != -1) showModel.setImagePath(cursor.getString(imagePathIndex));
                    showModels.add(showModel);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return showModels;
    }

    //Inserts a new tag or ignores if it already exists
    public void insertTag(String tagName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TAG_COL_NAME, tagName);
        db.insertWithOnConflict(TAG_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    //Retrieves all unique tags from the database
    public List<TagModel> getAllTags() {
        List<TagModel> tags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        //Selects all columns from the tag table
        Cursor cursor = db.rawQuery("SELECT * FROM " + TAG_TABLE, null);
        if (cursor.moveToFirst()) {
            do {
                TagModel tag = new TagModel();
                tag.setId(cursor.getInt(cursor.getColumnIndexOrThrow(TAG_COL_ID)));
                tag.setName(cursor.getString(cursor.getColumnIndexOrThrow(TAG_COL_NAME)));
                tags.add(tag);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tags;
    }

    //Deletes a tag by its ID
    public void deleteTag(int tagId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TAG_TABLE, TAG_COL_ID + " = ?", new String[]{String.valueOf(tagId)});
    }

    //Updates the set of tags associated with a show
    public void updateTagToShow(int showId, List<Integer> tagIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            //Clears existing tags for the show
            db.delete(SHOW_TAG_TABLE, SHOW_TAG_COL_SHOW_ID + " = ?", new String[]{String.valueOf(showId)});
            //Inserts the new set of tags
            for (int tagId : tagIds) {
                ContentValues cv = new ContentValues();
                cv.put(SHOW_TAG_COL_SHOW_ID, showId);
                cv.put(SHOW_TAG_COL_TAG_ID, tagId);
                db.insert(SHOW_TAG_TABLE, null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    //Retrieves all tags linked to a specific show
    public List<TagModel> getTagsForShow(int showId) {
        List<TagModel> tags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        //Joins tag table with junction table
        String query = "SELECT t.* FROM " + TAG_TABLE + " t " +
                "JOIN " + SHOW_TAG_TABLE + " st ON t." + TAG_COL_ID + " = st." + SHOW_TAG_COL_TAG_ID + " " +
                "WHERE st." + SHOW_TAG_COL_SHOW_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(showId)});
        if (cursor.moveToFirst()) {
            do {
                TagModel tag = new TagModel();
                tag.setId(cursor.getInt(cursor.getColumnIndexOrThrow(TAG_COL_ID)));
                tag.setName(cursor.getString(cursor.getColumnIndexOrThrow(TAG_COL_NAME)));
                tags.add(tag);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tags;
    }




    //Calculates the user level based on total shows
    public int calculateLevel(int totalShows) {
        // Base Case: If less than 10 shows, user is Level 1
        if (totalShows < 10) {
            return 1;
        }
        // Recursive Step: Reduce count by 10 and add 1 level
        return 1 + calculateLevel(totalShows - 10);
    }


    //Calculates shows left until the next level milestone
    public int showsLeftToNextLevel(int totalShows) {
        // Base Case: Remaining shows to hit the 10-show milestone
        if (totalShows < 10) {
            return 10 - totalShows;
        }
        // Recursive Step: Continue checking the next milestone block
        return showsLeftToNextLevel(totalShows - 10);
    }

    //Returns the total number of shows across all lists
    public int getTotalShowsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        //Executes a count query on the show table
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + SHOW_TABLE, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
