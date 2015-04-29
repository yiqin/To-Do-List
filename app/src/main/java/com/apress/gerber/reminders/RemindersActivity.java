package com.apress.gerber.reminders;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.widget.ToggleButton;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.text.Html;

public class RemindersActivity extends ActionBarActivity {

    private ListView mListView;
    private RemindersDbAdapter mDbAdapter;
    private RemindersSimpleCursorAdapter mCursorAdapter;

    private int selectedReminderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reminders);
        mListView = (ListView) findViewById(R.id.reminders_list_view);
        mListView.setDivider(null);

        selectedReminderId = -1;

        mDbAdapter = new RemindersDbAdapter(this);
        mDbAdapter.open();
        if (savedInstanceState == null) {
            //Clean all data
            mDbAdapter.deleteAllReminders();

            //Add some data
            insertSomeReminders();
        }

        updateListView();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedReminderId = (int)id;
                openContextMenu(view);
            }
        });

        registerForContextMenu(mListView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_delete_reminder, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit_reminder:
                openDialogOfReminder(false);
                return true;
            case R.id.menu_delete_reminder:
                mDbAdapter.deleteReminderById(selectedReminderId);
                updateListView();
                return true;
            default:
                return false;
        }
    }

    private void insertSomeReminders() {
        mDbAdapter.createReminder("Buy Learn Android Studio", true);
        mDbAdapter.createReminder("Send Dad birthday gift", false);
        mDbAdapter.createReminder("Dinner at the Gage on Friday", false);
        mDbAdapter.createReminder("String squash racket", false);
        mDbAdapter.createReminder("Shovel and salt walkways", false);
        mDbAdapter.createReminder("Prepare Advanced Android syllabus", true);
        mDbAdapter.createReminder("Buy new office chair", false);
        mDbAdapter.createReminder("Call Auto-body shop for quote", false);
        mDbAdapter.createReminder("Renew membership to club", false);
        mDbAdapter.createReminder("Buy new Galaxy Android phone", true);
        mDbAdapter.createReminder("Sell old Android phone - auction", false);
        mDbAdapter.createReminder("Buy new paddles for kayaks", false);
        mDbAdapter.createReminder("Call accountant about tax returns", false);
        mDbAdapter.createReminder("Buy 300,000 shares of Google", false);
        mDbAdapter.createReminder("Call the Dalai Lama back", true);

    }

    // Update List View
    private void updateListView() {
        Cursor cursor = mDbAdapter.fetchAllReminders();
        //from columns defined in the db
        String[] from = new String[]{
                RemindersDbAdapter.COL_CONTENT
        };

        //to the ids of views in the layout
        int[] to = new int[]{
                R.id.row_text
        };
        mCursorAdapter = new RemindersSimpleCursorAdapter(
                //context
                RemindersActivity.this,
                //the layout of the row
                R.layout.reminders_row,
                //cursor
                cursor,
                //from columns defined in the db
                from,
                //to the ids of views in the layout
                to,
                //flag - not used
                0);

        //the cursorAdapter (controller) is now updating the listView (view) with data from the db (model)
        mListView.setAdapter(mCursorAdapter);
    }

    private void openDialogOfReminder(final boolean isCreate){

        LayoutInflater li = LayoutInflater.from(RemindersActivity.this);
        View reminderDialogView = li.inflate(R.layout.reminder_dialog, null);
        final EditText userInput = (EditText) reminderDialogView.findViewById(R.id.dialog_userInput);
        final ToggleButton isImportant = (ToggleButton) reminderDialogView.findViewById(R.id.dialog_togglebutton);


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RemindersActivity.this);

        String positiveButtonString = "";
        if (isCreate) {
            alertDialogBuilder.setTitle(Html.fromHtml("<font color='#00ff00'>Create Reminder</font>"));
            positiveButtonString = "Create";
        }
        else {
            alertDialogBuilder.setTitle(Html.fromHtml("<font color='#0000FF'>Edit Reminder</font>"));
            positiveButtonString = "Confirm";

            Reminder reminder = mDbAdapter.fetchReminderById(selectedReminderId);
            userInput.setText(reminder.getContent());
            if (reminder.getImportant()==0){
                isImportant.setChecked(false);
            }
            else {
                isImportant.setChecked(true);
            }
        }

        // set prompts.xml to alert dialog builder
        alertDialogBuilder.setView(reminderDialogView);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(positiveButtonString,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and save it to database. Then update the list view.
                                if (isCreate) {
                                    mDbAdapter.createReminder(userInput.getText().toString(), isImportant.isChecked());
                                } else {
                                    Reminder reminder = mDbAdapter.fetchReminderById(selectedReminderId);
                                    reminder.setContent(userInput.getText().toString());
                                    reminder.setImportant(isImportant.isChecked()? 1 : 0);
                                    mDbAdapter.updateReminder(reminder);
                                }
                                updateListView();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                // Create New Reminder
                openDialogOfReminder(true);
                return true;
            case R.id.action_exit:
                finish();
                return true;
            case android.R.id.home:
                openDialogOfReminder(true);
                return true;
            default:
                return false;
        }
    }


}
