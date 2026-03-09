package com.example.cablejournal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public  class EquipmentAutoCompleteAdapter extends BaseAdapter
        implements Filterable
{
    private static final int MAX_RESULTS = 10;
    private final Context mContext;
    private List<Equipment> mResults;
    private SQLiteDatabase db;

    public EquipmentAutoCompleteAdapter(Context context) {
        mContext = context;
        mResults = new ArrayList<Equipment>();
    }

    void setDB(SQLiteDatabase db)
    {
        this.db = db;
    }

    @Override
    public int getCount() {
        return mResults.size();
    }

    @Override
    public Equipment getItem(int index) {
        return mResults.get(index);
    }

    @Override
    public long getItemId(int position) {
        return mResults.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);
        }
        Equipment eq = getItem(position);
        ((TextView) convertView.findViewById(R.id.text1)).setText(eq.getName());
        ((TextView) convertView.findViewById(R.id.text2)).setText(eq.getInv());
        //this

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Equipment> eq = findEq(constraint.toString());
                    filterResults.values = eq;
                    filterResults.count = eq.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mResults = (List<Equipment>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};

        return filter;
    }

    @SuppressLint("Range")
    private List<Equipment> findEq(String equipment) {
        /*todo доработать фильтр*/

        Cursor cursorList = db.query("equipment",
                new String[]{"_id", "name", "inventory"},
                "name LIKE ?",
                new String[]{equipment + "%"},
                null,null,"name");



        List<Equipment> list = new ArrayList<>();

        if (cursorList.moveToFirst())
        {
            @SuppressLint("Range") Equipment tempEq = new Equipment(cursorList.getInt(cursorList.getColumnIndex("_id")),
                    cursorList.getString(cursorList.getColumnIndex("name")),
                    cursorList.getString(cursorList.getColumnIndex("inventory")));
            list.add(tempEq);
            while (cursorList.moveToNext())
            {
                tempEq = new Equipment(cursorList.getInt(cursorList.getColumnIndex("_id")),
                        cursorList.getString(cursorList.getColumnIndex("name")),
                        cursorList.getString(cursorList.getColumnIndex("inventory")));
                list.add(tempEq);
            }
        }


        cursorList.close();

        return list;
    }
}