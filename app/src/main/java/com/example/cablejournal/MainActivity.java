package com.example.cablejournal;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ViewPager2 viewPager2;  //элемент для фрагментов

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //создание панели инструментов
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //создание области для вывода фрагментов
        viewPager2 = findViewById(R.id.viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);

        //вкладки
        TabLayout tabLayout = findViewById(R.id.tabs);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout,
                viewPager2,
                (tab, position) ->
                        tab.setText(getText(ViewPagerAdapter.tabs[position])));
        tabLayoutMediator.attach();

        //выезжающая слева панель навигации
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer,
                toolbar,
                R.string.openDrawer,
                R.string.closeDrawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //обработка нажатия кнопки назад
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                drawer.closeDrawer(GravityCompat.START);
            }
        };
        getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);

    }

    //Навигация по выезжающему меню.
    @Override
    public boolean onNavigationItemSelected (MenuItem item){
        int id = item.getItemId();
        Intent intent = null;

        //Добавление активного оборудования
        if (id == R.id.navigation_addANE) {
            intent = new Intent(this, AddANEActivity.class);
        //Добавление патч-панели
        } else if (id == R.id.navigation_addPP) {
            intent = new Intent(this, AddPPActivity.class);
        //Добавление оборудования
        } else if (id == R.id.navigation_addEquipment) {
            intent = new Intent(this, AddEquipmentActivity.class);
        //Просмотр оборудования
        } else if (id == R.id.navigation_equipment) {
            intent = new Intent(this, EquipmentActivity.class);
        //Экспорт в Excel
        } else if (id == R.id.navigation_export) {
            intent = new Intent(this, ExportExcelActivity.class);
        //Импорт из Excel
        } else if (id == R.id.navigation_import) {
            intent = new Intent(this, ImportExcelActivity.class);
        }
        startActivity(intent);

        //Спрятать шторку
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}