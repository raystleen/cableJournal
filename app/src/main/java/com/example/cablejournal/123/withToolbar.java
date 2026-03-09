package com.example.cablejournal;


import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public interface withToolbar {

    default void createToolbar(AppCompatActivity activity, String resourceTitle)
    {
        //создание панели инструментов
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(resourceTitle);
        toolbar.setNavigationOnClickListener(view -> activity.onBackPressed());
    }
}