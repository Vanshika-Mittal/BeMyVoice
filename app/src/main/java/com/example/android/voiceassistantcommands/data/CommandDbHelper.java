package com.example.android.voiceassistantcommands.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.voiceassistantcommands.data.CommandContract.CommandsEntry;

/**
 * Created by vanshika on 29-11-2018.
 */

public class CommandDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "voiceCommands.db";

    private static final int DATABASE_VERSION = 1;

    public CommandDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_COMMANDS_TABLE = "CREATE TABLE " + CommandsEntry.TABLE_NAME + " ( "
                + CommandsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CommandsEntry.COLUMN_COMMAND_TEXT + " TEXT NOT NULL, "
                + CommandsEntry.COLUMN_ASSISTANT_TYPE + " INTEGER NOT NULL DEFAULT 2 )";

        db.execSQL(SQL_CREATE_COMMANDS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
