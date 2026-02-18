package com.example.cablejournal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "cableJournal";   //Имя БД
    private static final int DB_VERSION = 1;                //версия БД

    public static final String table_pp = "pp";
    public static final String table_pp_ports = "pp_ports";
    public static final String table_ane = "ane";
    public static final String table_ane_ports = "ane_ports";
    public static final String table_equipment = "equipment";
    public static final String table_conect_pp_ane = "conect_pp_ane";
    public static final String table_conect_pp_equipment = "conect_pp_equipment";

    public static final String[] field_table_pp = new String[] {"_id", "name", "count_ports", "count_free_ports"};
    public static final String[] field_table_pp_ports = new String[] {"_id", "n_port", "label", "label_socket", "room", "id_pp"};
    public static final String[] field_table_ane = new String[] {"_id", "name", "model", "count_ports", "count_free_ports"};
    public static final String[] field_table_ane_ports = new String[] {"_id", "n_port", "id_ane"};
    public static final String[] field_table_equipment = new String[] {"_id", "name", "model", "type", "room", "inventory"};
    public static final String[] field_table_conect_pp_ane = new String[] {"_id", "id_pp", "id_ane"};
    public static final String[] field_table_conect_pp_equipment = new String[] {"_id", "id_pp", "id_equipment"};

    DBHelper (Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override
    public void onCreate (SQLiteDatabase db)
    {
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion)
    {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion < 1)
        {
            db.execSQL("CREATE TABLE pp (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT, "
                    + "count_ports INTEGER, "
                    + "count_free_ports INTEGER); ");

            db.execSQL("CREATE TABLE pp_ports (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "n_port INTEGER, "
                    + "label TEXT, "
                    + "label_socket TEXT, "
                    + "room TEXT, "
                    + "id_pp INTEGER REFERENCES pp (_id) ON DELETE CASCADE); ");

            db.execSQL("CREATE TABLE ane (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT, "
                    + "model TEXT, "
                    + "ip TEXT, "
                    + "count_ports INTEGER, "
                    + "count_free_ports INTEGER); ");

            db.execSQL("CREATE TABLE ane_ports (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "n_port INTEGER, "
                    + "id_ane INTEGER REFERENCES ane (_id) ON DELETE CASCADE); ");

            db.execSQL("CREATE TABLE equipment (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT, "
                    + "model TEXT, "
                    + "type TEXT, "
                    + "room TEXT, "
                    + "inventory TEXT); ");

            db.execSQL("CREATE TABLE conect_pp_ane (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "id_pp INTEGER REFERENCES pp_ports (_id) ON DELETE CASCADE, "
                    + "id_ane INTEGER REFERENCES ane_ports (_id) ON DELETE CASCADE); ");

            db.execSQL("CREATE TABLE conect_pp_equipment (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "id_pp INTEGER REFERENCES pp_ports (_id) ON DELETE CASCADE, "
                    + "id_equipment INTEGER REFERENCES equipment (_id) ON DELETE CASCADE); ");


            /*insertPP (db, "PP 1.1", 8);
            insertPP (db, "PP 1.2", 8);
            insertPP (db, "PP 1.3", 8);
            insertPP (db, "PP 1.4", 8);
            insertPP (db, "PP 1.5", 8);

            insertPPPorts (db, 1, "1.1.1", "101", 1);
            insertPPPorts (db, 2, "1.1.2", "101", 1);
            insertPPPorts (db, 3, "1.2.1", "101", 1);
            insertPPPorts (db, 4, "1.2.2", "101", 1);
            insertPPPorts (db, 5, "1.3.1", "102", 1);
            insertPPPorts (db, 6, "1.3.2", "102", 1);
            insertPPPorts (db, 7, "1.4.1", "102", 1);
            insertPPPorts (db, 8, "1.4.2", "102", 1);

            insertPPPorts (db, 1, "2.1.1", "103", 2);
            insertPPPorts (db, 2, "2.1.2", "103", 2);
            insertPPPorts (db, 3, "2.2.1", "103", 2);
            insertPPPorts (db, 4, "2.2.2", "103", 2);
            insertPPPorts (db, 5, "2.3.1", "104", 2);
            insertPPPorts (db, 6, "2.3.2", "104", 2);
            insertPPPorts (db, 7, "2.4.1", "104", 2);
            insertPPPorts (db, 8, "2.4.2", "104", 2);

            insertPPPorts (db, 1, "3.1.1", "105", 3);
            insertPPPorts (db, 2, "3.1.2", "105", 3);
            insertPPPorts (db, 3, "3.2.1", "105", 3);
            insertPPPorts (db, 4, "3.2.2", "105", 3);
            insertPPPorts (db, 5, "3.3.1", "106", 3);
            insertPPPorts (db, 6, "3.3.2", "106", 3);
            insertPPPorts (db, 7, "3.4.1", "106", 3);
            insertPPPorts (db, 8, "3.4.2", "106", 3);

            insertPPPorts (db, 1, "4.1.1", "107", 4);
            insertPPPorts (db, 2, "4.1.2", "107", 4);
            insertPPPorts (db, 3, "4.2.1", "107", 4);
            insertPPPorts (db, 4, "4.2.2", "107", 4);
            insertPPPorts (db, 5, "4.3.1", "108", 4);
            insertPPPorts (db, 6, "4.3.2", "108", 4);
            insertPPPorts (db, 7, "4.4.1", "108", 4);
            insertPPPorts (db, 8, "4.4.2", "108", 4);

            insertPPPorts (db, 1, "5.1.1", "109", 5);
            insertPPPorts (db, 2, "5.1.2", "109", 5);
            insertPPPorts (db, 3, "5.2.1", "109", 5);
            insertPPPorts (db, 4, "5.2.2", "109", 5);
            insertPPPorts (db, 5, "5.3.1", "110", 5);
            insertPPPorts (db, 6, "5.3.2", "110", 5);
            insertPPPorts (db, 7, "5.4.1", "110", 5);
            insertPPPorts (db, 8, "5.4.2", "110", 5);

            insertANE(db, "91004-cm001", "QTech QSW-3000", 15);
            insertANE(db, "91004-cm002", "QTech QSW-3000", 5);
            insertANE(db, "91004-cm003", "QTech QSW-8400", 5);

            insertANEPorts (db, 1, 1);
            insertANEPorts (db, 2, 1);
            insertANEPorts (db, 3, 1);
            insertANEPorts (db, 4, 1);
            insertANEPorts (db, 5, 1);
            insertANEPorts (db, 6, 1);
            insertANEPorts (db, 7, 1);
            insertANEPorts (db, 8, 1);
            insertANEPorts (db, 9, 1);
            insertANEPorts (db, 10, 1);
            insertANEPorts (db, 11, 1);
            insertANEPorts (db, 12, 1);
            insertANEPorts (db, 13, 1);
            insertANEPorts (db, 14, 1);
            insertANEPorts (db, 15, 1);
            insertANEPorts (db, 1, 2);
            insertANEPorts (db, 2, 2);
            insertANEPorts (db, 3, 2);
            insertANEPorts (db, 4, 2);
            insertANEPorts (db, 5, 2);
            insertANEPorts (db, 1, 3);
            insertANEPorts (db, 2, 3);
            insertANEPorts (db, 3, 3);
            insertANEPorts (db, 4, 3);
            insertANEPorts (db, 5, 3);

            insertEquipment(db, "test1", "model", "ws", "100", "000000000123");
            insertEquipment(db, "test2", "model", "ws", "100", "000000000124");
            insertEquipment(db, "test3", "model", "ws", "100", "000000000125");
            insertEquipment(db, "test4", "model", "ws", "100", "000000000126");
            insertEquipment(db, "test5", "model", "ws", "100", "000000000127");
            insertEquipment(db, "test6", "model", "ws", "100", "000000000128");
            insertEquipment(db, "test7", "model", "printer", "100", "000000000129");
            insertEquipment(db, "test8", "model", "printer", "100", "000000000130");

            insertConectPPANE (db, 1, 2);
            insertConectPPANE (db, 2, 3);
            insertConectPPANE (db, 3, 5);
            insertConectPPANE (db, 6, 1);
            insertConectPPANE (db, 4, 4);
            insertConectPPANE (db, 10, 7);
            insertConectPPANE (db, 8, 11);
            insertConectPPANE (db, 11, 21);
            insertConectPPANE (db, 12, 22);
            insertConectPPANE (db, 13, 23);*/
        }
        /*if (oldVersion < 2) {
            db.execSQL("ALTER TABLE DRINK ADD COLUMN FAVORITE NUMERIC;");
        }*/
    }

    public static Cursor selectFromTable(SQLiteDatabase db, String table, String selection, String[] args)
    {
        Cursor cursor;
        String[] list_fields = null;

        switch (table) {
            case table_ane:
                list_fields = field_table_ane;
                break;
            case table_pp:
                list_fields = field_table_pp;
                break;
            case table_ane_ports:
                list_fields = field_table_ane_ports;
                break;
            case table_pp_ports:
                list_fields = field_table_pp_ports;
                break;
            case table_equipment:
                list_fields = field_table_equipment;
                break;
        }

        cursor = db.query(table,
                list_fields,
                selection,
                args,
                null, null, null);

        return cursor;
    }

    public static Cursor selectFromTable(SQLiteDatabase db, String table) {
        return selectFromTable(db, table, null, null);
    }

    public static void recalculateCountOfPorts(SQLiteDatabase db, String table)
    {
        //Пересчет количества свободных портов
        Cursor cursor = db.rawQuery("SELECT "+table+"._id, "+table+".count_ports - count(conect_pp_ane._id)  FROM "+table+" " +
                        "JOIN "+table+"_ports ON ("+table+"._id = "+table+"_ports.id_"+table+") " +
                        "LEFT JOIN conect_pp_ane ON (conect_pp_ane.id_"+table+" = "+table+"_ports._id) " +
                        "GROUP BY "+table+"._id",
                null, null);

        if (!cursor.moveToFirst())
            return;

        //внесение данных о свободных портах в таблицу
        for (int i = 0; i < cursor.getCount(); i++)
        {
            ContentValues val = new ContentValues();
            val.put("count_free_ports", cursor.getInt(1));

            db.update(table, val, "_id = ?", new String[] {String.valueOf(cursor.getInt(0))});
            cursor.moveToNext();
        }
    }

    public static long insertIntoTable(SQLiteDatabase db, String table, ContentValues values)
    {
        return db.insert(table, null, values);
    }

    public static int deleteFromTableBySelection(SQLiteDatabase db, String table, String[] args, String selection)
    {
        return db.delete(table,
                selection,
                args);
    }

    public static Cursor selectAllANEPortsById(SQLiteDatabase db, long id)
    {
        Cursor cursor;
        cursor = db.rawQuery("SELECT ane_ports._id, " +
                "ane_ports.n_port n_port_ane, " +
                "pp_ports.n_port n_port_pp, " +
                "pp.name name_pp, " +
                "pp._id ppid, " +
                "pp_ports._id pp_ports_id,  " +
                "pp_ports.label " +
                "FROM ane_ports LEFT JOIN conect_pp_ane ON ane_ports._id = conect_pp_ane.id_ane " +
                "LEFT JOIN pp_ports ON pp_ports._id = conect_pp_ane.id_pp " +
                "LEFT JOIN pp ON pp._id = pp_ports.id_pp " +
                "WHERE ane_ports.id_ane = ? ORDER BY n_port_ane ", new String[]{Long.toString(id)});

        return cursor;
    }

    public static Cursor selectAllPPPortsById(SQLiteDatabase db, long id)
    {
        Cursor cursor;

        cursor = db.rawQuery("SELECT pp_ports._id, " +
                "pp_ports.n_port n_port_pp, " +
                "pp_ports.label, " +
                "pp_ports.label_socket, " +
                "pp_ports.room, " +
                "ane_ports.n_port n_port_ane," +
                "ane.name name_ane," +
                "ane.model, " +
                "ane._id ane_id, " +
                "ane_ports._id ane_ports_id  " +
                "FROM pp_ports LEFT JOIN conect_pp_ane ON pp_ports._id = conect_pp_ane.id_pp " +
                "LEFT JOIN ane_ports ON ane_ports._id = conect_pp_ane.id_ane " +
                "LEFT JOIN ane ON ane._id = ane_ports.id_ane " +
                "WHERE pp_ports.id_pp = ? ORDER BY n_port_pp ", new String[]{Long.toString(id)});

        return cursor;
    }

    public static Cursor selectEquipmentInPort(SQLiteDatabase db, long id)
    {
        Cursor cursor = db.rawQuery("SELECT equipment._id, name " +
                        "FROM equipment JOIN conect_pp_equipment ON (equipment._id = conect_pp_equipment.id_equipment) " +
                        "WHERE conect_pp_equipment.id_pp=?",
                new String[]{Long.toString(id)});

        return cursor;
    }

    public static Cursor selectAllPPPorts(SQLiteDatabase db, String name, long id)
    {
        Cursor cursor = db.rawQuery("SELECT pp_ports._id, " +
                        "pp_ports.n_port, " +
                        "pp_ports.label " +
                        "FROM pp_ports LEFT JOIN conect_pp_ane ON pp_ports._id = conect_pp_ane.id_pp " +
                        "WHERE pp_ports.id_pp = (SELECT _id FROM pp WHERE name = ?) " +
                        "AND (conect_pp_ane.id_ane iS NULL OR conect_pp_ane.id_pp = ?) ORDER BY pp_ports.n_port ",
                        new String[]{
                                name,
                                String.valueOf(id)});

        return cursor;
    }

    public static Cursor selectAllANEPorts(SQLiteDatabase db, String name, long id)
    {
        Cursor cursor;
        Log.i("123 db", ""+id);
        cursor = db.rawQuery("SELECT ane_ports._id _id, " +
                "ane_ports.n_port " +
                "FROM ane_ports LEFT JOIN conect_pp_ane ON ane_ports._id = conect_pp_ane.id_ane " +
                "WHERE ane_ports.id_ane = (SELECT _id FROM ane WHERE name = ?) " +
                "AND (conect_pp_ane.id_pp iS NULL OR conect_pp_ane.id_ane = ?) ORDER BY ane_ports.n_port ",
                new String[]{
                    name,
                    String.valueOf(id)});

        return cursor;
    }

    public static long insertANE(SQLiteDatabase db, ContentValues values)
    {
        int count = Integer.parseInt(values.get("count_ports").toString());
        long id_ane = db.insert(table_ane, null, values);

        if (id_ane != -1)
        {
            for (int i = 1; i <= count; i++)
            {
                //Упаковка вставляемого
                ContentValues valPort = new ContentValues();
                valPort.put("n_port", i);
                valPort.put("id_ane", id_ane);

                insertIntoTable(db, table_ane_ports, valPort);
            }
        }

        return id_ane;
    }

    public static long insertPP(SQLiteDatabase db, ContentValues values)
    {
        int count = Integer.parseInt(values.get("count_ports").toString());
        long id_pp = db.insert(table_pp, null, values);

        if (id_pp != -1)
        {
            for (int i = 1; i <= count; i++)
            {
                ContentValues valPort = new ContentValues();
                valPort.put("n_port", i);
                valPort.put("label", i);
                valPort.put("label_socket", i);
                valPort.put("room", i);
                valPort.put("id_pp", id_pp);

                db.insert("pp_ports", null, valPort);
            }
        }

        return id_pp;
    }

    public static Cursor selectDropdownHint(SQLiteDatabase db, String what)
    {
        Cursor cursor = db.query("equipment",
                new String[] {"_id", what},
                null,
                null,
                what,
                null,
                what);

        return cursor;
    }

    public static Cursor selectPPPortEquipment(SQLiteDatabase db, long eq_id)
    {
        Cursor cursor = db.rawQuery("SELECT pp_ports._id, pp_ports.room " +
                        "FROM pp_ports JOIN conect_pp_equipment ON (pp_ports._id = conect_pp_equipment.id_pp) " +
                        "WHERE conect_pp_equipment.id_equipment = ?",
                new String[] {Long.toString(eq_id)});

        return cursor;
    }

    public static Cursor selectRooms(SQLiteDatabase db)
    {
        Cursor cursor = db.query("pp_ports",
                new String[]{"_id", "room"},
                "room is not null",
                null,
                "room",
                null,
                "room asc");

        return cursor;
    }

    public static Cursor selectAllForExcel(SQLiteDatabase db)
    {
        Cursor cursor = db.rawQuery("SELECT pp.name, pp_ports.n_port, pp_ports.label_socket, pp_ports.room, ane_ports.n_port, ane.name, ane.model, equipment.name, equipment.model, equipment.type, equipment.inventory " +
                "FROM pp LEFT JOIN pp_ports ON (pp._id = pp_ports.id_pp) " +
                "LEFT JOIN conect_pp_ane ON (conect_pp_ane.id_pp = pp_ports._id) " +
                "LEFT JOIN ane_ports ON (conect_pp_ane.id_ane = ane_ports._id) " +
                "LEFT JOIN ane ON (ane._id = ane_ports.id_ane) " +
                "LEFT JOIN conect_pp_equipment ON (conect_pp_equipment.id_pp = pp_ports._id) " +
                "LEFT JOIN equipment ON (equipment._id = conect_pp_equipment.id_equipment)", null);

        return cursor;
    }

    public static Cursor selectForExcel(SQLiteDatabase db, String table, String[] fields)
    {
        Cursor cursor = db.query(table,
                fields,
                null,
                null,
                null,
                null,
                null);

        return cursor;
    }

    public static Cursor selectPPPortsInRoom(SQLiteDatabase db, String room)
    {
        Cursor cursor = db.query("pp_ports",
                new String[]{"_id", "label"},
                "room = ?",
                new String[]{room},
                null,
                null,
                "_id");

        return cursor;
    }


    public static boolean checkName(SQLiteDatabase db, String table, String name)
    {
        Cursor cursor = db.query(table,
                new String[]{"name"},
                "name=?",
                new String[]{name},
                null, null, null);

        return cursor.moveToFirst();
    }

    public static int updateTableBySelection(SQLiteDatabase db, String table, ContentValues values, String selection, String[] args)
    {
        return db.update(table,
                values,
                selection,
                args);
    }

    public static int deleteFromTableById(SQLiteDatabase db, String table, long id, String selection)
    {
        return db.delete(table,
                selection,
                new String[]{Long.toString(id)});
    }

    public static Cursor selectANEPorts(SQLiteDatabase db, String[] args)
    {
        Cursor cursor = db.rawQuery("SELECT ane_ports._id FROM " +
                "ane JOIN ane_ports ON (ane._id = ane_ports.id_ane) " +
                "WHERE ane.name = ? AND ane.model = ? AND ane_ports.n_port = ?",
                args);

        return cursor;
    }
}