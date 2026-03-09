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

public class AddANEActivity extends AppCompatActivity
        implements withToolbar
{
    private SQLiteDatabase db;  //db

    //Список всех элементов макета
    EditText edit_text_ane_name;        //имя
    EditText edit_text_ane_model;       //модель
    EditText edit_text_ane_count_ports; //количество портов
    Button btnSave;                     //кнопка сохранить
    Button btnBack;                     //кнопка удалить


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_aneactivity);

        createToolbar(this, getString(R.string.ane));
        initPointers();

        btnSave.setOnClickListener(view -> {
            String ane_name = edit_text_ane_name.getText().toString();
            String ane_model = edit_text_ane_model.getText().toString();
            String ane_count_ports = edit_text_ane_count_ports.getText().toString();

            //упаковка вставляемого
            ContentValues val = new ContentValues();
            val.put("name", ane_name);
            val.put("model", ane_model);
            val.put("count_ports", ane_count_ports);
            val.put("count_free_ports", ane_count_ports);

            SQLiteOpenHelper dbHelper = new DBHelper(getApplicationContext());
            try {
                db = dbHelper.getWritableDatabase();

                if (validateRequiredFeeld(val))
                {
                    if (DBHelper.insertANE(db, val) != -1)
                    {
                        Toast.makeText(getApplicationContext(), R.string.savedInDB, Toast.LENGTH_SHORT).show();
                        //onBackPressed();
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                    else
                    { Toast.makeText(getApplicationContext(), R.string.notSavedInDB, Toast.LENGTH_SHORT).show(); }
                }
            }catch(SQLiteException e)
            { Toast.makeText(getApplicationContext(), R.string.databaseUnavailable, Toast.LENGTH_SHORT).show(); }
        });

        btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }


    @SuppressLint("ResourceAsColor")
    protected boolean validateRequiredFeeld(ContentValues val)
    {
        /*todo привести в порядок валидацию*/
        boolean flagEmpty = false;
        if (validator.isEmpty(getApplicationContext(), edit_text_ane_name))
        {
            flagEmpty = true;
        }

        if (validator.isEmpty(getApplicationContext(), edit_text_ane_model))
        {
            flagEmpty = true;
        }

        if (validator.isEmpty(getApplicationContext(), edit_text_ane_count_ports))
        {
            flagEmpty = true;
        }

        if (flagEmpty)
        {
            Toast.makeText(getApplicationContext(), R.string.fillAllFields, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (DBHelper.checkName(db, DBHelper.table_ane, val.get("name").toString()))
        {
            Toast.makeText(getApplicationContext(), R.string.enterAnotherName, Toast.LENGTH_SHORT).show();
            edit_text_ane_name.setTextColor(getColor(R.color.red));
            edit_text_ane_name.getBackground().setColorFilter(getColor(R.color.red), PorterDuff.Mode.SRC_IN);

            return false;
        }
        return true;
    }

    private void initPointers()
    {
        edit_text_ane_name = findViewById(R.id.ane_name);
        edit_text_ane_name.getBackground().setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        edit_text_ane_model = findViewById(R.id.ane_model);
        edit_text_ane_model.getBackground().setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        edit_text_ane_count_ports = findViewById(R.id.ane_count_ports);
        edit_text_ane_count_ports.getBackground().setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (db != null)
            db.close();
    }
}