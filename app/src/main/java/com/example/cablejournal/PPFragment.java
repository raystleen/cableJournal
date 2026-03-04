package com.example.cablejournal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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


public class PPFragment extends Fragment {

    private SQLiteDatabase db;  //db
    private Cursor cursor;      //cursor for db

    ListView listPP;            //список отображаемых патч панелей
    EditText searchField;       //строка поиска

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //вьюха для доступа к ее элементам
        View view = inflater.inflate(R.layout.fragment_p_p, container, false);

        searchField = view.findViewById(R.id.searchField);
        //todo сделать поиск с задержкой
        searchField.addTextChangedListener(new checkChangeText());  //при изменении - выполнить поиск

        
        SQLiteOpenHelper DBHelper = new DBHelper(getContext());
        try
        {
            db = DBHelper.getReadableDatabase();
        } catch(SQLiteException e)
        {
            Toast.makeText(getContext(), R.string.databaseUnavailable, Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    class checkChangeText implements TextWatcher
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            showListOfPP(searchField.getText().toString());
        }
    }

    @SuppressLint("Range")
    @Override
    public void onResume()
    {
        super.onResume();

        searchField.setText("");

        //пересчет свободных портов и вывод списка патч-панелей
        DBHelper.recalculateCountOfPorts(db, DBHelper.table_pp);
        showListOfPP();
    }

    private void showListOfPP(String name)
    {
        listPP = getView().findViewById(R.id.list_pp);

        //Запрос всех патч-панелей
        cursor = DBHelper.selectFromTable(db,
                DBHelper.table_pp,
                "name LIKE ?",
                new String[] {"%" + name + "%"});

        //Адаптер для подсовывания в список ListView
        SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(getContext(),
                R.layout.item_pp,
                cursor,
                new String[]{"name", "count_ports", "count_free_ports"},
                new int[]{R.id.name_pp, R.id.count_pp, R.id.count_free_pp},
                0);
        listPP.setAdapter(listAdapter);

        //Обработчик нажатия на списке и контектсное меню
        listPP.setOnItemClickListener((adapterView, view, position, id) -> editPorts(position, id));
        registerForContextMenu(listPP);
    }

    private void showListOfPP()
    {
        showListOfPP("");
    }

    @Override
    public void onCreateContextMenu (@NonNull ContextMenu menu, @NonNull View view, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item)
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

    private void editPorts (int position, long id)
    {
        //Взятие имени патч-панели из курсора БД
        cursor.moveToPosition(position);
        @SuppressLint("Range") String pp_name = cursor.getString(cursor.getColumnIndex("name"));

        // Интент для вызова активити с портами патч-панели
        Intent intent = new Intent(getContext(), PPPortsActivity.class);
        intent.putExtra(PPPortsActivity.EXTRA_PP_ID, id);            //id_pp
        intent.putExtra(PPPortsActivity.EXTRA_PP_NAME, pp_name);    //pp_name
        startActivity(intent);
    }

    /*TODO реализовать редактирование самой патч-панели*/
    private void editItem(int position, long id)
    {

    }

    private void deleteItem(long idItem)
    {
        AlertDialog.Builder builderDialog = new AlertDialog.Builder(requireContext());
        builderDialog.setTitle(R.string.check);
        builderDialog.setMessage(R.string.areYouSure);
        builderDialog.setPositiveButton(R.string.yes,
                (dialog, i) -> {
                    DBHelper.deleteFromTableById(db, DBHelper.table_pp, idItem, "_id = ?");
                    //Запрос всех патч-панелей
                    cursor = DBHelper.selectFromTable(db, DBHelper.table_pp);

                    //Обновление данных в списке
                    CursorAdapter adapter = (CursorAdapter) listPP.getAdapter();
                    adapter.changeCursor(cursor);
                });
        builderDialog.setNegativeButton(R.string.no,
                (dialogInterface, i) -> {});
        builderDialog.show();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (db != null)
            db.close();
        if (cursor != null)
            cursor.close();
    }
}