package com.example.android.voiceassistantcommands;

import android.app.LoaderManager;

import android.content.CursorLoader;

import android.content.Loader;
import android.content.ContentUris;
import android.database.Cursor;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.android.voiceassistantcommands.data.CommandContract.CommandsEntry;

import java.util.Locale;


public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int COMMAND_LOADER = 17;

    CommandCursorAdapter mCommandCursorAdapter;

    TextToSpeech ttsObject;
    int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        this.setTitle(getResources().getString(R.string.catalog_activity_label));

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        SwipeMenuListView commandListView = (SwipeMenuListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        commandListView.setEmptyView(emptyView);

        mCommandCursorAdapter = new CommandCursorAdapter(this, null);
        commandListView.setAdapter(mCommandCursorAdapter);

        getLoaderManager().initLoader(COMMAND_LOADER, null, this);

        ttsObject = new TextToSpeech(CatalogActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    result = ttsObject.setLanguage(Locale.UK);
                } else {
                    Toast.makeText(getApplicationContext(), "Feature not Supported on your device",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem editItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                editItem.setBackground(R.color.colorAccent);
                // set item width
                editItem.setWidth(178);
                // set item title
                editItem.setIcon(R.drawable.ic_edit);
                // add to menu
                menu.addMenuItem(editItem);
            }
        };

// set creator
        commandListView.setMenuCreator(creator);

        commandListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                Uri currentCommandUri = ContentUris.withAppendedId(CommandsEntry.CONTENT_URI, position + 1);

                intent.setData(currentCommandUri);

                startActivity(intent);

                // false : close the menu; true : not close the menu
                return false;
            }
        });

        commandListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int id, long l) {

                String[] projection = {CommandsEntry.COLUMN_COMMAND_TEXT,
                        CommandsEntry.COLUMN_ASSISTANT_TYPE};

                Uri currentCommandUri = ContentUris.withAppendedId(CommandsEntry.CONTENT_URI, id + 1);

                Cursor cursor = getContentResolver().query(currentCommandUri,
                        projection,
                        null,
                        null,
                        null);

                if (cursor.moveToFirst()) {
                    int commandColumnIndex = cursor.getColumnIndex(CommandsEntry.COLUMN_COMMAND_TEXT);
                    int assistantColumnIndex = cursor.getColumnIndex(CommandsEntry.COLUMN_ASSISTANT_TYPE);

                    String commandText = cursor.getString(commandColumnIndex);
                    String assistantNumberText = cursor.getString(assistantColumnIndex);
                    String assistantText = "No text inputted";
                    int assistantNumber = Integer.parseInt(assistantNumberText);
                    if (assistantNumber == 1) {
                        assistantText = "Alexa";
                    }
                    if (assistantNumber == 2) {
                        assistantText = "Ok Google";
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ttsObject.speak(assistantText, TextToSpeech.QUEUE_ADD, null, null);
                        ttsObject.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
                        ttsObject.speak(commandText, TextToSpeech.QUEUE_ADD, null, null);
                    } else {
                        ttsObject.speak(assistantText, TextToSpeech.QUEUE_ADD, null);
                        ttsObject.playSilence(500, TextToSpeech.QUEUE_ADD, null);
                        ttsObject.speak(commandText, TextToSpeech.QUEUE_ADD, null);
                    }
                }

            }
        });
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                CommandsEntry._ID,
                CommandsEntry.COLUMN_COMMAND_TEXT,
                CommandsEntry.COLUMN_ASSISTANT_TYPE};

        return new CursorLoader(this,
                CommandsEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCommandCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCommandCursorAdapter.swapCursor(null);
    }
}
