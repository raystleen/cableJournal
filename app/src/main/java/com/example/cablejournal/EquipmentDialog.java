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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class EquipmentDialog extends DialogFragment
        implements View.OnClickListener{

    private SQLiteDatabase db;  //db
    private Cursor cursor;      //cursor for db

    public static final String EXTRA_EQ_ID = "eq_id";
    public static final String EXTRA_EQ_NAME = "eq_name";
    public static final String EXTRA_EQ_MODEL = "eq_model";
    public static final String EXTRA_EQ_TYPE = "eq_type";
    public static final String EXTRA_EQ_INVENTORY = "eq_inv";
    public static final String EXTRA_EQ_ROOM = "eq_room";


    View v;
    EditText editTextName;
    EditText editTextModel;
    EditText editTextType;
    EditText editTextInv;
    Spinner roomListSpinner;
    Spinner portsListSpinner;
    int eq_id;
    private int numberOfRoom;
    private int numberOfPort;

    int pp_ports_id;
    int n_port_selected;

    @SuppressLint("Range")
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //Получение view для работы с элементами
        v = inflater.inflate(R.layout.dialog_equipment, null);
        initPointers();

        editTextName.setText(getArguments().getString(EXTRA_EQ_NAME));
        editTextModel.setText(getArguments().getString(EXTRA_EQ_MODEL));
        editTextType.setText(getArguments().getString(EXTRA_EQ_TYPE));
        editTextInv.setText(getArguments().getString(EXTRA_EQ_INVENTORY));

        SQLiteOpenHelper dbHelper = new DBHelper(getContext());
        try
        {
            db = dbHelper.getWritableDatabase();

            //инициализация выпадающих списков комнат и розеток
            String eq_room = getArguments().getString(EXTRA_EQ_ROOM);
            eq_id = getArguments().getInt(EXTRA_EQ_ID);

            cursor = DBHelper.selectPPPortEquipment(db, eq_id);  //поиск порта, в который подключен eq_id
            if (cursor.moveToFirst())
                pp_ports_id = cursor.getInt(0);

            cursor = DBHelper.selectRooms(db);
            ArrayList<String> roomList = new ArrayList<>();
            if (cursor.moveToFirst())
            {
                for (int i = 0; i < cursor.getCount(); i++)
                {
                    String roomTemp = cursor.getString(cursor.getColumnIndex("room"));

                    if (roomTemp.equals(eq_room))
                        numberOfRoom = i + 1;

                    roomList.add(cursor.getString(cursor.getColumnIndex("room")));
                    cursor.moveToNext();
                }
            }
            roomList.add(0, getString(R.string.empty));
            ArrayAdapter<String> adapterRoomList = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, roomList);
            roomListSpinner.setAdapter(adapterRoomList);
            roomListSpinner.setSelection(numberOfRoom);
            roomListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    cursor = DBHelper.selectPPPortsInRoom(db, adapterView.getItemAtPosition(i).toString());

                    ArrayList<String> portsRoomList = new ArrayList<>();
                    if (cursor.moveToFirst())
                    {
                        numberOfPort = 0;

                        for (int j = 0; j < cursor.getCount(); j++)
                        {
                            if (pp_ports_id == cursor.getInt(cursor.getColumnIndex("_id")))
                                numberOfPort = j + 1;

                            portsRoomList.add(cursor.getString(1));
                            cursor.moveToNext();
                        }
                    }
                    portsRoomList.add(0, getString(R.string.empty));
                    ArrayAdapter<String> adapterPortsRoomList = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_dropdown_item, portsRoomList);
                    portsListSpinner.setAdapter(adapterPortsRoomList);

                    portsListSpinner.setSelection(numberOfPort);
                    portsListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            n_port_selected = i;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            /*todo надо наверное в дело вввести freeportlabel*/
//TextView freePortLabel = v.findViewById(R.id.free_ports_label);

            v.findViewById(R.id.btnOk).setOnClickListener(this);
            v.findViewById(R.id.btnCancel).setOnClickListener(view -> dismiss());

        } catch(SQLiteException e)
        {
            Toast.makeText(getContext(), R.string.databaseUnavailable, Toast.LENGTH_SHORT).show();
        }

        return v;
    }

    private void initPointers()
    {
        editTextName = v.findViewById(R.id.eq_name);
        editTextModel = v.findViewById(R.id.eq_model);
        editTextType = v.findViewById(R.id.eq_type);
        editTextInv = v.findViewById(R.id.eq_inv);

        roomListSpinner = v.findViewById(R.id.room_list);
        portsListSpinner = v.findViewById(R.id.pp_port_list);
    }

    @Override
    public void onClick(View view)
    {
        String strName = editTextName.getText().toString();
        String strModel = editTextModel.getText().toString();
        String strType = editTextType.getText().toString();
        String strInv = editTextInv.getText().toString();

        String strRoom = roomListSpinner.getSelectedItem().toString();
        String strPort = portsListSpinner.getSelectedItem().toString();

        ContentValues val = new ContentValues();
        val.put("name", strName);
        val.put("model", strModel);
        val.put("type", strType);
        val.put("inventory", strInv);
        if (strRoom.equals(R.string.empty))
            val.put("room", "");
        else
            val.put("room", strRoom);

        if (DBHelper.updateTableBySelection(db, DBHelper.table_equipment, val, "_id = ?", new String[]{Long.toString(eq_id)}) != 0)
            Toast.makeText(getContext(), R.string.savedInDB, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), R.string.notSavedInDB, Toast.LENGTH_SHORT).show();

        if (!strPort.equals(getText(R.string.empty)))
        {
            val.clear();

            cursor.moveToPosition(n_port_selected - 1);

            val.put("id_pp", cursor.getInt(0));
            val.put("id_equipment", eq_id);

            Cursor temp = DBHelper.selectFromTable(db, DBHelper.table_pp_ports, "label = ?", new String[]{portsListSpinner.getSelectedItem().toString()});
            //Cursor temp = DBHelper.selectIdByLabel(db, portsListSpinner.getSelectedItem().toString());
            temp.moveToFirst();
            int id_pp = temp.getInt(0);

            DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_equipment, eq_id, "id_equipment = ?");
            DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_equipment, id_pp, "id_pp = ?");
            DBHelper.insertIntoTable(db, DBHelper.table_conect_pp_equipment, val);
        }
        else
            DBHelper.deleteFromTableById(db, DBHelper.table_conect_pp_equipment, eq_id, "id_equipment = ?");

        dismiss();
        reloadEquipmentActivity();
    }

    private void reloadEquipmentActivity()
    {
        /*todo переделать изящней*/
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
