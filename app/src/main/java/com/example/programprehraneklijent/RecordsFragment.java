package com.example.programprehraneklijent;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class RecordsFragment extends Fragment {

    private static final String NAMESPACE = "http://programprehrane.com/app/";
    String userId = "";
    String clientId = "";
    String result = "";
    private TableLayout tlRecords;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_records, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tlRecords = view.findViewById(R.id.tlRecords);
        if(isNetworkAvailable()==true){
            RecordsFragment.ATgetClientLog atGetClientLog = new RecordsFragment.ATgetClientLog();
            atGetClientLog.execute();
        }else{
            NetworkAlert na = new NetworkAlert();
            na.show(getActivity().getSupportFragmentManager(), "alert");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class ATgetClientLog extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            int id = 0;
            String date = "";
            Double weight = 0.0;
            Double waist = 0.0;
            Double hip = 0.0;

            try{
                JSONArray arr = new JSONArray(result);




               // for(int i=0; i<arr.length(); i++){
                for(int i=arr.length()-1; i>= 0; i--){
                    JSONObject row = arr.getJSONObject(i);
                    id = row.getInt("id");
                    date = row.getString("date").substring(0,10);
                    weight = row.getDouble("weight");
                    waist = row.getDouble("waist");
                    hip = row.getDouble("hip");

                    TableRow tr = new TableRow(getContext());
                    tr.setPadding(0, 15, 0, 0);

                           /* final String id = res.getString(0);
                            tr.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    alertDialogDelete(id);
                                    return false;
                                }
                            });
                            */

                    TextView tvDate = new TextView(getContext());
                    tvDate.setText(date);
                    tvDate.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
                    tr.addView(tvDate);

                    TextView tvWeight = new TextView(getContext());
                    tvWeight.setText(weight.toString());
                    tvWeight.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
                    tr.addView(tvWeight);

                    TextView tvWaist = new TextView(getContext());
                    tvWaist.setText(waist.toString());
                    tvWaist.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
                    tr.addView(tvWaist);

                    TextView tvHip = new TextView(getContext());
                    tvHip.setText(hip.toString());
                    tvHip.setGravity(View.TEXT_ALIGNMENT_GRAVITY);
                    tr.addView(tvHip);

                    tlRecords.addView(tr);
                }
            } catch (Exception e){
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            String METHODNAME = "GetClientLog";
            String SOAPACTION = "http://programprehrane.com/app/GetClientLog";
            String URL =  "https://www.programprehrane.com/ClientsData.asmx";

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
                    try{
                      //  JSONObject json = new JSONObject(result);
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
