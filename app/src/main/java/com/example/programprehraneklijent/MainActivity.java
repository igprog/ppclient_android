package com.example.programprehraneklijent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String NAMESPACE = "http://programprehrane.com/app/";
    private static final String METHODNAME = "Get";
    private static final String SOAPACTION = "http://programprehrane.com/app/Get";
    private static final String URL =  "https://www.programprehrane.com/Clients.asmx";

    String result = "";
    String userId= "";
    String clientId = "";

    private ViewPager viewPager;
    private DrawerLayout drawer;
    private TabLayout tabLayout;
    private String[] pageTitle = {"Mjere", "Statistika", "Jelovnici"};
    private LinearLayout llMainLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = this.getSharedPreferences("Client", MODE_PRIVATE);
        Boolean isActive = sp.getBoolean("isActive", false);

        if (isActive == true) {
            /****activity_main****/
            setContentView(R.layout.activity_main);

            viewPager = (ViewPager)findViewById(R.id.view_pager);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
            llMainLayout = findViewById(R.id.llMainLayout);

            setSupportActionBar(toolbar);

            //create default navigation drawer toggle
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            //setting Tab layout (number of Tabs = number of ViewPager pages)
            tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            for (int i = 0; i < 3; i++) {
                tabLayout.addTab(tabLayout.newTab().setText(pageTitle[i]));
            }

            //set gravity for tab bar
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            //handling navigation view item event
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            assert navigationView != null;
            navigationView.setNavigationItemSelectedListener(this);

            //set viewpager adapter
            ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(pagerAdapter);

            //change Tab selection when swipe ViewPager
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

            //change ViewPager page when tab selected
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

        } else {
            /****activity_activation****/
           // setContentView(R.layout.activity_activation);  //TODO
            Intent i = new Intent(getApplicationContext(), ActivationActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        loadPreferences();
        SharedPreferences sp = this.getSharedPreferences("Client", MODE_PRIVATE);
        clientId = sp.getString("clientId", "no Id...");
        userId = sp.getString("userId", "uid...");
        CallService();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.itClientData:
                Intent iClient = new Intent(getApplicationContext(), ClientActivity.class);
               // iClient.putExtra("string", "Client data...");
                startActivity(iClient);
               break;
            case R.id.itRecord:
                viewPager.setCurrentItem(0);
                break;
            case R.id.itRecords:
                viewPager.setCurrentItem(1);
                break;
            case R.id.itMenus:
                viewPager.setCurrentItem(2);
                break;
            case R.id.itSettings:
                Intent iSettings = new Intent(getApplicationContext(), PreferenceScreenActivity.class);
                startActivity(iSettings);
                break;
            case R.id.itInfo:
                Intent iInfo = new Intent(getApplicationContext(), InfoActivity.class);
                startActivity(iInfo);
                break;
            case R.id.itLogout:
                alertDialogLogout();
                break;
            case R.id.itExit:
                ExitDialog ed = new ExitDialog();
                ed.show(getSupportFragmentManager(), "exit");
                break;

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

     private void alertDialogLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Dali ste sigurni da se želite odjaviti?");
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sp = getSharedPreferences("Client", MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("isActive", false);
                ed.commit();
                finish();
                Intent i = new Intent(getApplicationContext(), ActivationActivity.class);
                startActivity(i);
            }
        });
         builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
             }

         });
         builder.show();
    }

    private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains("color")) {
            int backgroundColor = Color.WHITE;

            String color = prefs.getString("color", "1");
            switch (color) {
                case "0":
                    backgroundColor = getColor(R.color.color0);
                    break;
                case "1":
                    backgroundColor = getColor(R.color.color1);
                    break;
                case "2":
                    backgroundColor = getColor(R.color.color2);
                    break;
                case "3":
                    backgroundColor = getColor(R.color.color3);
                    break;
            }
            getWindow().getDecorView().setBackgroundColor(backgroundColor);
        }
    }


    private void CallService() {
        MainActivity.AT at = new MainActivity.AT();
        at.execute();
    }

    private class AT extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(Void... params) {

            SoapObject soapObject = new SoapObject(NAMESPACE, METHODNAME);

            PropertyInfo pi_userId = new PropertyInfo();
            pi_userId.setName("userId");
            pi_userId.setValue(userId);  // TODO
            pi_userId.setType(String.class);
            soapObject.addProperty(pi_userId);

            PropertyInfo pi_clientId = new PropertyInfo();
            pi_clientId.setName("clientId");
            pi_clientId.setValue(clientId);  // TODO
            pi_clientId.setType(String.class);
            soapObject.addProperty(pi_clientId);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
            try {
                httpTransportSE.call(SOAPACTION, envelope);
                SoapPrimitive soapPrimitive = (SoapPrimitive) envelope.getResponse();
                result = soapPrimitive.toString();
                if(TextUtils.isEmpty(result)==false){
                    try{
                        JSONObject json = new JSONObject(result);
                        SharedPreferences sp = getSharedPreferences("Client", MODE_PRIVATE);
                        SharedPreferences.Editor ed = sp.edit();
                        ed.putString("firstName", json.getString("firstName"));
                        ed.putString("lastName", json.getString("lastName"));
                        ed.putString("birthDate", json.getString("birthDate"));
                        JSONObject gender = json.getJSONObject("gender");
                        ed.putInt("gender", gender.getInt("value"));
                        ed.putString("phone", json.getString("phone"));
                        ed.putString("email", json.getString("email"));
                        ed.putString("date", json.getString("date"));
                        ed.commit();

                    } catch(Exception e) {
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = e.toString();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                result = e.toString();
            }
            return null;
        }
    }


}
