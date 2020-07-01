package br.org.sistemafieg.aquecimentoapp.fragments.confdispositivos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.org.sistemafieg.aquecimentoapp.R;
import br.org.sistemafieg.aquecimentoapp.controller.DispositivoConfExercicioController;
import br.org.sistemafieg.aquecimentoapp.controller.PadraoTreinamentoController;
import br.org.sistemafieg.aquecimentoapp.enums.IdentificacaoDispositivoEnum;
import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;

public class EditConfDeviceActivity extends AppCompatActivity {

    private DispositivoConfExercicio dataSource;
    private BluetoothDevice bluetoothDevice;

    private Spinner spinnerIdentificacaoDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_conf_device);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        dataSource = (DispositivoConfExercicio)bundle.get("dataSource");

        // Get the local Bluetooth adapter
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        String  addressDevice = (String)bundle.get("addressDevice");
        for(BluetoothDevice d : pairedDevices) {
            if (d.getAddress().equalsIgnoreCase(addressDevice)) {
                bluetoothDevice = d;
                break;
            }
        }


        TextView textViewIdentificacaoDispositivo = (TextView) findViewById(R.id.textViewIdentificacaoDispositivo);
        textViewIdentificacaoDispositivo.setText(bluetoothDevice.getName());

        spinnerIdentificacaoDispositivo = (Spinner) findViewById(R.id.spinnerIdentificacaoDispositivo);
        ArrayAdapterEnum adapter = new ArrayAdapterEnum(this);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinnerIdentificacaoDispositivo.setAdapter(adapter);
        if(dataSource != null) {
            spinnerIdentificacaoDispositivo.setSelection(IdentificacaoDispositivoEnum.findByName(dataSource.getIdentificacaoDevice()).ordinal()+1);
        }

        Button buttonDesparear = (Button) findViewById(R.id.buttonDesparear);
        buttonDesparear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unpairDevice(bluetoothDevice);
            }
        });


    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK,returnIntent);
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            salvar();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);

            remover();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        salvar();
        super.onBackPressed();
    }

    private void salvar() {
        final IdentificacaoDispositivoEnum identificacaoDispositivoEnum = IdentificacaoDispositivoEnum.findByLabel((String) spinnerIdentificacaoDispositivo.getSelectedItem());

        if (identificacaoDispositivoEnum == null) {
            ((TextView) spinnerIdentificacaoDispositivo.getSelectedView()).setError("Campo obrigatório");
            spinnerIdentificacaoDispositivo.requestFocus();
            return;
        }

        class SaveTask extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    if(dataSource == null) {
                        dataSource = new DispositivoConfExercicio();
                    }
                    dataSource.setNameDevice(bluetoothDevice.getName());
                    dataSource.setAddressDevice(bluetoothDevice.getAddress());
                    dataSource.setIdentificacaoDevice(identificacaoDispositivoEnum.name());

                    dataSource = DispositivoConfExercicioController.getInstance(getApplicationContext()).insertOrUpdate(dataSource);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return ex.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String  value) {
                super.onPostExecute(value);

                if(value != null) {
                    Toast.makeText(getApplicationContext(), value, Toast.LENGTH_LONG).show();
                }
            }
        }

        SaveTask st = new SaveTask();
        st.execute();

    }

    private void remover() {
        class RemoveTask extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {
                try{
                    DispositivoConfExercicioController.getInstance(getApplicationContext()).delete(dataSource);
                    PadraoTreinamentoController.getInstance(getApplicationContext()).deletePadraoTreinamentoDispItemPorDispositivoConfExercicio(dataSource.getId());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return ex.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String  value) {
                super.onPostExecute(value);
                finish();
            }
        }

        RemoveTask st = new RemoveTask();
        st.execute();
    }

    private AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

}

class ArrayAdapterEnum extends ArrayAdapter<String> {

    private static List<String> listValues;

    static {
        listValues = new ArrayList<String>();
        listValues.add("");
        for(IdentificacaoDispositivoEnum item : IdentificacaoDispositivoEnum.values()) {
            listValues.add(item.getLabel());
        }
    }


    public ArrayAdapterEnum(Context context) {
        super(context, 0, listValues);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CheckedTextView text= (CheckedTextView) convertView;

        if (text== null) {
            text = (CheckedTextView) LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item,  parent, false);
        }

        text.setText(getItem(position));//.getLabel());
        return text;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        CheckedTextView text = (CheckedTextView) convertView;

        if (text == null) {
            text = (CheckedTextView) LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item,  parent, false);
        }

        text.setText(getItem(position));//.getLabel());
        return text;
    }
}