package com.example.android.voiceassistantcommands.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static android.text.style.TtsSpan.GENDER_FEMALE;
import static android.text.style.TtsSpan.GENDER_MALE;

/**
 * Created by vanshika on 29-11-2018.
 */

public final class CommandContract {

    private CommandContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.android.voiceassistantcommands";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_COMMANDS = "commands";

    public static abstract class CommandsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_COMMANDS);

        /** Name of database table for commands */
        public final static String TABLE_NAME = "commands";

        public final static String _ID = BaseColumns._ID;

        /**
         * Command to be spoken
         *
         * Type: TEXT NOT NULL
         */
        public final static String COLUMN_COMMAND_TEXT ="command_text";

        /**
         * Type of assistant command is being given to
         *
         * The only possible values are {@link #ASSISTANT_GOOGLE_HOME}, {@link #ASSISTANT_ALEXA},
         *
         * Type: INTEGER NOT NULL
         */
        public final static String COLUMN_ASSISTANT_TYPE = "assistant_type";

        public static final int ASSISTANT_ALEXA = 1;
        public static final int ASSISTANT_GOOGLE_HOME = 2;

        public static boolean isValidAssistant(int assistant) {
            if (assistant == ASSISTANT_ALEXA || assistant == ASSISTANT_GOOGLE_HOME) {
                return true;
            }else{
                return false;
            }
        }

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMANDS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMANDS;



    }
}
