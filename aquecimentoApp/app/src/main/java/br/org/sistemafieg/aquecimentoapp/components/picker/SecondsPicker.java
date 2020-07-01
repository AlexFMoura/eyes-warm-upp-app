package br.org.sistemafieg.aquecimentoapp.components.picker;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import br.org.sistemafieg.aquecimentoapp.R;

public class SecondsPicker extends LinearLayout {

    private Context context;

    public SecondsPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.seconds_picker,this);

        int[] sets = {R.attr.value};
        TypedArray typedArray = context.obtainStyledAttributes(attrs, sets);

        Float value = typedArray.getFloat(0, 0.0f);
        typedArray.recycle();

        ((TextView) findViewById(R.id.textViewSeconds)).setText("0");
        ((Button) findViewById(R.id.btnDown)).setOnClickListener(new OnClickDownListener((TextView) findViewById(R.id.textViewSeconds)));
        ((Button) findViewById(R.id.btnUp)).setOnClickListener(new OnClickUpListener(findViewById(R.id.textViewSeconds)));

        setValue(value);
    }

    public Float getValue() {
        try{
            return Float.valueOf(((TextView)findViewById(R.id.textViewSeconds)).getText().toString().replace(",", ".")) * 1000;
        } catch (Exception ex) {
            return 0.0f;
        }
    }

    public void setValue(Float value) {
        ((TextView)findViewById(R.id.textViewSeconds)).setText((value == null || value.equals(0.0f)) ? "0" : Float.valueOf(value / 1000).toString().replace(".", ","));
    }

    public TextView getViewTextViewSeconds() {
        return (TextView) findViewById(R.id.textViewSeconds);
    }

    private class OnClickDownListener implements View.OnClickListener {

        private TextView textView;
        private final DecimalFormat df2 = new DecimalFormat("#.##");

        public OnClickDownListener(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void onClick(View v) {
            Double value = Double.valueOf(this.textView.getText().toString().isEmpty() ? "0" : this.textView.getText().toString().replace(",", "."));
            if(value.equals(0.0)) {
                return;
            }

            this.textView.setText(df2.format((value-0.1)));
        }
    }

    private class OnClickUpListener implements View.OnClickListener {

        private TextView textView;
        private final DecimalFormat df2 = new DecimalFormat("#.##");

        public OnClickUpListener(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void onClick(View v) {
            Double value = Double.valueOf(this.textView.getText().toString().isEmpty() ? "0" : this.textView.getText().toString().replace(",", "."));
            this.textView.setText(df2.format((value+0.1)));
        }
    }
}
