package com.example.cablejournal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ANEFragment extends Fragment {
    private SQLiteDatabase db;
    private Cursor cursor;

    ListView listANE;
    View v;

    EditText searchField;

    public ANEFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //Инициализация указателей
        v = inflater.inflate(R.layout.fragment_a_n_e, container, false);

        listANE = v.findViewById(R.id.list_ane);
        searchField = v.findViewById(R.id.searchField);
        searchField.addTextChangedListener(new ANEFragment.checkChangeText());

        /*SQLiteOpenHelper dbHelper = new DBHelper(getContext());
        try
        {
            db = dbHelper.getReadableDatabase();
        } catch(SQLiteException e)
        {
            Toast.makeText(getContext(), R.string.databaseUnavailable, Toast.LENGTH_SHORT).show();
        }*/

        return v;
    }

    class checkChangeText implements TextWatcher
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            loadListOfANE(searchField.getText().toString());
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        /*searchField.setText("");

        DBHelper.recalculateCountOfPorts(db, DBHelper.table_ane);
        loadListOfANE();*/
    }

    private void loadListOfANE()
    {
        loadListOfANE("");
    }

    private void loadListOfANE(String name)
    {
        /*cursor = DBHelper.selectFromTable(db, DBHelper.table_ane, "name LIKE ? OR model LIKE ?", new String[] {"%" + name + "%", "%" + name + "%"});

        SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(getContext(),
                R.layout.item_ane_list,
                cursor,
                new String[]{"name", "model", "count_ports", "count_free_ports"},
                new int[]{R.id.name_ane, R.id.model_ane, R.id.count_ane, R.id.count_free_ane},
                0);
        listANE.setAdapter(listAdapter);

        registerForContextMenu(listANE);
        listANE.setOnItemClickListener((adapterView, view, position, id) -> editItem(position, id));*/
    }

    @SuppressLint("Range")
    private void editItem (int position, long id_item)
    {
        /*//Взятие имени активного оборудования из курсора БД
        cursor.moveToPosition(position);
        String ane_name;
        ane_name = cursor.getString(cursor.getColumnIndex("name"));

        // Интент для вызова активити с портами патч-панели
        Intent intent = new Intent(getContext(), ANEPortsActivity.class);
        intent.putExtra(ANEPortsActivity.EXTRA_ANE_ID, id_item);
        intent.putExtra(ANEPortsActivity.EXTRA_ANE_NAME, ane_name);
        startActivity(intent);*/
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();

        inflater.inflate(R.menu.context_menu, menu);

        menu.getItem(0).setOnMenuItemClickListener(this::onContextItemSelected);
        menu.getItem(1).setOnMenuItemClickListener(this::onContextItemSelected);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int selectedItem = item.getItemId();
        if (selectedItem == R.id.edit)
        {
            editItem(info.position, info.id);
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

    private void deleteItem(long id_item) {
        /*AlertDialog.Builder builderDialog = new AlertDialog.Builder(getContext());
        builderDialog.setTitle(R.string.check);
        builderDialog.setMessage(R.string.areYouSure);
        builderDialog.setPositiveButton(R.string.yes,
                (dialog, i) -> {
                    if (DBHelper.deleteFromTableBySelection(db, DBHelper.table_ane, new String[] {Long.toString(id_item)}, "_id = ?") != 0)
                        Toast.makeText(getContext(), R.string.delete_done, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(), R.string.delete_undone, Toast.LENGTH_SHORT).show();

                    cursor = DBHelper.selectFromTable(db, DBHelper.table_ane);

                    CursorAdapter adapter = (CursorAdapter) listANE.getAdapter();
                    adapter.changeCursor(cursor);
                });
        builderDialog.setNegativeButton(R.string.no,
                (dialogInterface, i) -> {

                });
        builderDialog.show();*/
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