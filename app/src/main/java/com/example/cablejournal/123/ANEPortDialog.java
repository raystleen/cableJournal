package com.example.cablejournal;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class ANEPortDialog extends DialogFragment
        implements View.OnClickListener{
    private SQLiteDatabase db;  //db
    private Cursor cursor;      //cursor for db

    public static final String EXTRA_PP_N = "pp_n";
    public static final String EXTRA_ANE_PORT_ID = "ane_port_id";
    public static final String EXTRA_ANE_ID = "ane_id";
    public static final String EXTRA_PP_NAMES = "pp_names";
    public static final String EXTRA_PP_PORT_ID = "pp_port_id";

    private Spinner ppList;
    private Spinner ppPortsList;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //Получение view для работы с элементами
        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.dialog_ane_port, null);

        SQLiteOpenHelper dbHelper = new DBHelper(getContext());
        try
        {
            db = dbHelper.getWritableDatabase();

            //Задание списка пп (выпадающий список)
            ppList = v.findViewById(R.id.pp_list);
            ArrayList<String> namesPP;
            namesPP = getArguments().getStringArrayList(EXTRA_PP_NAMES);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),  android.R.layout.simple_spinner_dropdown_item, namesPP);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ppList.setAdapter(adapter);
            int ppN = getArguments().getInt(EXTRA_PP_N);
            ppList.setSelection(ppN);

            TextView freePortLabel = v.findViewById(R.id.free_ports_label);


            ppList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
                {
                    //Запрос всех свободных портов пп + тот, который соединен с ane
                    cursor = DBHelper.selectAllPPPorts(db, ppList.getItemAtPosition(position).toString(), getArguments().getLong(EXTRA_PP_PORT_ID, 0));

                    if (!cursor.moveToFirst())
                        freePortLabel.setVisibility(View.VISIBLE);
                    else
                        freePortLabel.setVisibility(View.INVISIBLE);

                    if (ppList.getSelectedItem().toString().equals(getText(R.string.empty).toString()))
                        freePortLabel.setVisibility(View.INVISIBLE);

                    //выпадающий список доступных портов
                    ppPortsList = v.findViewById(R.id.pp_port_list);
                    SimpleCursorAdapter listPortsAdapter = new SimpleCursorAdapter(getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            cursor,
                            new String[]{"label"},
                            new int[]{android.R.id.text1},
                            0);
                    ppPortsList.setAdapter(listPortsAdapter);

                    int findPPN = searchN();
                    ppPortsList.setSelection(findPPN);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            v.findViewById(R.id.btnOk).setOnClickListener(this);
            v.findViewById(R.id.btnCancel).setOnClickListener(view -> dismiss());

        } catch(SQLiteException e)
        {
            Toast.makeText(getContext(), R.string.databaseUnavailable, Toast.LENGTH_SHORT).show();
        }

        return v;
    }

    private int searchN()
    {
        int findPPN = 0;        //Поиск порядкового номера активного оборудования в списке
        long id_pp_port = getArguments().getLong(EXTRA_PP_PORT_ID, 0);
        if (id_pp_port != 0)
        {
            cursor.moveToFirst();
            for (int j = 0; j < cursor.getCount(); j++) {
                @SuppressLint("Range") int cur_id = cursor.getInt(cursor.getColumnIndex("_id"));

                if (id_pp_port == cur_id)
                {
                    findPPN = j;
                    break;
                }
                cursor.moveToNext();
            }
        }
        return findPPN;
    }

    @Override
    public void onClick(View view)
    {
        Cursor spinnerCursor = (Cursor) ppPortsList.getSelectedItem();

        long id_ane_port = getArguments().getLong(EXTRA_ANE_PORT_ID, 0);

        if (ppList.getSelectedItem().toString().contentEquals(getText(R.string.empty)))
        {
            if (DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_ane, id_ane_port, "id_ane = ?") != 0)
                Toast.makeText(getContext(), R.string.linkDeleted, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), R.string.linkNotDeleted, Toast.LENGTH_SHORT).show();

            dismiss();
            reloadANEPortActivity();
            return;
        }

        if (spinnerCursor == null)
        {
            Toast.makeText(getContext(), R.string.portNotSelected, Toast.LENGTH_SHORT).show();

            dismiss();
            reloadANEPortActivity();
            return;
        }

        String[] args = new String[] {Long.toString(getArguments().getLong(EXTRA_ANE_PORT_ID))};
        cursor = DBHelper.selectFromTable(db, DBHelper.table_conect_pp_ane, "id_ane = ?", args);

        if (cursor.moveToFirst())
        {
            ContentValues val = new ContentValues();
            val.put("id_pp", spinnerCursor.getString(0));

            if (DBHelper.updateTableBySelection(db, DBHelper.table_conect_pp_ane, val, "id_ane = ?", new String[] {Long.toString(getArguments().getLong(EXTRA_ANE_PORT_ID))}) != 0)
                Toast.makeText(getContext(), R.string.connectSaved, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), R.string.connectNotSaved, Toast.LENGTH_SHORT).show();
        }
        else
        {
            ContentValues val = new ContentValues();
            val.put("id_pp", spinnerCursor.getString(0));
            val.put("id_ane", Long.toString(getArguments().getLong(EXTRA_ANE_PORT_ID)));

            if (DBHelper.insertIntoTable(db, DBHelper.table_conect_pp_ane, val) != 0)
                Toast.makeText(getContext(), R.string.connectSaved, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), R.string.connectNotSaved, Toast.LENGTH_SHORT).show();
        }

        dismiss();
        reloadANEPortActivity();
    }

    private void reloadANEPortActivity()
    {
        /*todo переделать этот момент*/
        Intent intent = getActivity().getIntent();
        getActivity().finish();
        //getActivity().findViewById(R.id.ane_list);
        getActivity().overridePendingTransition(0, 0);
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) { super.onCancel(dialog); }

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
