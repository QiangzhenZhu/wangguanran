package com.a10835.easywol.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.a10835.easywol.R;
import com.a10835.easywol.fragment.AddFragment;
import com.a10835.easywol.fragment.HomeFragment;
import com.a10835.easywol.fragment.MineFragment;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = "HomeActivity";
    private static final int HOMEPAGER_FRAGMENT = 0;
    private static final int ADD_FRAGMENT = 1;
    private static final int MINE_FRAGMENT = 2;

    private BottomNavigationView mBottomNavigationView;
    private FragmentManager fragmentManager;

    private HomeFragment homeFragment;
    private AddFragment addFragment;
    private MineFragment mineFragment;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_slidingmenu);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        mContext = this;
        initView();
        initData();
        setSelection(HOMEPAGER_FRAGMENT);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    //初始化视图
    public void initView() {
        mBottomNavigationView = findViewById(R.id.bootom_navigationview_tab);
        //设置匿名监听器，监听点击了底部哪一个tab
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        setSelection(HOMEPAGER_FRAGMENT);
                        Toast.makeText(mContext, "考勤", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.navigation_dashboard:
                        setSelection(ADD_FRAGMENT);
                        Toast.makeText(mContext, "控制", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.navigation_notifications:
                        setSelection(MINE_FRAGMENT);
                        Toast.makeText(mContext, "设置", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

    }


    //初始化数据
    public void initData() {
        homeFragment = new HomeFragment();
        mineFragment = new MineFragment();
        addFragment = new AddFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fl_fragment_container, homeFragment)
                .add(R.id.fl_fragment_container, addFragment)
                .add(R.id.fl_fragment_container, mineFragment)
                .commit();
    }

    //选择显示哪一个Fragemnt
    public void setSelection(int selection) {
        hideAllFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (selection) {
            case HOMEPAGER_FRAGMENT:
                transaction.show(homeFragment).commit();
                break;
            case ADD_FRAGMENT:
                transaction.show(addFragment).commit();
                break;
            case MINE_FRAGMENT:
                transaction.show(mineFragment).commit();
                break;
            default:
                break;


        }
    }

    //隐藏全部的Fragment
    public void hideAllFragment() {
        if (homeFragment != null) {
            fragmentManager.beginTransaction().hide(homeFragment).commit();
        }
        if (addFragment != null) {
            fragmentManager.beginTransaction().hide(addFragment).commit();
        }
        if (mineFragment != null) {
            fragmentManager.beginTransaction().hide(mineFragment).commit();
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.slidingmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
