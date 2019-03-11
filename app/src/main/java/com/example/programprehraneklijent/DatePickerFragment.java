package com.example.programprehraneklijent;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);
        return dpd;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date = year + "-" + FormatDateStr(month + 1) + "-" + FormatDateStr(day);
        EditText et = getActivity().findViewById(R.id.etDate);
        et.setText(date);
    }

    private String FormatDateStr(int x){
        return x < 10 ? "0" + x : Integer.toString(x);
    }

}




