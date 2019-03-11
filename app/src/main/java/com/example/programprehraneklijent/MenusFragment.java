package com.example.programprehraneklijent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MenusFragment extends Fragment {

    private static final String NAMESPACE = "http://programprehrane.com/app/";
    String userId = "";
    String clientId = "";
    String result = null;
    String menuId = "";
    String menu = "";
    String currMenu = null;
    private TableLayout tlMenus;
    long start = 0;
    long end = 0;
    long elapsedTime = 0;
    long timeout = 1400;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menus, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tlMenus = view.findViewById(R.id.tlMenus);

        MenusFragment.ATgetClientMenus atGetClientLog = new MenusFragment.ATgetClientMenus();
        atGetClientLog.execute();
    }

    private class ATgetClientMenus extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            String date = "";
            String title = "";

            try {
                JSONArray arr = new JSONArray(result);
                for (int i=0; i<arr.length(); i++){
                    JSONObject row = arr.getJSONObject(i);
                    final String id = row.getString("id");
                    date = row.getString("date").substring(0,10);
                    title = row.getString("title");
                    TableRow tr = new TableRow(getContext());
                    tr.setPadding(0, 30, 0, 0);
                  //  tr.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

                    TextView tvDate = new TextView(getContext());
                    tvDate.setText(date);
                    tvDate.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
                    tvDate.setTypeface(tvDate.getTypeface(), Typeface.BOLD);
                    tvDate.setTextSize(14);
                    tvDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.food, 0, 0,0);
                    tr.addView(tvDate);
                    final TextView tvTitle = new TextView(getContext());
                    tvTitle.setText(title);
                    tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);
                    tvTitle.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.arrow_right,0);
                    tvTitle.setTextSize(18);
                    tr.addView(tvTitle);

                    final int id_ = i;
                    tr.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            currMenu = null;
                            start = System.currentTimeMillis();
                            end = 0;
                            elapsedTime = 0;
                            menuId = id;

                            final TextView tvFood;
                            tvFood = getView().findViewById(id_);
                            if (tvFood.getVisibility() == View.GONE) {
                                MenusFragment.ATgetMenu atGetMenu = new MenusFragment.ATgetMenu();
                                start = System.currentTimeMillis();
                                atGetMenu.execute();
                                Toast.makeText(getActivity(),R.string.loading,Toast.LENGTH_SHORT).show();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                       // Toast.makeText(getActivity(),Long.toString(elapsedTime),Toast.LENGTH_SHORT).show();
                                       // Toast.makeText(getActivity(),Long.toString(timeout),Toast.LENGTH_SHORT).show();
                                        if(currMenu != null){
                                            tvFood.setVisibility(View.VISIBLE);
                                            tvTitle.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.arrow_down,0);
                                            tvFood.setText(currMenu);
                                        } else {
                                            Toast.makeText(getActivity(),"try again",Toast.LENGTH_SHORT).show();
                                            timeout += 400;
                                        }
                                    }
                                },  timeout);
                            } else {
                                tvFood.setVisibility(View.GONE);
                                tvTitle.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.arrow_right,0);
                            }
                        }
                    });
                    tlMenus.addView(tr);
                    TextView tvFood = new TextView(getContext());
                    tvFood.setId(id_);
                    tvFood.setTextSize(14);
                    tvFood.setTextColor(getResources().getColor(R.color.black));
                    tvFood.setVisibility(getView().GONE);
                    tlMenus.addView(tvFood);
                }
            } catch (Exception e){
                Toast.makeText(getActivity(), e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            String METHODNAME = "LoadClientMenues";
            String SOAPACTION = "http://programprehrane.com/app/LoadClientMenues";
            String URL =  "https://www.programprehrane.com/Menues.asmx";

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
                result = soapPrimitive.toString();
                if(TextUtils.isEmpty(result)==false){
                    try {
                         // JSONObject json = new JSONObject(result);
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

    private class ATgetMenu extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                String food = "";
                String quantity = "";
                String unit = "";
                String mass = "";
                StringBuilder sb = new StringBuilder();
                JSONObject m = new JSONObject(menu);
                JSONObject data = m.getJSONObject("data");
                JSONArray selectedFoods = data.getJSONArray("selectedFoods");
                JSONArray meals = data.getJSONArray("meals");

                for(int j=0; j<meals.length(); j++ ){
                    JSONObject meal = meals.getJSONObject(j);
                    if(meal.getBoolean("isSelected")== true){
                        sb.append("\n");
                        sb.append(meal.getString("title").toUpperCase());
                        sb.append("\n");
                        for(int i=0; i<selectedFoods.length(); i++) {
                            JSONObject row = selectedFoods.getJSONObject(i);
                            JSONObject meal_ = row.getJSONObject("meal");
                            if(meal_.getString("code").equals(meal.getString("code"))){
                                food = row.getString("food");
                                quantity = row.getString("quantity");
                                unit = row.getString("unit");
                                mass = row.getString("mass");
                                sb.append("- " + food + " " + quantity + " " + unit + " (" + mass + " g)");
                                sb.append("\n");
                            }
                        }
                    }
                }
                currMenu = sb.toString();
                end = System.currentTimeMillis();
                elapsedTime = end - start;
            } catch (Exception e){
                Toast.makeText(getActivity(), e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            String METHODNAME = "Get";
            String SOAPACTION = "http://programprehrane.com/app/Get";
            String URL =  "https://www.programprehrane.com/Menues.asmx";

            SoapObject soapObject = new SoapObject(NAMESPACE, METHODNAME);

            SharedPreferences sp = getActivity().getSharedPreferences("Client", Context.MODE_PRIVATE);
            userId = sp.getString("userId", "");
            soapObject.addProperty("userId", userId);
            soapObject.addProperty("id", menuId);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
            try {
                httpTransportSE.call(SOAPACTION, envelope);
                SoapPrimitive soapPrimitive = (SoapPrimitive) envelope.getResponse();
                menu = soapPrimitive.toString();
                if(TextUtils.isEmpty(menu)==false){
                    try{
                         // JSONObject json = new JSONObject(menu);
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
