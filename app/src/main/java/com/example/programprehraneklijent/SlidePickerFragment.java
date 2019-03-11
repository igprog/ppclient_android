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

public class SlidePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                AlertDialog.THEME_HOLO_LIGHT,this,year,month,day);

        c.add(Calendar.DATE, -1825);
        dpd.getDatePicker().setMaxDate(c.getTimeInMillis());
        c.add(Calendar.DATE, -29200);
        dpd.getDatePicker().setMinDate(c.getTimeInMillis());


        return  dpd;
    }

    public void onDateSet(DatePicker view, int year, int month, int day){

        EditText etDate = getActivity().findViewById(R.id.etBirthDate);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month, day, 0, 0, 0);
        Date chosenDate = cal.getTime();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
        String formattedDate = df.format(chosenDate);

        etDate.setText(formattedDate);
    }
}
