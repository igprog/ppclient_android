package com.example.programprehraneklijent;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import java.util.Calendar;
import java.util.Date;

public class ClientActivity extends AppCompatActivity {

    private static final String NAMESPACE = "http://programprehrane.com/app/";
    private static final String METHODNAME = "UpdateClientFromAndroid";
    private static final String SOAPACTION = "http://programprehrane.com/app/UpdateClientFromAndroid";
    private static final String URL =  "https://www.programprehrane.com/Clients.asmx";

    String result = "";

    private EditText etDate;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etBirthDate;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private EditText etPhone;
    private EditText etEmail;
    private Button btnSave;

    String userId = "";
    String clientId = "";

    public class Client{
        public String clientId;
        public String firstName;
        public String lastName;
        public String birthDate;
        public Gender gender = new Gender();
        public String phone;
        public String email;
        public String userId;
        public String date;
        public int isActive;
        public Object clientData;
    }

    public class Gender {
        public int value;
        public String title;
    }

    String firstName = "";
    String lastName = "";
    String birthDate = "";
    int gender = 0;
    String phone = "";
    String email = "";
    String date = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        TextView textView = (TextView)findViewById(R.id.text_view);

        setSupportActionBar(toolbar);
        if (getIntent() != null) {
            textView.setText(getIntent().getStringExtra("string"));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final LinearLayout rl = findViewById(R.id.rl);
        EditText btnBirthDate =  findViewById(R.id.etBirthDate);

        btnBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dFragment = new SlidePickerFragment();
                dFragment.show(getSupportFragmentManager(), "Date Picker");
            }
        });

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               SaveClient();
            }
        });

        SharedPreferences sp = this.getSharedPreferences("Client", MODE_PRIVATE);
        userId = sp.getString("userId", "");
        clientId = sp.getString("clientId", "");
        firstName = sp.getString("firstName", "");
        lastName = sp.getString("lastName", "");
        gender = sp.getInt("gender", 0);
        birthDate = sp.getString("birthDate", "").substring(0,10);
        phone = sp.getString("phone", "");
        email = sp.getString("email", "");
        date = sp.getString("date", "");

        etFirstName = findViewById(R.id.etFirstName);
        etFirstName.setText(firstName);
        etLastName = findViewById(R.id.etLastName);
        etLastName.setText(lastName);
        rbMale = findViewById(R.id.rbMale);
        rbMale.setChecked(gender==0?true:false);
        rbFemale = findViewById(R.id.rbFemale);
        rbFemale.setChecked(gender==1?true:false);
        etBirthDate = findViewById(R.id.etBirthDate);
        etBirthDate.setText(birthDate);  // TODO
        etPhone = findViewById(R.id.etPhone);
        etPhone.setText(phone);
        etEmail = findViewById(R.id.etEmail);
        etEmail.setText(email);

        loadPreferences();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void SaveClient(){
        if(isNetworkAvailable()==true){
            ClientActivity.AT at = new ClientActivity.AT();
            at.execute();
        }else{
            NetworkAlert na = new NetworkAlert();
            na.show(getSupportFragmentManager(), "alert");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class AT extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(ClientActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(Void... params) {
            SoapObject soapObject = new SoapObject(NAMESPACE, METHODNAME);

            SharedPreferences sp = getSharedPreferences("Client", MODE_PRIVATE);
            Client c = new Client();
            c.clientId = sp.getString("clientId", "");
            c.firstName = etFirstName.getText().toString();
            c.lastName = etLastName.getText().toString();
            c.birthDate = etBirthDate.getText().toString();
            c.gender = new Gender();
            c.gender.value = rbMale.isChecked()==true ? 0 : 1;
            c.gender.title = rbMale.isChecked()==true ? "male" : "female";
            c.phone = etPhone.getText().toString();
            c.email = etEmail.getText().toString();
            c.isActive = 1;

            soapObject.addProperty("userId", userId);
            soapObject.addProperty("firstName", c.firstName);
            soapObject.addProperty("lastName", c.lastName);
            soapObject.addProperty("birthDate", c.birthDate);
            soapObject.addProperty("gender", c.gender.value);
            soapObject.addProperty("phone", c.phone);
            soapObject.addProperty("email", c.email);
            soapObject.addProperty("clientId", c.clientId);

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
                        //JSONObject json = new JSONObject(result);
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
