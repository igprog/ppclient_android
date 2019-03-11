package com.example.programprehraneklijent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ActivationActivity extends AppCompatActivity {

    private static final String NAMESPACE = "http://programprehrane.com/app/";
    private static final String METHODNAME = "Activate";
    private static final String SOAPACTION = "http://programprehrane.com/app/Activate";
    private static final String URL =  "https://www.programprehrane.com/ClientApp.asmx";

    String result = "";
    Boolean isActive = false;
    String code = "";

    private EditText etActivationCode;
    private Button btnActivation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        etActivationCode = findViewById(R.id.etActivationCode);
        btnActivation = findViewById(R.id.btnActivation);

        setupListeners();

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        TextView textView = (TextView)findViewById(R.id.text_view);

        setSupportActionBar(toolbar);
        if (getIntent() != null) {
            textView.setText(getIntent().getStringExtra("string"));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    private void setupListeners() {
        btnActivation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = etActivationCode.getText().toString();
                CallService();
            }
        });
    }

    private Boolean checkActivationCode(String x) {
        if (x.equals(code)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
           // finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
       // finish();
    }


   private void CallService() {
        if(isNetworkAvailable()==true){
            AT at = new AT();
            at.execute();
        } else {
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
            if(isActive == false) {
                Toast.makeText(ActivationActivity.this, "Pogrešan aktivacijski kod!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            SoapObject soapObject = new SoapObject(NAMESPACE, METHODNAME);

            PropertyInfo pi = new PropertyInfo();
            pi.setName("code");
            pi.setValue(code);
            pi.setType(String.class);
            soapObject.addProperty(pi);

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
                        String id = json.getString("clientId");
                        String uid = json.getString("userId");
                        String code = json.getString("code");

                        isActive = checkActivationCode(code);
                        if(isActive == true) {
                            SharedPreferences sp = getSharedPreferences("Client", MODE_PRIVATE);
                            SharedPreferences.Editor ed = sp.edit();
                            ed.putBoolean("isActive", isActive);
                            ed.putString("clientId", id);
                            ed.putString("userId", uid);
                            ed.commit();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            finish();
                        }
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
