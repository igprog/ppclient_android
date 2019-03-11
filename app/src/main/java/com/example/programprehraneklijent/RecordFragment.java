package com.example.programprehraneklijent;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RecordFragment extends Fragment {

    private static final String NAMESPACE = "http://programprehrane.com/app/";

    String result = "";

    private EditText etDate;
    private EditText etWeight;
    private EditText etHeight;
    private EditText etWaist;
    private EditText etHip;

    private TextView tvShowOtherMeasures;
    private LinearLayout llOtherMeasures;
    private Button btnSave;
    private Spinner spPal;

    String clientId = "";
    String height = "";
    String weight = "";
    String waist = "";
    String hip = "";
    String pal = "";  // TODO
    String date = "";
    String userId = "";


    String[] palValues = new String[]{ "1.3", "1.52", "1.67", "1.85", "2.2", "2.5" };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] palText = new String[]{ getString(R.string.inactive),
                getString(R.string.sedentary),
                getString(R.string.moderately_active),
                getString(R.string.active),
                getString(R.string.very_active),
                getString(R.string.extremely_active)};

        etDate = view.findViewById(R.id.etDate);
        etWeight = view.findViewById(R.id.etWeight);
        etHeight = view.findViewById(R.id.etHeigth);
        etWaist = view.findViewById(R.id.etWaist);
        etHip = view.findViewById(R.id.etHip);

        tvShowOtherMeasures = view.findViewById(R.id.tvShowOtherMeasures);
        tvShowOtherMeasures.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_right, 0);
        llOtherMeasures = view.findViewById(R.id.llOtherMeasures);
        btnSave = view.findViewById(R.id.btnSave);
        spPal = view.findViewById(R.id.spPal);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        String today = year + "-" + FormatDateStr(month) + "-" + FormatDateStr(day);

        etDate.setText(today);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, palText);
        spPal.setAdapter(adapter);

        SpinnerAdapter adap = new ArrayAdapter<String>(getActivity(), R.layout.my_spinner_style, palText);
        spPal.setAdapter(adap);

        RecordFragment.ATgetData atGetData = new RecordFragment.ATgetData();
        atGetData.execute();

        setupListeners();

    }

    private String FormatDateStr(int x){
        return x < 10 ? "0" + x : Integer.toString(x);
    }

    private void setupListeners() {
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dpf = new DatePickerFragment();
                dpf.show(getFragmentManager(), "Date picker");
            }
        });

        tvShowOtherMeasures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(llOtherMeasures.getVisibility() == View.GONE) {
                    llOtherMeasures.setVisibility(View.VISIBLE);
                    tvShowOtherMeasures.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                } else {
                    llOtherMeasures.setVisibility(View.GONE);
                    tvShowOtherMeasures.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_right, 0);
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveClientData();
            }
        });

        spPal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pal = palValues[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void SaveClientData(){
        date = etDate.getText().toString();
        weight = etWeight.getText().toString();
        height = etHeight.getText().toString();
        waist = etWaist.getText().toString();
        hip = etHip.getText().toString();
        pal = palValues[spPal.getSelectedItemPosition()];
        if(isNetworkAvailable()==true){
            RecordFragment.ATsave atSave = new RecordFragment.ATsave();
            atSave.execute();
        } else {
            NetworkAlert na = new NetworkAlert();
            na.show(getActivity().getSupportFragmentManager(), "alert");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class ATsave extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(result.equals("saved")){
                Toast.makeText(getActivity(),R.string.saved,Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            String METHODNAME = "SaveClientDataFromAndroid";
            String SOAPACTION = "http://programprehrane.com/app/SaveClientDataFromAndroid";
            String URL =  "https://www.programprehrane.com/ClientsData.asmx";

            SoapObject soapObject = new SoapObject(NAMESPACE, METHODNAME);

            SharedPreferences sp = getActivity().getSharedPreferences("Client", Context.MODE_PRIVATE);
            clientId = sp.getString("clientId", "");
            userId = sp.getString("userId", "");

            soapObject.addProperty("clientId", clientId);
            soapObject.addProperty("height", height);
            soapObject.addProperty("weight", weight);
            soapObject.addProperty("waist", waist);
            soapObject.addProperty("hip", hip);
            soapObject.addProperty("pal", pal);
            soapObject.addProperty("date", date);
            soapObject.addProperty("userId", userId);

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

    private class ATgetData extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            etWeight.setText(weight);
            etHeight.setText(height);
            etWaist.setText(waist);
            etHip.setText(hip);
            int pos = new ArrayList<String>(Arrays.asList(palValues)).indexOf(pal);
            spPal.setSelection(pos);
        }

        @Override
        protected String doInBackground(Void... params) {

            String METHODNAME = "Get";
            String SOAPACTION = "http://programprehrane.com/app/Get";
            String URL =  "https://www.programprehrane.com/ClientsData.asmx";
            String res = "";
            SoapObject soapObject = new SoapObject(NAMESPACE, METHODNAME);

            SharedPreferences sp = getActivity().getSharedPreferences("Client", Context.MODE_PRIVATE);
            userId = sp.getString("userId", "");
            clientId = sp.getString("clientId", "");
            soapObject.addProperty("userId", userId);
            soapObject.addProperty("clientId", clientId);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
            try {
                httpTransportSE.call(SOAPACTION, envelope);
                SoapPrimitive soapPrimitive = (SoapPrimitive) envelope.getResponse();
                res = soapPrimitive.toString();
                if(TextUtils.isEmpty(res)==false){
                    try{
                        JSONObject json = new JSONObject(res);
                        weight = json.getString("weight");
                        height = json.getString("height");
                        waist = json.getString("waist");
                        hip = json.getString("hip");
                        String pal_ = json.getString("pal");
                        JSONObject pal_json = new JSONObject(pal_);
                        pal = pal_json.getString("value");
                        } catch(Exception e) {
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                res = e.toString();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                res = e.toString();
            }
            return null;
        }
    }

}
