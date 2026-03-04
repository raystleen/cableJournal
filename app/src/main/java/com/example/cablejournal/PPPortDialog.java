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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class PPPortDialog extends DialogFragment
        implements View.OnClickListener{
    private SQLiteDatabase db;  //db
    private Cursor cursor;      //cursor for db
    private View v;             //view для доступа к элементам макета

    private boolean changeLabelFlag = false;    //если true то вносятся изменения в label
    public static final String EXTRA_PP_PORT_ID = "pp_port_id";
    public static final String EXTRA_ANE_ID = "ane_id";
    public static final String EXTRA_ANE_PORT_ID = "ane_port_id";
    public static final String EXTRA_PP_PORT_LABEL = "pp_port_label";
    public static final String EXTRA_PP_PORT_N = "pp_port_n";
    public static final String EXTRA_PP_NAME = "pp_name";
    public static final String EXTRA_PP_SOCKET_LABEL = "pp_socket_label";
    public static final String EXTRA_PP_ROOM = "pp_room";


    TextView title;
    EditText editLabel;
    EditText socket_label;
    EditText room;
    Spinner aneList;
    Spinner anePortsList;
    TextView freePortLabel;
    AutoCompleteTextView eq;

    String id_pp_port;  //id порта для которого вызвано окно


    private long selectedEqID;
    private int selectedNumb;
    private boolean socketChangeFlag = false;


    @SuppressLint("Range")
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //Получение view для работы с элементами
        v = inflater.inflate(R.layout.dialog_pp_port, null);

        id_pp_port = Long.toString(getArguments().getLong(EXTRA_PP_PORT_ID));

        //Заголовок диалогового окна
        title = v.findViewById(R.id.title);
        String titleText = "# " + getArguments().getInt(EXTRA_PP_PORT_N) + " ( " + getArguments().getString(EXTRA_PP_NAME) + " ) :";
        title.setText(titleText);

        //Задание надписи на порт
        editLabel = v.findViewById(R.id.port_label);
        editLabel.setText(getArguments().getString(EXTRA_PP_PORT_LABEL));
        //editLabel.addTextChangedListener(new checkChangeText());

        //как подписана розетка
        socket_label = v.findViewById(R.id.socket_label);
        socket_label.setText(getArguments().getString(EXTRA_PP_SOCKET_LABEL));
        //socket_label.addTextChangedListener(new checkChangeText());

        //кабинет
        room = v.findViewById(R.id.room);
        room.setText(getArguments().getString(EXTRA_PP_ROOM));
        //room.addTextChangedListener(new checkChangeText());

        //Задание списка активного оборудования (выпадающий список)
        aneList = v.findViewById(R.id.ane_list);
        freePortLabel = v.findViewById(R.id.free_ports_label);

        anePortsList = v.findViewById(R.id.ane_port_list);

        eq = v.findViewById(R.id.list_equipment);

        v.findViewById(R.id.btnOk).setOnClickListener(this);
        v.findViewById(R.id.btnCancel).setOnClickListener(view -> dismiss());

        SQLiteOpenHelper dbHelper = new DBHelper(getContext());
        try
        {
            db = dbHelper.getWritableDatabase();

            eq.setThreshold(1);
            EquipmentAutoCompleteAdapter autoCompliteAdapter = new EquipmentAutoCompleteAdapter(getContext());
            autoCompliteAdapter.setDB(db);
            eq.setAdapter(autoCompliteAdapter);
            //eq.setLoadingIndicator((ProgressBar) findViewById(R.id.progress_bar));
            eq.setOnItemClickListener((adapterView, view, position, id) -> {
                Equipment eqTemp = (Equipment) adapterView.getItemAtPosition(position);
                eq.setText(eqTemp.getName());
                selectedNumb = position;
                selectedEqID = id;
            });

            Cursor tempCursor = DBHelper.selectEquipmentInPort(db, getArguments().getLong(EXTRA_PP_PORT_ID));

            if (tempCursor.moveToFirst())
                eq.setText(tempCursor.getString(tempCursor.getColumnIndex("name")));

            loadANESpinner(v);

        } catch(SQLiteException e)
        {
            Toast.makeText(getContext(), R.string.databaseUnavailable, Toast.LENGTH_SHORT).show();
        }
        return v;
    }
    /*class checkChangeText implements TextWatcher
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            changeLabelFlag = true;
        }
    }*/

    @Override
    public void onClick(View view)
    {
        ContentValues val = new ContentValues();
        val.put("label", editLabel.getText().toString());
        val.put("label_socket", socket_label.getText().toString());
        val.put("room", room.getText().toString());

        if (validateRequiredFeeld(val))
        {
            if (DBHelper.updateTableBySelection(db, DBHelper.table_pp_ports, val, "_id = ?", new String[] {id_pp_port}) != 0)
                Toast.makeText(getContext(), R.string.connectSaved, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), R.string.connectNotSaved, Toast.LENGTH_SHORT).show();
        }

        if (aneList.getSelectedItem().toString().equals(getString(R.string.empty)))
        {
            if (DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_ane, Long.parseLong(id_pp_port), "id_pp = ?") != 0)
                Toast.makeText(getContext(), R.string.linkDeleted, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), R.string.linkNotDeleted, Toast.LENGTH_SHORT).show();

            dismiss();
            reloadPPPortActivity();
            return;
        }

        Cursor spinnerCursor = (Cursor) anePortsList.getSelectedItem();

        long id_ane_port = anePortsList.getSelectedItemId();

        if (spinnerCursor == null)
        {
            Toast.makeText(getContext(), R.string.portNotSelected, Toast.LENGTH_SHORT).show();

            dismiss();
            reloadPPPortActivity();
            return;
        }
        else
        {
            DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_ane, Long.parseLong(id_pp_port), "id_pp = ?");
            //DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_ane, spinnerCursor.getLong(0), "id_ane = ?");

            DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_ane, id_ane_port, "id_ane = ?");


            val.clear();
            //val.put("id_ane", spinnerCursor.getString(0));
            val.put("id_ane", id_ane_port);
            val.put("id_pp", id_pp_port);
            //Log.i("123", spinnerCursor.getString(0));
            Log.i("123", Long.toString(id_ane_port));
            Log.i("123", id_pp_port);

            DBHelper.insertIntoTable(db, DBHelper.table_conect_pp_ane, val);
            //db.insert("conect_pp_ane", null, val);
        }

        if (eq.getText().toString().trim().isEmpty())
        {
            DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_equipment, Long.parseLong(id_pp_port), "id_pp = ?");
        }
        else if (selectedEqID != 0)
        {
            cursor = DBHelper.selectFromTable(db, DBHelper.table_equipment, "_id = ?", new String[]{Long.toString(selectedEqID)});


            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String sel = cursor.getString(cursor.getColumnIndex("name"));
                if (eq.getText().toString().equals(sel)) {
                    DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_equipment, selectedEqID, "id_equipment = ?");
                    /*db.delete("conect_pp_equipment",
                            "id_equipment=?",
                            new String[]{Long.toString(selectedEqID)});*/

                    DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_equipment, Long.parseLong(id_pp_port), "id_pp = ?");
                    /*db.delete("conect_pp_equipment",
                            "id_pp = ?",
                            new String[]{id_pp_port});*/

                    val.clear();
                    val.put("id_pp", id_pp_port);
                    val.put("id_equipment", selectedEqID);

                    DBHelper.insertIntoTable(db, DBHelper.table_conect_pp_equipment, val);
                    /*db.insert("conect_pp_equipment",
                            null,
                            val);*/

                    val.clear();
                    val.put("room", room.getText().toString());
                    DBHelper.updateTableBySelection(db, DBHelper.table_equipment, val, "_id = ?", new String[] {Long.toString(selectedEqID)});
                    //db.update("equipment", val, "_id = ?", new String[]{""+selectedEqID});

                }
            }
            else
            {
                Toast.makeText(getContext(), R.string.equipmentOnlyFromList, Toast.LENGTH_SHORT).show();
            }
        }

        dismiss();
        reloadPPPortActivity();
    }

    @SuppressLint("ResourceAsColor")
    protected boolean validateRequiredFeeld(ContentValues val)
    {
        /*todo привести в порядок валидацию*/
        boolean flagEmpty = false;
        if (val.get("label").toString().trim().isEmpty())
        {
            editLabel.setHighlightColor(R.color.red);
            flagEmpty = true;
        }
        if (val.get("label_socket").toString().trim().isEmpty())
        {
            socket_label.setHighlightColor(R.color.red);
            flagEmpty = true;
        }
        if (val.get("room").toString().trim().isEmpty())
        {
            room.setHighlightColor(R.color.red);
            flagEmpty = true;
        }

        if (flagEmpty)
        {
            Toast.makeText(getContext(), R.string.fillAllFields, Toast.LENGTH_SHORT).show();
            return false;
        }

        /*cursor = db.query("equipment", new String[]{"name"},
                "name=?",
                new String[]{val.get("name").toString()},
                null, null, null);

        if (cursor.moveToFirst())
        {
            Toast.makeText(getApplicationContext(), R.string.enterAnotherName, Toast.LENGTH_LONG).show();
            return false;
        }*/

        return true;
    }

    @SuppressLint("Range")
    private void loadANESpinner(View v)
    {
        Cursor cursorANENames;
        cursorANENames = DBHelper.selectFromTable(db, DBHelper.table_ane);

        //Список активки передается в диалог, в выпадающий список
        cursorANENames.moveToFirst();
        ArrayList<String> namesANE = new ArrayList<>();
        while(!cursorANENames.isAfterLast()) {
            namesANE.add(cursorANENames.getString(cursorANENames.getColumnIndex("name")));
            cursorANENames.moveToNext();
        }
        namesANE.add(0, getText(R.string.empty).toString());   //первый элемент пустой

        int id = getArguments().getInt(EXTRA_ANE_ID); //id активки к которой подключен порт пп

        //int findANE = findNOf(cursorANENames, id, 1);        //Поиск порядкового номера активного оборудования в списке
        int findANE = 0;
        cursorANENames.moveToFirst();
        for (int j = 1; j <= cursorANENames.getCount(); j++)
        {
            if (id == cursorANENames.getInt(cursorANENames.getColumnIndex("_id")))
            {
                findANE = j;
                break;
            }
            cursorANENames.moveToNext();
        }

        //Spinner aneList = v.findViewById(R.id.ane_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),  android.R.layout.simple_spinner_dropdown_item, namesANE);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        aneList.setAdapter(adapter);
        aneList.setSelection(findANE);

        aneList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("Range")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                Log.i("123", "arg = "+getArguments().getLong(EXTRA_ANE_PORT_ID, 0));
                cursor = DBHelper.selectAllANEPorts(db, aneList.getItemAtPosition(i).toString(), getArguments().getLong(EXTRA_ANE_PORT_ID, 0));

                if (!cursor.moveToFirst())
                    freePortLabel.setVisibility(View.VISIBLE);
                else
                    freePortLabel.setVisibility(View.INVISIBLE);

                if (aneList.getSelectedItem().toString().equals(getString(R.string.empty)))
                    freePortLabel.setVisibility(View.INVISIBLE);

                //выпадающий список доступных портов

                SimpleCursorAdapter listPortsAdapter = new SimpleCursorAdapter(getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        cursor,
                        new String[]{"n_port"},
                        new int[]{android.R.id.text1},
                        0);
                anePortsList.setAdapter(listPortsAdapter);

                long id_ane_port = getArguments().getLong(EXTRA_ANE_PORT_ID, 0);

                int findANEN = 0;

                if (id_ane_port != 0)
                {
                    cursor.moveToFirst();
                    for (int j = 0; j < cursor.getCount(); j++)
                    {
                        if (id_ane_port == cursor.getInt(cursor.getColumnIndex("_id")))
                        {
                            findANEN = j;
                            break;
                        }
                        cursor.moveToNext();
                    }
                }
                anePortsList.setSelection(findANEN);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        cursorANENames.close();
    }

    @SuppressLint("Range")
    /*private int findNOf(Cursor cursor, int id, int corr)
    {
        //int id = getArguments().getInt(EXTRA_ANE_ID); //id активки к которой подключен порт пп

        int find = 0;        //Поиск порядкового номера активного оборудования в списке
        cursor.moveToFirst();
        for (int i = corr; i <= cursor.getCount(); i++)
        {
            if (id == cursor.getInt(cursor.getColumnIndex("_id")))
            {
                find = i;
                break;
            }
            cursor.moveToNext();
        }

        return find;
    }*/


    private void reloadPPPortActivity()
    {
        /*todo переделать изящней */
        Intent intent = getActivity().getIntent();
        getActivity().finish();
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


