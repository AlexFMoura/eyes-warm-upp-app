package br.org.sistemafieg.aquecimentoclientapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import br.org.sistemafieg.aquecimentoclientapp.controller.ConfDispositivoController;
import br.org.sistemafieg.aquecimentoclientapp.enums.IdentificacaoDispositivoEnum;
import br.org.sistemafieg.aquecimentoclientapp.model.ConfDispositivo;
import br.org.sistemafieg.aquecimentoclientapp.util.Constants;

public class MainActivity extends AppCompatActivity {

    private ConfDispositivo dataSource;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Spinner spinnerIdentificacaoDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));


        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Activity activity = this;
            Toast.makeText(activity, "Bluetooth está desabilitado", Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }

        findViewById(R.id.layoutMain).setVisibility(View.GONE);
        buscarDataSource();

        spinnerIdentificacaoDispositivo = (Spinner) findViewById(R.id.spinnerIdentificacaoDispositivo);
        ArrayAdapterIdentificacaoDispositivoEnum adapterIdentificacaoDispositivoEnum = new ArrayAdapterIdentificacaoDispositivoEnum(this);
        adapterIdentificacaoDispositivoEnum.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinnerIdentificacaoDispositivo.setAdapter(adapterIdentificacaoDispositivoEnum);

        Button buttonConectar= (Button) findViewById(R.id.buttonConectar);
        buttonConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               salvar();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
           case Constants.REQUEST_CHANGE_DEVICE:
               findViewById(R.id.layoutMain).setVisibility(View.VISIBLE);
           case Constants.REQUEST_ENABLE_BT:
               // When the request to enable Bluetooth returns
               if (resultCode != Activity.RESULT_OK) {
                   // User did not enable Bluetooth or an error occurred
                   Activity activity = this;
                   Toast.makeText(activity, getString(R.string.msg_bluetooth_desativado), Toast.LENGTH_LONG).show();
                   activity.finish();
               }
        }
    }

    private void startDisplayComunicao(ConfDispositivo confDispositivo) {
        Intent it = new Intent(MainActivity.this, DisplayComunicaoActivity.class);
        it.putExtra("dataSource", confDispositivo);
        startActivityForResult(it, Constants.REQUEST_CHANGE_DEVICE);
    }

    private void buscarDataSource() {
        class GetDataSourceTask extends AsyncTask<Void, Void, ConfDispositivo> {

            @Override
            protected ConfDispositivo doInBackground(Void... voids) {
                ConfDispositivo ds = ConfDispositivoController.getInstance(getApplicationContext()).buscarConf();

                return ds;
            }

            @Override
            protected void onPostExecute(ConfDispositivo ds) {
                super.onPostExecute(ds);

                dataSource = ds;
                if(dataSource != null) {
                    startDisplayComunicao(dataSource);
                    return;
                }

                findViewById(R.id.layoutMain).setVisibility(View.VISIBLE);
                dataSource = new ConfDispositivo();

            }
        }

        if(dataSource == null) {
            GetDataSourceTask newGetDataSourceTask = new GetDataSourceTask();
            newGetDataSourceTask.execute();
        }
    }

    private void salvar() {
        final IdentificacaoDispositivoEnum identificacaoDispositivoEnum = (IdentificacaoDispositivoEnum) spinnerIdentificacaoDispositivo.getSelectedItem();

        if (identificacaoDispositivoEnum == null) {
            ((TextView) spinnerIdentificacaoDispositivo.getSelectedView()).setError("Campo obrigatório");
            spinnerIdentificacaoDispositivo.requestFocus();
            return;
        }


        class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                dataSource.setIdentificacaoDevice(identificacaoDispositivoEnum.name());
                dataSource.setCorLayout(identificacaoDispositivoEnum.getColor());

                ConfDispositivoController.getInstance(getApplicationContext()).insertOrUpdate(dataSource);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                startDisplayComunicao(dataSource);
            }
        }

        SaveTask st = new SaveTask();
        st.execute();

    }



}


class ArrayAdapterIdentificacaoDispositivoEnum extends ArrayAdapter<IdentificacaoDispositivoEnum> {

    public ArrayAdapterIdentificacaoDispositivoEnum(Context context) {
        super(context, 0, IdentificacaoDispositivoEnum.values());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CheckedTextView text= (CheckedTextView) convertView;

        if (text== null) {
            text = (CheckedTextView) LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item,  parent, false);
        }

        text.setTextColor(Color.WHITE);
        text.setTextSize(18);
        text.setText(getItem(position).getLabel().toUpperCase());
        return text;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        CheckedTextView text = (CheckedTextView) convertView;

        if (text == null) {
            text = (CheckedTextView) LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item,  parent, false);
        }

        text.setText(getItem(position).getLabel());
        return text;
    }
}


