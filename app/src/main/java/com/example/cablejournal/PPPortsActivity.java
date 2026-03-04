package com.example.cablejournal;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class PPPortsActivity extends AppCompatActivity
    implements withToolbar{

    private SQLiteDatabase db;      //db
    private Cursor cursor;          //cursor for db
    public static final String EXTRA_PP_ID = "pp_id";        //id patch panel
    public static final String EXTRA_PP_NAME = "pp_name";    //name patch panel

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppports);

        //создание панели инструментов
        String title = getIntent().getExtras().get(EXTRA_PP_NAME).toString() + ":";
        createToolbar(this, title);

        ListView listPPPorts = findViewById(R.id.list_pp_ports);         //список портов патч панели

        SQLiteOpenHelper dbHelper = new DBHelper(this);
        try
        {
            db = dbHelper.getReadableDatabase();

            cursor = DBHelper.selectAllPPPortsById(db, getIntent().getExtras().getLong(EXTRA_PP_ID));

            SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
                    R.layout.item_pp_port,
                    cursor,
                    new String[]{"n_port_pp", "label", "n_port_ane", "name_ane", "model"},
                    new int[]{R.id.n_port_pp, R.id.label, R.id.n_port_ane, R.id.name_ane, R.id.model_ane},
                    0);
            listPPPorts.setAdapter(listAdapter);

            listPPPorts.setOnItemClickListener((adapterView, view, position, id_item) -> {
                //Диалоговое окно с данными порта патч-панели
                PPPortDialog dlg = new PPPortDialog();

                cursor.moveToPosition(position);

                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("ane_id")); //id активки к которой подключен порт пп
                @SuppressLint("Range") String labelPPPort = cursor.getString(cursor.getColumnIndex("label"));   //label порта
                @SuppressLint("Range") long id_port_ane = cursor.getInt(cursor.getColumnIndex("ane_ports_id"));  //порт активки
                int n_port_pp = cursor.getInt(cursor.getColumnIndex("n_port_pp"));
                String label_socket = cursor.getString(cursor.getColumnIndex("label_socket"));
                String room = cursor.getString(cursor.getColumnIndex("room"));
                String name_pp = getIntent().getExtras().getString(EXTRA_PP_NAME);

                Bundle bundle = new Bundle();
                bundle.putLong(PPPortDialog.EXTRA_PP_PORT_ID, id_item);             //id pp_port
                bundle.putLong(PPPortDialog.EXTRA_ANE_PORT_ID, id_port_ane);        //id порта активки
                bundle.putInt(PPPortDialog.EXTRA_ANE_ID, id);                       //id активки
                bundle.putString(PPPortDialog.EXTRA_PP_PORT_LABEL, labelPPPort);    //надпись на порте пп
                bundle.putInt(PPPortDialog.EXTRA_PP_PORT_N, n_port_pp);             //Номер выбранного порта
                bundle.putString(PPPortDialog.EXTRA_PP_NAME, name_pp);              //Имя патч панели
                bundle.putString(PPPortDialog.EXTRA_PP_SOCKET_LABEL, label_socket); //надпись на розетке
                bundle.putString(PPPortDialog.EXTRA_PP_ROOM, room);                 //комната

                dlg.setArguments(bundle);
                dlg.show(getSupportFragmentManager(), "dlg");
            });
        } catch(SQLiteException e)
        {
            Toast.makeText(this, R.string.databaseUnavailable, Toast.LENGTH_SHORT).show();
        }
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