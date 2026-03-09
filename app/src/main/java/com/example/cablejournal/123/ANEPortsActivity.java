package com.example.cablejournal;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class ANEPortsActivity extends AppCompatActivity
        implements withToolbar{
    private SQLiteDatabase db;
    private Cursor cursor;
    private Cursor cursorPPNames;  //cursor names of pp

    public static final String EXTRA_ANE_ID = "ane_id";        //id ane
    public static final String EXTRA_ANE_NAME = "ane_name";    //name ane

    ListView listANEPorts;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aneports);

        String title = getIntent().getExtras().get(EXTRA_ANE_NAME).toString() + ":";
        createToolbar(this, title);
        initPointers();

        SQLiteOpenHelper dbHelper = new DBHelper(this);
        try
        {
            db = dbHelper.getReadableDatabase();

            cursorPPNames = DBHelper.selectFromTable(db, DBHelper.table_pp);

            //Список пп передается в диалог, в выпадающий список
            cursorPPNames.moveToFirst();
            ArrayList<String> namesPP = new ArrayList<>();
            int columnIndex = cursorPPNames.getColumnIndex("name");
            while(!cursorPPNames.isAfterLast())
            {
                namesPP.add(cursorPPNames.getString(columnIndex));
                cursorPPNames.moveToNext();
            }
            namesPP.add(0, getText(R.string.empty).toString());   //первый элемент пустой

            long id_ane = getIntent().getExtras().getLong(EXTRA_ANE_ID);

            cursor = DBHelper.selectAllANEPortsById(db, id_ane);

            CursorAdapter listAdapter = new SimpleCursorAdapter(this,
                    R.layout.item_ane_port,
                    cursor,
                    new String[]{"n_port_ane", "n_port_pp", "name_pp", "label"},
                    new int[]{R.id.n_port_ane, R.id.n_port_pp, R.id.name_pp, R.id.label},
                    0);
            listANEPorts.setAdapter(listAdapter);

            listANEPorts.setOnItemClickListener((adapterView, view, position, id_item) -> {
                //Диалоговое окно с данными порта патч-панели
                ANEPortDialog dlg = new ANEPortDialog();

                cursor.moveToPosition(position);

                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("ppid"));
                @SuppressLint("Range") long id_port_pp = cursor.getLong(cursor.getColumnIndex("pp_ports_id"));
                int findPP = searchN(id);

                Bundle bundle = new Bundle();
                bundle.putInt(ANEPortDialog.EXTRA_PP_N, findPP);        //номер ane по порядку в выпадающем списке
                bundle.putLong(ANEPortDialog.EXTRA_ANE_PORT_ID, id_item);     //id ane_port
                bundle.putStringArrayList(ANEPortDialog.EXTRA_PP_NAMES, namesPP);    //список пп
                bundle.putLong(ANEPortDialog.EXTRA_PP_PORT_ID, id_port_pp);
                bundle.putLong(ANEPortDialog.EXTRA_ANE_ID, (long)getIntent().getExtras().get(EXTRA_ANE_ID));
                dlg.setArguments(bundle);
                dlg.show(getSupportFragmentManager(), "dlg");
            });
        } catch(SQLiteException e) {
            Toast.makeText(this, R.string.databaseUnavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void initPointers()
    {
        listANEPorts = findViewById(R.id.list_ane_ports);
    }

    @SuppressLint("Range")
    private int searchN(long id)
    {
        int find = 0;
        cursorPPNames.moveToFirst();
        for (int j = 1; j <= cursorPPNames.getCount(); j++)
        {
            if (id == cursorPPNames.getInt(cursorPPNames.getColumnIndex("_id")))
            {
                find = j;
                break;
            }
            cursorPPNames.moveToNext();
        }

        return find;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (db != null)
            db.close();
        if (cursor != null)
            cursor.close();
        if (cursorPPNames != null)
            cursorPPNames.close();
    }
}
