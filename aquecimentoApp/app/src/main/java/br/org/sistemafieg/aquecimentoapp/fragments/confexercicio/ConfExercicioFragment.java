package br.org.sistemafieg.aquecimentoapp.fragments.confexercicio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import br.org.sistemafieg.aquecimentoapp.MainActivity;
import br.org.sistemafieg.aquecimentoapp.R;
import br.org.sistemafieg.aquecimentoapp.controller.ParametrosConfExercicioController;
import br.org.sistemafieg.aquecimentoapp.model.ParametrosConfExercicio;
import br.org.sistemafieg.aquecimentoapp.util.UtilConvert;

public class ConfExercicioFragment extends Fragment {

    private ParametrosConfExercicio dataSource;

    private EditText editTextQtdDispositivosClientes;
    private EditText editTextSequenciaNumeros;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conf_exercicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Conf. Exercício");

        editTextQtdDispositivosClientes = (EditText) getView().findViewById(R.id.editTextQtdDispositivosClientes);
        editTextSequenciaNumeros = (EditText) getView().findViewById(R.id.editTextSequenciaNumeros);

        buscarDataSource();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_conf_exercicio, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_conf_exercicio_salvar: {
                salvar();
                return true;
            }
            case R.id.menu_conf_exercicio_cancelar: {
                getActivity().onBackPressed();
                return true;
            }
        }
        return false;
    }

    private void carregaDadosTela() {
        if(dataSource != null) {
            editTextQtdDispositivosClientes.setText(UtilConvert.convertIntegerToString(dataSource.getQtdDispositivoCliente()));
            editTextSequenciaNumeros.setText(dataSource.getSequenciaNumeroExercicio());
        }
    }

    private void buscarDataSource() {
        class GetDataSourceTask extends AsyncTask<Void, Void, ParametrosConfExercicio> {

            @Override
            protected ParametrosConfExercicio doInBackground(Void... voids) {
                ParametrosConfExercicio ds = ParametrosConfExercicioController.getInstance(getContext()).find();

                return ds;
            }

            @Override
            protected void onPostExecute(ParametrosConfExercicio ds) {
                super.onPostExecute(ds);

                dataSource = ds;
                if(dataSource == null) {
                    dataSource = new ParametrosConfExercicio();
                }
                carregaDadosTela();
            }
        }

        if(dataSource == null) {
            GetDataSourceTask newGetDataSourceTask = new GetDataSourceTask();
            newGetDataSourceTask.execute();
        }
    }

    private void salvar() {
        final String qtdDispositivosClientes = editTextQtdDispositivosClientes.getText().toString().trim();
        final String sequenciaNumeros = editTextSequenciaNumeros.getText().toString().trim();

        if (qtdDispositivosClientes.isEmpty()) {
            editTextQtdDispositivosClientes.setError("Campo obrigatório");
            editTextQtdDispositivosClientes.requestFocus();
            return;
        }

        if (sequenciaNumeros.isEmpty()) {
            editTextSequenciaNumeros.setError("Campo obrigatório");
            editTextSequenciaNumeros.requestFocus();
            return;
        }

        class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                dataSource.setQtdDispositivoCliente(Integer.valueOf(qtdDispositivosClientes));
                dataSource.setSequenciaNumeroExercicio(sequenciaNumeros);

                ParametrosConfExercicioController.getInstance(getContext()).insertOrUpdate(dataSource);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getActivity().onBackPressed();
            }
        }

        SaveTask st = new SaveTask();
        st.execute();

    }

}


