package com.example.cablejournal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class ExportExcelActivity extends AppCompatActivity
        implements withToolbar{

    private SQLiteDatabase db;
    private Cursor cursor;      //cursor for db

    EditText fileName;
    Button exportInExcelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_excel);

        createToolbar(this, getString(R.string.exportExcel));

        fileName = findViewById(R.id.filename);
        exportInExcelButton = findViewById(R.id.exportInExcelButton);

        exportInExcelButton.setOnClickListener(view -> {
            exportInExcel();

            /*todo доделать обработку*/
        });
    }

    protected void exportInExcel()
    {
        /*todo доделать проверку на наличие прав*/
        ActivityCompat.requestPermissions(this,
                new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        XSSFWorkbook workBook = new XSSFWorkbook();
        FileOutputStream fileOut;
        try {
            //String fileNameStr = "/sdcard/Download/" + fileName.getText().toString() + ".xlsx";

            String fileNameStr = Environment.getExternalStorageDirectory().getPath() + fileName.getText().toString() + ".xlsx";

            fileOut = new FileOutputStream(fileNameStr);

            SQLiteOpenHelper dbHelper = new DBHelper(this);
            db = dbHelper.getWritableDatabase();

            XSSFSheet sheet = workBook.createSheet("CableJournal");

            XSSFFont fontTitle = workBook.createFont();
            fontTitle.setBold(true);
            XSSFCellStyle styleTitle = workBook.createCellStyle();
            styleTitle.setBorderTop(BorderStyle.MEDIUM);
            styleTitle.setBorderRight(BorderStyle.MEDIUM);
            styleTitle.setBorderLeft(BorderStyle.MEDIUM);
            styleTitle.setBorderBottom(BorderStyle.MEDIUM);
            styleTitle.setAlignment(HorizontalAlignment.CENTER);
            styleTitle.setWrapText(true);
            styleTitle.setFont(fontTitle);

            /*XSSFFont fontContent = workBook.createFont();
            fontContent.setBold(true);*/
            XSSFCellStyle styleContent = workBook.createCellStyle();
            styleContent.setBorderTop(BorderStyle.THIN);
            styleContent.setBorderRight(BorderStyle.THIN);
            styleContent.setBorderLeft(BorderStyle.THIN);
            styleContent.setBorderBottom(BorderStyle.THIN);

            cursor = DBHelper.selectAllForExcel(db);
            cursor.moveToFirst();

            XSSFRow row = sheet.createRow(0);
            printCell(row, 0, styleTitle, "Патч-панель");
            printCell(row, 1, styleTitle, "Номер порта");
            printCell(row, 2, styleTitle, "Подпись розетки");
            printCell(row, 3, styleTitle, "Кабинет");
            printCell(row, 4, styleTitle, "Номер порта активного оборудования");
            printCell(row, 5, styleTitle, "Активное оборудование");
            printCell(row, 6, styleTitle, "Модель активного оборудования");
            printCell(row, 7, styleTitle, "Конечное оборудование");
            printCell(row, 8, styleTitle, "Модель оборудования");
            printCell(row, 9, styleTitle, "Тип оборудования");
            printCell(row, 10, styleTitle, "Инвентарный номер");

            printRows(cursor, row, sheet, styleContent);

            cursor = DBHelper.selectForExcel(db, DBHelper.table_pp, new String[]{"name", "count_ports"});
            cursor.moveToFirst();

            XSSFSheet sheetPP = workBook.createSheet("PP");
            row = sheetPP.createRow(0);
            printCell(row, 0, styleTitle, "Имя патч-панели");
            printCell(row, 1, styleTitle, "Количество портов");

            printRows(cursor, row, sheetPP, styleContent);


            cursor = DBHelper.selectForExcel(db, DBHelper.table_ane, new String[]{"name", "model", "count_ports"});
            cursor.moveToFirst();

            XSSFSheet sheetANE = workBook.createSheet("ANE");
            row = sheetANE.createRow(0);
            printCell(row, 0, styleTitle, "Имя активного оборудования");
            printCell(row, 1, styleTitle, "Модель");
            printCell(row, 2, styleTitle, "Количество портов");

            printRows(cursor, row, sheetANE, styleContent);


            cursor = DBHelper.selectForExcel(db, DBHelper.table_equipment, new String[]{"name", "model", "type", "room", "inventory"});
            cursor.moveToFirst();

            XSSFSheet sheetEquipment = workBook.createSheet("Equipment");
            row = sheetEquipment.createRow(0);
            printCell(row, 0, styleTitle, "Имя оборудования");
            printCell(row, 1, styleTitle, "Модель");
            printCell(row, 2, styleTitle, "Тип");
            printCell(row, 3, styleTitle, "Кабинет");
            printCell(row, 4, styleTitle, "Инвентарный");

            printRows(cursor, row, sheetEquipment, styleContent);


            workBook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printRows(Cursor cursor, XSSFRow row, XSSFSheet sheet, XSSFCellStyle style)
    {
        for (int i = 0; i < cursor.getCount(); i++)
        {
            row = sheet.createRow(i + 1);

            for (int j = 0; j < cursor.getColumnCount(); j++)
            {
                printCell(row, j, style, cursor.getString(j));

            }
            cursor.moveToNext();
        }
    }

    private void printCell(XSSFRow row, int columnIndex, XSSFCellStyle style, String content)
    {
        XSSFCell cell = row.createCell(columnIndex);
        cell.setCellType(CellType.STRING);
        cell.setCellStyle(style);
        cell.setCellValue(content);
    }
}