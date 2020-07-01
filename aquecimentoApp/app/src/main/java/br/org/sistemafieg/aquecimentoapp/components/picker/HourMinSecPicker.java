package br.org.sistemafieg.aquecimentoapp.components.picker;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import br.org.sistemafieg.aquecimentoapp.R;

public class HourMinSecPicker extends LinearLayout {

    private Context context;
    private NumberPicker numpickerHours;
    private NumberPicker numpickerMinutes;
    private NumberPicker numpickerSeconds;

    public HourMinSecPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.hour_min_sec_picker,this);

        int[] sets = {R.attr.value};
        TypedArray typedArray = context.obtainStyledAttributes(attrs, sets);

        Float value = typedArray.getFloat(0, 0.0f);
        typedArray.recycle();

        numpickerHours = (NumberPicker) findViewById(R.id.numpicker_hours);
        numpickerMinutes = (NumberPicker) findViewById(R.id.numpicker_minutes);
        numpickerSeconds = (NumberPicker) findViewById(R.id.numpicker_seconds);

        numpickerHours.setMinValue(0);
        numpickerHours.setMaxValue(24);

        numpickerMinutes.setMinValue(0);
        numpickerMinutes.setMaxValue(59);

        numpickerSeconds.setMinValue(0);
        numpickerSeconds.setMaxValue(59);
    }

    public Float getMilliseconds() {
        int hour = numpickerHours.getValue();
        int minutes = numpickerMinutes.getValue();
        int seconds = numpickerSeconds.getValue();

        Float result = Long.valueOf((hour*60*60*1000) + (minutes*60*1000) + (seconds*1000)).floatValue();
        return result;
    }

    public void setMilliseconds(Float value) {
        int hour = (int)(value / 3600000);
        int minutes = (int)(value % 3600000) / 60000;
        int seconds = (int)(value % 60000) / 1000;

        numpickerHours.setValue(hour);
        numpickerMinutes.setValue(minutes);
        numpickerSeconds.setValue(seconds);
    }

    public void requestFocusEdit() {
        numpickerHours.requestFocus();
    }







}
