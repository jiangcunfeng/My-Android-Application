package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.Priority;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract.TodoNote;
import com.byted.camp.todolist.db.TodoDbHelper;


public class NoteActivity extends AppCompatActivity {

    private EditText editText;
    private Button addBtn;
    private RadioGroup radioGroup;
    private AppCompatRadioButton lowRadio;

    private TodoDbHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        init();
        processIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        database = null;
        dbHelper.close();
        dbHelper = null;
    }

    private void init() {
        setTitle(R.string.take_a_note);
        editText = findViewById(R.id.edit_text);
        radioGroup = findViewById(R.id.radio_group);
        lowRadio = findViewById(R.id.btn_low);
        lowRadio.setChecked(true);
        addBtn = findViewById(R.id.btn_add);

        dbHelper = new TodoDbHelper(this);
        database = dbHelper.getWritableDatabase();
    }

    private void processIntent() {
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        addBtn.setText(message);

        if(message.equals("UPDATE") ){
            String content = MainActivity.EXTRA_TEXT;
            editText.setText(content);
        }
    }

    public void commitNoteItem(View view) {
        switch (addBtn.getText().toString()) {
            case "ADD":
                addNoteItem();
                break;
            case "UPDATE":
                updateNoteItem();
                break;
            default:
                finish();
                break;
        }
    }

    public void addNoteItem() {
        CharSequence content = editText.getText();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(NoteActivity.this,
                    "No content to add", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean succeed = saveNote2Database_insert(content.toString().trim(),
                getSelectedPriority());
        if (succeed) {
            Toast.makeText(NoteActivity.this,
                    "Note added", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
        } else {
            Toast.makeText(NoteActivity.this,
                    "Error", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    public void updateNoteItem(){
        //TODO 获取id的方式很诡异啊
        Intent intent = getIntent();
        long id = MainActivity.EXTRA_LONG;
        if(id==-1){
            Toast.makeText(NoteActivity.this,
                    "update fail. can't get the id", Toast.LENGTH_SHORT).show();
            finish();
        }

        CharSequence content = editText.getText();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(NoteActivity.this,
                    "Empty content", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean succeed = saveNote2Database_update(content.toString().trim(),
                getSelectedPriority(), id);
        if (succeed) {
            Toast.makeText(NoteActivity.this,
                    "Note updated", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
        } else {
            Toast.makeText(NoteActivity.this,
                    "Error", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private boolean saveNote2Database_insert(String content, Priority priority) {
        if (database == null || TextUtils.isEmpty(content)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(TodoNote.COLUMN_CONTENT, content);
        values.put(TodoNote.COLUMN_STATE, State.TODO.intValue);
        values.put(TodoNote.COLUMN_DATE, System.currentTimeMillis());
        values.put(TodoNote.COLUMN_PRIORITY, priority.intValue);

        long rowId = database.insert(TodoNote.TABLE_NAME, null, values);
        return rowId != -1;
    }

    private boolean saveNote2Database_update(String content, Priority priority, long id) {
        if (database == null || TextUtils.isEmpty(content)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(TodoNote.COLUMN_CONTENT, content);
        values.put(TodoNote.COLUMN_STATE, State.TODO.intValue);
        values.put(TodoNote.COLUMN_DATE, System.currentTimeMillis());
        values.put(TodoNote.COLUMN_PRIORITY, priority.intValue);

        long rowId = database.update(TodoNote.TABLE_NAME, values,
                TodoNote._ID + "=?",
                new String[]{String.valueOf(id)});
        return rowId != -1;
    }

    private Priority getSelectedPriority() {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.btn_high:
                return Priority.High;
            case R.id.btn_medium:
                return Priority.Medium;
            default:
                return Priority.Low;
        }
    }
}
