package com.example.android.voiceassistantcommands;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.voiceassistantcommands.data.CommandContract.CommandsEntry;

/**
 * Created by vanshika on 03-12-2018.
 */

public class CommandCursorAdapter extends CursorAdapter {

    public CommandCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    public String mAssistant;

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView commandTextView = (TextView) view.findViewById(R.id.text_view_command_text);
        TextView assistantTextView = (TextView) view.findViewById(R.id.text_view_assistant_name);

        int commandColumnIndex = cursor.getColumnIndex(CommandsEntry.COLUMN_COMMAND_TEXT);
        int assistantColumnIndex = cursor.getColumnIndex(CommandsEntry.COLUMN_ASSISTANT_TYPE);

        String commandText = cursor.getString(commandColumnIndex);
        String assistantNumberText = cursor.getString(assistantColumnIndex);
        String assistantText = "No text inputted";
        int assistantNumber = Integer.parseInt(assistantNumberText);
        if( assistantNumber == 1){
            assistantText = "Alexa";
        } if ( assistantNumber == 2){
            assistantText = "Google Home";
        }

        commandTextView.setText(commandText);
        assistantTextView.setText(assistantText);
    }
}
