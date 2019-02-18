package com.ahmadelbaz.remember;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    Calendar c = Calendar.getInstance();
    int hour = c.get(Calendar.HOUR_OF_DAY);
    int minute = c.get(Calendar.MINUTE);

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getActivity(), hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));

    }
}