package com.example.android.voiceassistantcommands;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.voiceassistantcommands.data.CommandContract;

import java.util.Locale;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

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

    private TextToSpeech ttsObject;
    int result;

    private static final int EXISTING_COMMAND_LOADER = 17;

    private boolean mCommandHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCommandHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentCommandUri = intent.getData();

        if (mCurrentCommandUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_command));

            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_command));

            getSupportLoaderManager().initLoader(EXISTING_COMMAND_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mCommandEditText = (EditText) findViewById(R.id.edit_command);
        mAssistantSpinner = (Spinner) findViewById(R.id.spinner_assistant);

        mCommandEditText.setOnTouchListener(mTouchListener);
        mAssistantSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        ttsObject = new TextToSpeech(EditorActivity.this, new TextToSpeech.OnInitListener() {
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
                saveCommand();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
                // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:

                if (!mCommandHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentCommandUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mCommandHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void saveCommand() {
        String commandString = mCommandEditText.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(CommandContract.CommandsEntry.COLUMN_COMMAND_TEXT, commandString);
        values.put(CommandContract.CommandsEntry.COLUMN_ASSISTANT_TYPE, mAssistant);

        // Show a toast message depending on whether or not the insertion was successful
        if (mCurrentCommandUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Uri newUri = getContentResolver().insert(CommandContract.CommandsEntry.CONTENT_URI, values);

            if (newUri == null) {

                Toast.makeText(this, getString(R.string.editor_insert_command_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_command_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentCommandUri, values, null, null);

            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_command_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_command_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteCommand() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentCommandUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentCommandUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_command_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_command_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    public void textToSpeakButtonClick(View v) {
        textToSpeak();
    }

    public void textToSpeak() {
        if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {

            Toast.makeText(getApplicationContext(), "Feature not Supported on your device",
                    Toast.LENGTH_SHORT).show();
        } else {
            String commandToSpeak = mCommandEditText.getText().toString();
            String assistant = mAssistantSpinner.getSelectedItem().toString();
            String assistantToSpeak;
            if (assistant.equals("Google Home")) {
                assistantToSpeak = "Ok Google";
            } else {
                assistantToSpeak = assistant;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsObject.speak(assistantToSpeak, TextToSpeech.QUEUE_ADD, null, null);
                ttsObject.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
                ttsObject.speak(commandToSpeak, TextToSpeech.QUEUE_ADD, null, null);
            } else {
                ttsObject.speak(assistantToSpeak, TextToSpeech.QUEUE_ADD, null);
                ttsObject.playSilence(500, TextToSpeech.QUEUE_ADD, null);
                ttsObject.speak(commandToSpeak, TextToSpeech.QUEUE_ADD, null);
            }
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {CommandContract.CommandsEntry._ID,
                CommandContract.CommandsEntry.COLUMN_COMMAND_TEXT,
                CommandContract.CommandsEntry.COLUMN_ASSISTANT_TYPE};

        return new CursorLoader(this,
                mCurrentCommandUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {

            int commandColumnIndex = cursor.getColumnIndex(CommandContract.CommandsEntry.COLUMN_COMMAND_TEXT);
            int assistantColumnIndex = cursor.getColumnIndex(CommandContract.CommandsEntry.COLUMN_ASSISTANT_TYPE);

            String commandText = cursor.getString(commandColumnIndex);
            int assistant = cursor.getInt(assistantColumnIndex);

            mCommandEditText.setText(commandText);

            if (assistant == CommandContract.CommandsEntry.ASSISTANT_GOOGLE_HOME) {
                mAssistantSpinner.setSelection(0);
            } else {
                mAssistantSpinner.setSelection(1);
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mCommandEditText.setText("");
        mAssistantSpinner.setSelection(1);

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteCommand();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}