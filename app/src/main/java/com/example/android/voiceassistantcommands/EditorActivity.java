package com.example.android.voiceassistantcommands;


import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.voiceassistantcommands.data.CommandContract;


public class EditorActivity extends AppCompatActivity {

    /**
     * EditText field to enter the command to be spoken
     */
    private EditText mCommandEditText;

    /**
     * EditText field to enter the type of assistant
     */
    private Spinner mAssistantSpinner;

    private int mAssistant = 0;

    private Uri mCurrentCommandUri;

    private static final int EXISTING_COMMAND_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentCommandUri = intent.getData();

        if (mCurrentCommandUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_command));
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_command));
        }

        // Find all relevant views that we will need to read user input from
        mCommandEditText = (EditText) findViewById(R.id.edit_command);
        mAssistantSpinner = (Spinner) findViewById(R.id.spinner_assistant);

        setupSpinner();

    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter assistantSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_assistant_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        assistantSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mAssistantSpinner.setAdapter(assistantSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mAssistantSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.command_alexa))) {
                        mAssistant = 1; // Alexa
                    } else if (selection.equals(getString(R.string.command_google_home))) {
                        mAssistant = 2; // Google Home
                    } else {
                        Toast.makeText(EditorActivity.this, "You must select an assistant", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mAssistant = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                insertCommand();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertCommand() {
        String commandString = mCommandEditText.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(CommandContract.CommandsEntry.COLUMN_COMMAND_TEXT, commandString);
        values.put(CommandContract.CommandsEntry.COLUMN_ASSISTANT_TYPE, mAssistant);

        Uri newUri = getContentResolver().insert(CommandContract.CommandsEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_command_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_command_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }
}