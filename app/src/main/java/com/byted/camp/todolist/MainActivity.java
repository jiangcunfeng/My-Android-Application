package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.Priority;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract.TodoNote;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.debug.DebugActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private RadioGroup radioGroup;

    private TodoDbHelper dbHelper;
    private SQLiteDatabase database;

    public static final String EXTRA_MESSAGE = "";
    public static String EXTRA_TEXT = "";
    public static long EXTRA_LONG = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }

            @Override
            public void updateNote(long id, String content) {
                MainActivity.this.goNoteActivity_update(id, content);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        database = null;
        dbHelper.close();
        dbHelper = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private void init(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        radioGroup = findViewById(R.id.radio_group_filter);

        FloatingActionButton fab = findViewById(R.id.fab);

        dbHelper = new TodoDbHelper(this);
        database = dbHelper.getWritableDatabase();


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                notesAdapter.refresh(loadNotesFromDatabase());
            }
        });
    }

    public void goNoteActivity_add(View view){
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "ADD");
        startActivityForResult(intent, REQUEST_CODE_ADD);
    }

    public void goNoteActivity_update(long id, String content){
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "UPDATE");
        //TODO 不可以进行多次putExtra吗，试了好多次都不行啊
        EXTRA_LONG = id;
        EXTRA_TEXT = content;
        startActivityForResult(intent, REQUEST_CODE_ADD);
    }

    private List<Note> loadNotesFromDatabase() {
        if (database == null) {
            return Collections.emptyList();
        }

        String sql = "select * from "+TodoNote.TABLE_NAME+" where "+TodoNote.COLUMN_PRIORITY+" = ";
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.btn_high_filter:
                sql += Priority.High.intValue;
                break;
            case R.id.btn_medium_filter:
                sql += Priority.Medium.intValue;
                break;
            case R.id.btn_low_filter:
                sql += Priority.Low.intValue;
                break;
            case R.id.btn_all_filter:
                sql = "select * from "+TodoNote.TABLE_NAME;
                break;
            default:
                sql = "select * from "+TodoNote.TABLE_NAME;
                break;
        }
        sql+=" order by date";

        List<Note> result = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(TodoNote._ID));
                String content = cursor.getString(cursor.getColumnIndex(TodoNote.COLUMN_CONTENT));
                long dateMs = cursor.getLong(cursor.getColumnIndex(TodoNote.COLUMN_DATE));
                int intState = cursor.getInt(cursor.getColumnIndex(TodoNote.COLUMN_STATE));
                int intPriority = cursor.getInt(cursor.getColumnIndex(TodoNote.COLUMN_PRIORITY));

                Note note = new Note(id);
                note.setContent(content);
                note.setDate(new Date(dateMs));
                note.setState(State.from(intState));
                note.setPriority(Priority.from(intPriority));

                result.add(note);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    private void deleteNote(Note note) {
        if (database == null) {
            return;
        }
        int rows = database.delete(TodoNote.TABLE_NAME,
                TodoNote._ID + "=?",
                new String[]{String.valueOf(note.id)});
        if (rows > 0) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private void updateNode(Note note) {
        if (database == null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(TodoNote.COLUMN_STATE, note.getState().intValue);

        int rows = database.update(TodoNote.TABLE_NAME, values,
                TodoNote._ID + "=?",
                new String[]{String.valueOf(note.id)});
        if (rows > 0) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

}
