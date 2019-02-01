package com.example.android.voiceassistantcommands.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.voiceassistantcommands.data.CommandContract.CommandsEntry;

/**
 * Created by vanshika on 02-12-2018.
 * <p>
 * Content Provider for the app
 */

public class CommandProvider extends ContentProvider {

    private CommandDbHelper mDbHelper;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ContentProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int COMMANDS = 100;

    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int COMMAND_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(CommandContract.CONTENT_AUTHORITY, CommandContract.PATH_COMMANDS, COMMANDS);
        sUriMatcher.addURI(CommandContract.CONTENT_AUTHORITY, CommandContract.PATH_COMMANDS + "/#", COMMAND_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new CommandDbHelper(getContext());

        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMANDS:
                cursor = database.query(CommandsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;
            case COMMAND_ID:
                selection = CommandsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(CommandsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMANDS:
                return CommandsEntry.CONTENT_LIST_TYPE;
            case COMMAND_ID:
                return CommandsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMANDS:
                return insertCommand(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertCommand(Uri uri, ContentValues values) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        String command = values.getAsString(CommandsEntry.COLUMN_COMMAND_TEXT);
        Integer assistant = values.getAsInteger(CommandsEntry.COLUMN_ASSISTANT_TYPE);

        if (command == null) {
            throw new IllegalArgumentException("Text for command required");
        }

        if (assistant == null || !CommandsEntry.isValidAssistant(assistant)) {
            throw new IllegalArgumentException("Type of assistant command is being given to is required");
        }

        // Insert the new pet with the given values
        long id = database.insert(CommandsEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Track the number of rows that were deleted
        int rowsDeleted;

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMANDS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(CommandsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COMMAND_ID:
                // Delete a single row given by the ID in the URI
                selection = CommandsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(CommandsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMANDS:
                return updateCommand(uri, contentValues, selection, selectionArgs);
            case COMMAND_ID:
                selection = CommandsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateCommand(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateCommand(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(CommandsEntry.COLUMN_COMMAND_TEXT)) {
            String name = values.getAsString(CommandsEntry.COLUMN_COMMAND_TEXT);
            if (name == null) {
                throw new IllegalArgumentException("Text for command required");
            }
        }

        if (values.containsKey(CommandsEntry.COLUMN_ASSISTANT_TYPE)) {
            Integer assistant = values.getAsInteger(CommandsEntry.COLUMN_ASSISTANT_TYPE);
            if (assistant == null || !CommandsEntry.isValidAssistant(assistant)) {
                throw new IllegalArgumentException("Type of assistant command is being given to is required");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(CommandsEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

}



