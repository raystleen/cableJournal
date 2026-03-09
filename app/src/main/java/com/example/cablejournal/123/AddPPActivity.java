package com.example.cablejournal;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddPPActivity extends AppCompatActivity
        implements withToolbar
{
    private SQLiteDatabase db;  //db

    EditText edit_text_pp_name;
    EditText edit_text_pp_count_ports;
    Button btnSave;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ppactivity);

        createToolbar(this, getString(R.string.pp));
        initPointers();

        btnSave.setOnClickListener(view -> {
            SQLiteOpenHelper dbHelper = new DBHelper(getApplicationContext());
            try {
                db = dbHelper.getWritableDatabase();

                ContentValues val = new ContentValues();

                String pp_name = edit_text_pp_name.getText().toString();
                String pp_count_ports = edit_text_pp_count_ports.getText().toString();

                val.put("name", pp_name);
                val.put("count_ports", pp_count_ports);
                val.put("count_free_ports", pp_count_ports);

                if (validateRequiredFeeld(val))
                {
                    long id_pp = DBHelper.insertPP(db, val);

                    if (id_pp != -1)
                    {
                        Toast.makeText(getApplicationContext(), R.string.savedInDB, Toast.LENGTH_SHORT).show();
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                    else
                        Toast.makeText(getApplicationContext(), R.string.notSavedInDB, Toast.LENGTH_SHORT).show();
                }
            } catch(SQLiteException e)
            {
                Toast.makeText(getApplicationContext(), R.string.databaseUnavailable, Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void initPointers()
    {
        edit_text_pp_name = findViewById(R.id.pp_name);
        edit_text_pp_count_ports = findViewById(R.id.pp_count_ports);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
    }

    @SuppressLint("ResourceAsColor")
    protected boolean validateRequiredFeeld(ContentValues val)
    {
        /*todo привести в порядок валидацию*/
        boolean flagEmpty = false;
        if (validator.isEmpty(getApplicationContext(), edit_text_pp_name))
        {
            flagEmpty = true;
        }
        if (validator.isEmpty(getApplicationContext(), edit_text_pp_count_ports))
        {
            flagEmpty = true;
        }
        if (flagEmpty)
        {
            Toast.makeText(getApplicationContext(), R.string.fillAllFields, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (DBHelper.checkName(db, DBHelper.table_pp, val.get("name").toString()))
        {
            Toast.makeText(getApplicationContext(), R.string.enterAnotherName, Toast.LENGTH_LONG).show();
            edit_text_pp_name.setTextColor(getColor(R.color.red));
            edit_text_pp_name.getBackground().setColorFilter(getColor(R.color.red), PorterDuff.Mode.SRC_IN);

            return false;
        }

        return true;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (db != null)
            db.close();
    }
}