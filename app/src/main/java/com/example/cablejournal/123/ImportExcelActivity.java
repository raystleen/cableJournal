package com.example.cablejournal;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class ImportExcelActivity extends AppCompatActivity
        implements withToolbar
{
    private SQLiteDatabase db;
    private Cursor cursor;
    EditText fileName;
    Button selectFileButton;
    Button importButton;
    Uri filename = null;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import_excel);
        createToolbar(this, getString(R.string.importExcel));

        SQLiteOpenHelper dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent intent = result.getData();

                    filename = intent.getData();
                    fileName.setText(filename.getLastPathSegment());

                    selectFileButton.setVisibility(View.GONE);
                    importButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(this, "Произошла ошибка импорта", Toast.LENGTH_SHORT).show();
                }
            });

        progressBar = findViewById(R.id.progressBar);
        fileName = findViewById(R.id.filename);
        selectFileButton = findViewById(R.id.selectFileButton);
        importButton = findViewById(R.id.importButton);

        selectFileButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            mStartForResult.launch(intent);
        });

        importButton.setOnClickListener(view -> {
            importButton.setVisibility(View.GONE);
            importFromExcel(filename);
        });
    }

    //Подсчет строк на странице таблицы
    private int calculateCountRows(XSSFWorkbook workBook, String nameOfSheet)
    {
        int count = 0;
        XSSFSheet sheet = workBook.getSheet(nameOfSheet);
        Iterator<Row> ri = sheet.rowIterator();
        ri.next();  //Пропуск заголовка таблицы
        while (ri.hasNext())
        {
            ri.next();
            count++;
        }

        return count;
    }

    private void importFromExcel(Uri filename)
    {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);


        InputStream fileIn;

        try {
            fileIn = getContentResolver().openInputStream(filename);

            XSSFWorkbook workBook = (XSSFWorkbook) WorkbookFactory.create(fileIn);

            int count_journal =  calculateCountRows(workBook, "CableJournal");
            int count_pp = calculateCountRows(workBook, DBHelper.table_pp);
            int count_ane = calculateCountRows(workBook, DBHelper.table_ane);
            int count_eq =  calculateCountRows(workBook, DBHelper.table_equipment);
            int count_all = count_journal + count_pp + count_ane + count_eq;

            progressBar.setMax(count_all);


            new Thread(() -> {
                importTable(workBook, DBHelper.table_pp, new String[]{"name", "count_ports"});
                importTable(workBook, DBHelper.table_ane, new String[]{"name", "model", "count_ports"});
                importTable(workBook, DBHelper.table_equipment, new String[]{"name", "model", "type", "room", "inventory"});

                XSSFSheet sheet = workBook.getSheet("CableJournal");

                Iterator<Row> ri = sheet.rowIterator();
                ri.next();

                int count = 0;

                while(ri.hasNext())
                {
                    count++;
                    progressBar.setProgress(progressBar.getProgress() + 1);

                    ContentValues val = new ContentValues();

                    XSSFRow row = (XSSFRow) ri.next();

                    Iterator<Cell> ci = row.cellIterator();
                    XSSFCell cell = (XSSFCell) ci.next();
                    String namePP = cell.toString();

                    cursor = DBHelper.selectFromTable(db, DBHelper.table_pp, "name = ?", new String[]{namePP});

                    cursor.moveToFirst();

                    int id_pp = cursor.getInt(0);

                    cell = (XSSFCell) ci.next();

                    String[] nameCols = new String[] {"n_port", "label_socket", "room"};
                    for (String name:nameCols)
                    {
                        val.put(name, cell.toString());
                        if (ci.hasNext())
                            cell = (XSSFCell) ci.next();
                    }
                    val.put("label", val.get("label_socket").toString());

                    DBHelper.updateTableBySelection(db, DBHelper.table_pp_ports, val, "id_pp = ? AND n_port = ?", new String[]{Long.toString(id_pp), val.getAsString("n_port")});

                    String n_port = cell.toString();
                    cell = (XSSFCell) ci.next();
                    String ane_name = cell.toString();
                    cell = (XSSFCell) ci.next();
                    String ane_model = cell.toString();

                    cursor = DBHelper.selectFromTable(db, DBHelper.table_pp_ports, "id_pp = ? AND n_port = ?", new String[]{Long.toString(id_pp), val.getAsString("n_port")});

                    cursor.moveToFirst();
                    int id_pp_port = cursor.getInt(0);


                    cursor = DBHelper.selectANEPorts(db, new String[]{ane_name, ane_model, n_port});

                    if (cursor.moveToFirst())
                    {
                        int id_ane_port = cursor.getInt(0);


                        val.clear();
                        val.put("id_ane", id_ane_port);
                        val.put("id_pp", id_pp_port);
                        DBHelper.insertIntoTable(db, DBHelper.table_conect_pp_ane, val);
                    }
                    cell = (XSSFCell) ci.next();
                    String eq_name = cell.toString();
                    cell = (XSSFCell) ci.next();
                    String eq_model = cell.toString();
                    cell = (XSSFCell) ci.next();
                    String eq_type = cell.toString();
                    cell = (XSSFCell) ci.next();
                    String eq_inventory = cell.toString();

                    cursor = DBHelper.selectFromTable(db, DBHelper.table_equipment, "name = ? AND model = ? AND type = ? AND inventory = ?", new String[]{eq_name, eq_model, eq_type, eq_inventory});

                    if (cursor.moveToFirst())
                    {
                        val.clear();
                        val.put("id_pp", id_pp_port);
                        val.put("id_equipment", cursor.getInt(0));
                        DBHelper.insertIntoTable(db, DBHelper.table_conect_pp_equipment, val);
                    }
                }
            }).start();

            fileIn.close();
        } catch (EncryptedDocumentException | /*InvalidFormatException | */IOException e) {
            e.printStackTrace();
        }
    }

    private int importTable(XSSFWorkbook workBook, String nameOfSheet, String[] nameCols)
    {
        XSSFSheet sheet = workBook.getSheet(nameOfSheet);

        Iterator<Row> rowIterator = sheet.rowIterator();
        if (rowIterator.hasNext())
            rowIterator.next();
        else
            return 0;

        int count = 0;

        while (rowIterator.hasNext())
        {
            XSSFRow row = (XSSFRow) rowIterator.next();
            count ++;
            progressBar.setProgress(progressBar.getProgress()+1);

            Iterator<Cell> cellIterator = row.cellIterator();
            XSSFCell cell;
            int count_cols = 0;

            ContentValues val = new ContentValues();
            for (String name:nameCols)
            {
                if (cellIterator.hasNext()) {
                    cell = (XSSFCell) cellIterator.next();
                    val.put(name, cell.toString());
                    count_cols++;
                }
            }

            //Если считаных столбцов меньше чем надо то
            if (nameCols.length != count_cols)
            {
                count--;
                continue;
            }

            //Если такое имя уже есть
            if (DBHelper.selectFromTable(db, nameOfSheet, "name = ?", new String[]{val.getAsString("name")}).moveToFirst())
            {
                continue;
            }
            long id = DBHelper.insertIntoTable(db, nameOfSheet, val);

            if (nameCols[nameCols.length - 1].equals("count_ports"))
            {
                createPorts(val, nameOfSheet, id);
            }

        }

        return count;
    }

    private void createPorts(ContentValues values, String nameOfTable, long id)
    {
        if (values.getAsInteger("count_ports") != null)
        {
            int count = values.getAsInteger("count_ports");

            for (int i = 1; i <= count; i++)
            {
                ContentValues val = new ContentValues();
                val.put("n_port", i);
                val.put("id_"+nameOfTable, id);

                DBHelper.insertIntoTable(db, nameOfTable+"_ports", val);
            }
        }
    }
}