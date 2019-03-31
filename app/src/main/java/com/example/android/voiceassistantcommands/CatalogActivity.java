package com.example.android.voiceassistantcommands;

import android.app.LoaderManager;

import android.content.CursorLoader;

import android.content.Loader;
import android.content.ContentUris;
import android.content.res.Resources;
import android.database.Cursor;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.example.android.voiceassistantcommands.data.CommandContract.CommandsEntry;

import java.util.Locale;


public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int COMMAND_LOADER = 17;

    CommandCursorAdapter mCommandCursorAdapter;

    TextToSpeech ttsObject;
    int result;
    private AudioManager mAudioManager;
    private int media_current_volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Get the audio manager instance
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        media_current_volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

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

        this.setTitle(getResources().getString(R.string.catalog_activity_label));

        // Setup FAB to open EditorActivity
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView commandListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        commandListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCommandCursorAdapter = new CommandCursorAdapter(this, null);
        commandListView.setAdapter(mCommandCursorAdapter);

        getLoaderManager().initLoader(COMMAND_LOADER, null, this);

        commandListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long position) {

                String[] projection = {CommandsEntry.COLUMN_COMMAND_TEXT,
                        CommandsEntry.COLUMN_ASSISTANT_TYPE};

                Uri currentCommandUri = ContentUris.withAppendedId(CommandsEntry.CONTENT_URI, position);

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
                    String assistantText = "";
                    int assistantNumber = Integer.parseInt(assistantNumberText);
                    if (assistantNumber == 1) {
                        assistantText = "Alexa";
                    }
                    if (assistantNumber == 2) {
                        assistantText = "Ok Google";
                    }

                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, // Stream type
                            mAudioManager.getStreamMaxVolume(mAudioManager.STREAM_MUSIC), // Index
                            AudioManager.FLAG_SHOW_UI);

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

        commandListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> adapterView, View v, int index, long position) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                Uri currentCommandUri = ContentUris.withAppendedId(CommandsEntry.CONTENT_URI, position );

                intent.setData(currentCommandUri);

                startActivity(intent);
                return false;
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

    @Override
    protected void onStop() {
        super.onStop();
        ttsObject.stop();
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                media_current_volume, AudioManager.FLAG_PLAY_SOUND);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ttsObject.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsObject.stop();
        ttsObject.shutdown();
    }
}

