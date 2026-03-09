package com.example.cablejournal;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class AddEquipmentActivity extends AppCompatActivity
        implements withToolbar
{
    private SQLiteDatabase db;  //db

    EditText edit_text_eq_name;
    AutoCompleteTextView edit_text_eq_model;
    AutoCompleteTextView edit_text_eq_type;
    EditText edit_text_eq_inv;
    EditText edit_text_eq_room;
    Button btnSave;
    Button btnBack;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_equipment);

        createToolbar(this, getString(R.string.equipment));
        initPointers();

        SQLiteOpenHelper dbHelper = new DBHelper(getApplicationContext());
        try {
            db = dbHelper.getWritableDatabase();

            loadDropdownHint("type", R.id.eq_type);
            loadDropdownHint("model", R.id.eq_model);

        }  catch(SQLiteException e)
        {
            Toast.makeText(getApplicationContext(), R.string.databaseUnavailable, Toast.LENGTH_SHORT).show();
        }

        btnSave.setOnClickListener(view -> {
            String eq_name = edit_text_eq_name.getText().toString();
            String eq_model = edit_text_eq_model.getText().toString();
            String eq_type = edit_text_eq_type.getText().toString();
            String eq_inv = edit_text_eq_inv.getText().toString();
            String eq_room = edit_text_eq_room.getText().toString();

            ContentValues val = new ContentValues();

            val.put("name", eq_name);
            val.put("model", eq_model);
            val.put("type", eq_type);
            val.put("inventory", eq_inv);
            val.put("room", eq_room);

            if (validateRequiredFeeld (val))
            {
                if (DBHelper.insertIntoTable(db, DBHelper.table_equipment, val) != 0)
                    Toast.makeText(getApplicationContext(), R.string.savedInDB, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), R.string.notSavedInDB, Toast.LENGTH_SHORT).show();

                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void initPointers()
    {
        edit_text_eq_name = findViewById(R.id.eq_name);
        edit_text_eq_model = findViewById(R.id.eq_model);
        edit_text_eq_type = findViewById(R.id.eq_type);
        edit_text_eq_inv = findViewById(R.id.eq_inv);
        edit_text_eq_room = findViewById(R.id.eq_room);

        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
    }

    @SuppressLint("ResourceAsColor")
    protected boolean validateRequiredFeeld(ContentValues val)
    {
        /*todo привести в порядок валидацию*/
        boolean flagEmpty = false;
        if (validator.isEmpty(getApplicationContext(), edit_text_eq_name))
        {
            flagEmpty = true;
        }
        /*if (val.get("name").toString().trim().isEmpty())
        {
            edit_text_eq_name.setHighlightColor(R.color.red);
            flagEmpty = true;
        }*/
        if (validator.isEmpty(getApplicationContext(), edit_text_eq_model))
        {
            flagEmpty = true;
        }
        /*if (val.get("model").toString().trim().isEmpty())
        {
            edit_text_eq_model.setHighlightColor(R.color.red);
            flagEmpty = true;
        }*/
        if (validator.isEmpty(getApplicationContext(), edit_text_eq_type))
        {
            flagEmpty = true;
        }
        /*if (val.get("type").toString().trim().isEmpty())
        {
            edit_text_eq_type.setHighlightColor(R.color.red);
            flagEmpty = true;
        }*/
        if (validator.isEmpty(getApplicationContext(), edit_text_eq_inv))
        {
            flagEmpty = true;
        }
        /*if (val.get("inventory").toString().trim().isEmpty())
        {
            edit_text_eq_inv.setHighlightColor(R.color.red);
            flagEmpty = true;
        }*/

        if (flagEmpty)
        {
            Toast.makeText(getApplicationContext(), R.string.fillAllFields, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (DBHelper.checkName(db, DBHelper.table_equipment, val.get("name").toString()))
        {
            edit_text_eq_name.setTextColor(getColor(R.color.red));
            edit_text_eq_name.getBackground().setColorFilter(getColor(R.color.red), PorterDuff.Mode.SRC_IN);
            Toast.makeText(getApplicationContext(), R.string.enterAnotherName, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @SuppressLint("ResourceAsColor")
    protected void loadDropdownHint(String what, int idResource)
    {
        Cursor cursor = DBHelper.selectDropdownHint(db, what);

        cursor.moveToFirst();
        ArrayList<String> listOfString = new ArrayList<>();
        while(!cursor.isAfterLast()) {
            listOfString.add(cursor.getString(1));
            cursor.moveToNext();
        }

        String[] arrayString = listOfString.toArray(new String[listOfString.size()]);

        AutoCompleteTextView eq_what = findViewById(idResource);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<> (this,
                        android.R.layout.simple_list_item_1,
                        arrayString);
        eq_what.setThreshold(1);
        eq_what.setAdapter(adapter);

        cursor.close();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (db != null)
            db.close();
    }
}
