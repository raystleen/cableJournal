package com.example.cablejournal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class EquipmentActivity extends AppCompatActivity
        implements withToolbar{

    private SQLiteDatabase db;
    private Cursor cursor;

    ListView listEquipment;

    EditText searchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);

        createToolbar(this, getString(R.string.equipment));

        listEquipment = findViewById(R.id.list_equipment);
        registerForContextMenu(listEquipment);

        searchField = findViewById(R.id.searchField);
        searchField.addTextChangedListener(new EquipmentActivity.checkChangeText());


        SQLiteOpenHelper dbHelper = new DBHelper(this);
        try
        {
            db = dbHelper.getReadableDatabase();


        } catch(SQLiteException e)
        {
            Toast.makeText(this, R.string.databaseUnavailable, Toast.LENGTH_SHORT).show();
        }
    }

    class checkChangeText implements TextWatcher
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            loadListOfEquipment(searchField.getText().toString());
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();

        loadListOfEquipment();
    }

    private void loadListOfEquipment() {loadListOfEquipment("");}

    private void loadListOfEquipment(String name)
    {
        //Запрос всех оборудок
        cursor = DBHelper.selectFromTable(db,
                DBHelper.table_equipment,
                "name LIKE ? OR model LIKE ?",
                new String[] {"%" + name + "%", "%" + name + "%"});



        //Адаптер для подсовывания в список ListView
        SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
                R.layout.item_equipment,
                cursor,
                new String[]{"name", "model", "type", "inventory"},
                new int[]{R.id.name_eq, R.id.model_eq, R.id.type_eq, R.id.inv_eq},
                0);
        listEquipment.setAdapter(listAdapter);
        //Обработчики нажатий на списке
        listEquipment.setOnItemClickListener((adapterView, view, i, l) -> editItem(i));
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int selectedItem = item.getItemId();
        if (selectedItem == R.id.edit)
        {
            editItem(info.position);
            return true;
        }
        else if (selectedItem == R.id.delete)
        {
            deleteItem(info.id);
            return true;
        }
        else
        {
            return super.onContextItemSelected(item);
        }
    }

    private void editItem (int id_item)
    {
        //Взятие имени оборудования из курсора БД
        cursor.moveToPosition(id_item);
        @SuppressLint("Range") int eq_id = cursor.getInt(cursor.getColumnIndex("_id"));
        @SuppressLint("Range") String eq_name = cursor.getString(cursor.getColumnIndex("name"));
        @SuppressLint("Range") String eq_model = cursor.getString(cursor.getColumnIndex("model"));
        @SuppressLint("Range") String eq_type = cursor.getString(cursor.getColumnIndex("type"));
        @SuppressLint("Range") String eq_inventory = cursor.getString(cursor.getColumnIndex("inventory"));
        @SuppressLint("Range") String eq_room = cursor.getString(cursor.getColumnIndex("room"));

        //Диалоговое окно с данными оборудования
        EquipmentDialog dlg = new EquipmentDialog();

        Bundle bundle = new Bundle();
        bundle.putInt(EquipmentDialog.EXTRA_EQ_ID, eq_id);
        bundle.putString(EquipmentDialog.EXTRA_EQ_NAME, eq_name);
        bundle.putString(EquipmentDialog.EXTRA_EQ_MODEL, eq_model);
        bundle.putString(EquipmentDialog.EXTRA_EQ_TYPE, eq_type);
        bundle.putString(EquipmentDialog.EXTRA_EQ_INVENTORY, eq_inventory);
        bundle.putString(EquipmentDialog.EXTRA_EQ_ROOM, eq_room);
        dlg.setArguments(bundle);

        dlg.show(getSupportFragmentManager(), "dlg");
    }

    private void deleteItem(long idItem)
    {
        AlertDialog.Builder builderDialog = new AlertDialog.Builder(this);
        builderDialog.setTitle("Проверка");
        builderDialog.setMessage("Вы ув     ерены?");
        builderDialog.setPositiveButton("Да",
                (dialog, i) -> {
                    DBHelper.deleteFromTableById(db, DBHelper.table_equipment, idItem, "_id = ?");
                    loadListOfEquipment();
                });
        builderDialog.setNegativeButton("Нет",
                (dialogInterface, i) -> {

                });
        builderDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (db != null)
            db.close();
        if (cursor != null)
            cursor.close();
    }
}
