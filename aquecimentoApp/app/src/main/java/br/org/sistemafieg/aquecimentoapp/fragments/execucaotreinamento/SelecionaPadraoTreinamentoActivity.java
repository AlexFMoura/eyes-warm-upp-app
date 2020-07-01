package br.org.sistemafieg.aquecimentoapp.fragments.execucaotreinamento;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import br.org.sistemafieg.aquecimentoapp.R;
import br.org.sistemafieg.aquecimentoapp.controller.PadraoTreinamentoController;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamento;

public class SelecionaPadraoTreinamentoActivity extends Activity {

    private ListPadraoTreinamentoAdaper padraoTreinamentoArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleciona_padrao_treinamento);

        padraoTreinamentoArrayAdapter = new ListPadraoTreinamentoAdaper(this, R.layout.item_list_seleciona_padrao_treinamento);

        ListView newDevicesListView = (ListView) findViewById(R.id.listViewSelecionaPadraoTreinamento);
        newDevicesListView.setAdapter(padraoTreinamentoArrayAdapter);
        newDevicesListView.setOnItemClickListener(padraoTreinamentoClickListener);

        refreshPadraoTreinamentos();
    }

    private AdapterView.OnItemClickListener padraoTreinamentoClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            Intent it = new Intent();
            it.putExtra("dataSource", padraoTreinamentoArrayAdapter.getItem(position));
            setResult(RESULT_OK,it);

            finish();
        }
    };

    private void refreshPadraoTreinamentos() {
        padraoTreinamentoArrayAdapter.clear();

        class ListPadraoTreinamentoTask extends AsyncTask<Void, Void, List<PadraoTreinamento>> {

            @Override
            protected List<PadraoTreinamento> doInBackground(Void... voids) {
                List<PadraoTreinamento> ds = PadraoTreinamentoController.getInstance(getApplicationContext()).getAll();

                return ds;
            }

            @Override
            protected void onPostExecute(List<PadraoTreinamento> ds) {
                super.onPostExecute(ds);

                padraoTreinamentoArrayAdapter.addAll(ds);
            }
        }

        ListPadraoTreinamentoTask newListPadraoTreinamentoTask = new ListPadraoTreinamentoTask();
        newListPadraoTreinamentoTask.execute();

    }

    private class ListPadraoTreinamentoAdaper extends ArrayAdapter<PadraoTreinamento> {

        private int layout;
        private View.OnClickListener eventClickListener;
        private ListPadraoTreinamentoAdaper(Context context, int resource) {
            super(context, resource);
            this.layout = resource;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final PadraoTreinamento padraoTreinamento = getItem(position);

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewItem viewDeviceItem = new ViewItem();
                viewDeviceItem.icon = (TextView) convertView.findViewById(R.id.textViewItemListSelecionaPadraoTreinamentoIcon);
                viewDeviceItem.padraoTreinamentoNome = (TextView) convertView.findViewById(R.id.textViewItemListSelecionaPadraoTreinamentoNome);

                viewDeviceItem.padraoTreinamento = padraoTreinamento;
                convertView.setTag(viewDeviceItem);
            }

            final ViewItem mainViewDeviceItem = (ViewItem) convertView.getTag();
            mainViewDeviceItem.padraoTreinamentoNome.setText(padraoTreinamento.getNome());
            mainViewDeviceItem.padraoTreinamento = padraoTreinamento;

            if(eventClickListener != null) {
                convertView.setOnClickListener(eventClickListener);
            }

            return convertView;
        }

        public void setOnItemClickListener(View.OnClickListener event) {
            this.eventClickListener = event;
        }


    }

    private class ViewItem {
        TextView icon;
        TextView padraoTreinamentoNome;
        PadraoTreinamento padraoTreinamento;

    }


}
