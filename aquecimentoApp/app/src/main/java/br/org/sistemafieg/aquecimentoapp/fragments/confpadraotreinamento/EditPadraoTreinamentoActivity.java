package br.org.sistemafieg.aquecimentoapp.fragments.confpadraotreinamento;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.org.sistemafieg.aquecimentoapp.R;
import br.org.sistemafieg.aquecimentoapp.components.picker.HourMinSecPicker;
import br.org.sistemafieg.aquecimentoapp.components.picker.SecondsPicker;
import br.org.sistemafieg.aquecimentoapp.controller.DispositivoConfExercicioController;
import br.org.sistemafieg.aquecimentoapp.controller.PadraoTreinamentoController;
import br.org.sistemafieg.aquecimentoapp.dto.PadraoTreinamentoDispItemConfDto;
import br.org.sistemafieg.aquecimentoapp.enums.IdentificacaoDispositivoEnum;
import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamento;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamentoDispItem;
import br.org.sistemafieg.aquecimentoapp.util.UtilConvert;

public class EditPadraoTreinamentoActivity extends AppCompatActivity {

    // Intent request codes
    private static final int REQUEST_EDIT_ITEM_PADRAO_TREINAMENTO = 1;


    private PadraoTreinamento dataSource;
    private Boolean alertarAplicarPadraoConfDispositivos = null;

    private ListPadraoTreinamentoDispItemAdaper listPadraoTreinamentoDispItemAdaper;
    private EditText editTextNomePadraoTreinamento;
    private EditText editTextDescricaoPadraoTreinamento;
    private SecondsPicker secondsPickerTempoTrocaEntreDispositivos;
    private HourMinSecPicker hourMinSecPickerTempoExecucaoTreinamento;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_padrao_treinamento);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        dataSource = (PadraoTreinamento)bundle.get("dataSource");

        editTextNomePadraoTreinamento = (EditText) findViewById(R.id.editTextNomePadraoTreinamento);
        editTextDescricaoPadraoTreinamento = (EditText) findViewById(R.id.editTextDescricaoPadraoTreinamento);
        secondsPickerTempoTrocaEntreDispositivos = (SecondsPicker) findViewById(R.id.secondsPickerTempoTrocaEntreDispositivos);
        hourMinSecPickerTempoExecucaoTreinamento = (HourMinSecPicker) findViewById(R.id.hourMinSecPickerTempoExecucaoTreinamento);
        listPadraoTreinamentoDispItemAdaper = new ListPadraoTreinamentoDispItemAdaper(getApplicationContext(), R.layout.item_list_edit_padrao_treinamento);
        listPadraoTreinamentoDispItemAdaper.setActivityOpenButtonClick(EditItemPadraoTreinamentoActivity.class);

        // Find and set up the ListView for paired devices
        ListView dispConfExercListView = (ListView) findViewById(R.id.listViewPadraoTreinamentoDispositivoConfExerciciosItens);
        dispConfExercListView.setAdapter(listPadraoTreinamentoDispItemAdaper);
        buscarDataSource(dataSource.getId());
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK,returnIntent);
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_padrao_treinamento_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_edit_padrao_treinamento_activity_salvar: {
                salvar();
                return true;
            }
            case R.id.menu_edit_padrao_treinamento_activity_cancelar: {
                finish();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_EDIT_ITEM_PADRAO_TREINAMENTO:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    PadraoTreinamentoDispItemConfDto dto = (PadraoTreinamentoDispItemConfDto)bundle.get("dataSource");
                    if (dataSource != null && dataSource.getPadraoTreinamentoDispItems() != null) {
                        for (PadraoTreinamentoDispItem item : dataSource.getPadraoTreinamentoDispItems()) {
                            if (item.getIdDispositivoConfExercicio().equals(dto.getDispositivoConfExercicio().getId())) {
                                item.setAtivarBeep(dto.getPadraoTreinamentoDispItem().getAtivarBeep());
                                item.setTempoVisualizacaoNumeroExercicio(dto.getPadraoTreinamentoDispItem().getTempoVisualizacaoNumeroExercicio());
                                break;
                            }
                        }
                    }

                    if(alertarAplicarPadraoConfDispositivos) {
                        alertAplicarPadraoConfDispositivos(dto);
                    }

                    refreshDispositivoConfExercicios();
                }
                break;

        }

    }

    private void alertAplicarPadraoConfDispositivos(PadraoTreinamentoDispItemConfDto dtoConf) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Atenção!");
        adb.setMessage(getString(R.string.msg_aplicar_padrao_conf_dispositivos));
        adb.setIcon(android.R.drawable.ic_dialog_info);
        adb.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        adb.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (dataSource != null && dataSource.getPadraoTreinamentoDispItems() != null) {
                    for (PadraoTreinamentoDispItem item : dataSource.getPadraoTreinamentoDispItems()) {
                        if (!item.getIdDispositivoConfExercicio().equals(dtoConf.getDispositivoConfExercicio().getId())) {
                            item.setAtivarBeep(dtoConf.getPadraoTreinamentoDispItem().getAtivarBeep());
                            item.setTempoVisualizacaoNumeroExercicio(dtoConf.getPadraoTreinamentoDispItem().getTempoVisualizacaoNumeroExercicio());
                        }
                    }
                }

                listPadraoTreinamentoDispItemAdaper.notifyDataSetChanged();
            }
        });

        adb.show();
        alertarAplicarPadraoConfDispositivos = false;
    }

    private void carregaDadosTela() {
        if(dataSource != null) {
            editTextNomePadraoTreinamento.setText(dataSource.getNome());
            editTextDescricaoPadraoTreinamento.setText(dataSource.getDescricao());
            secondsPickerTempoTrocaEntreDispositivos.setValue(UtilConvert.convertDoubleToFloat(dataSource.getTempoTrocaEntreDispositivosMillis()));
            hourMinSecPickerTempoExecucaoTreinamento.setMilliseconds(UtilConvert.convertDoubleToFloat(dataSource.getTempoRepeticaoExercicioMillis()));
        }
    }


    private void buscarDataSource(Long id) {
        class GetDataSourceTask extends AsyncTask<Void, Void, PadraoTreinamento> {

            @Override
            protected PadraoTreinamento doInBackground(Void... voids) {
                PadraoTreinamento ds = PadraoTreinamentoController.getInstance(getApplicationContext()).find(id);

                return ds;
            }

            @Override
            protected void onPostExecute(PadraoTreinamento ds) {
                super.onPostExecute(ds);

                dataSource = ds;
                if(dataSource == null) {
                    dataSource = new PadraoTreinamento();
                }
                carregaDadosTela();
                refreshDispositivoConfExercicios();
            }
        }

        if(dataSource != null && dataSource.getId() != null) {
            GetDataSourceTask newGetDataSourceTask = new GetDataSourceTask();
            newGetDataSourceTask.execute();
        } else {
            refreshDispositivoConfExercicios();
        }
    }

    private void salvar() {
        final String nomePadraoTreinamento = editTextNomePadraoTreinamento.getText().toString().trim();
        final String descricaoPadraoTreinamento = editTextDescricaoPadraoTreinamento.getText().toString().trim();
        final Float tempoRepeticaoExercicio = hourMinSecPickerTempoExecucaoTreinamento.getMilliseconds();
        final Float tempoTrocaEntreDispositivos = secondsPickerTempoTrocaEntreDispositivos.getValue();

        if (nomePadraoTreinamento.isEmpty()) {
            editTextNomePadraoTreinamento.setError("Campo obrigatório");
            editTextNomePadraoTreinamento.requestFocus();
            return;
        }

        if (descricaoPadraoTreinamento.isEmpty()) {
            editTextDescricaoPadraoTreinamento.setError("Campo obrigatório");
            editTextDescricaoPadraoTreinamento.requestFocus();
            return;
        }

        if (tempoRepeticaoExercicio.equals(0.0)) {
            hourMinSecPickerTempoExecucaoTreinamento.requestFocus();
            Toast.makeText(this, "O campo tempo de repetição é obrigatório!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tempoTrocaEntreDispositivos.equals(0.0)) {
            secondsPickerTempoTrocaEntreDispositivos.requestFocus();
            Toast.makeText(this, "O campo tempo de troca entre dispositivos é obrigatório!", Toast.LENGTH_SHORT).show();
            return;
        }

        class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                if(dataSource == null) {
                    dataSource = new PadraoTreinamento();
                }

                dataSource.setNome(nomePadraoTreinamento);
                dataSource.setDescricao(descricaoPadraoTreinamento);
                dataSource.setTempoRepeticaoExercicioMillis(Double.valueOf(tempoRepeticaoExercicio));
                dataSource.setPadraoTreinamentoDispItems(getPadraoTreinamentoDispItems());
                dataSource.setTempoTrocaEntreDispositivosMillis(Double.valueOf(secondsPickerTempoTrocaEntreDispositivos.getValue()));

                PadraoTreinamentoController.getInstance(getApplicationContext()).insertOrUpdate(dataSource);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
            }

            private Set<PadraoTreinamentoDispItem> getPadraoTreinamentoDispItems() {
                Set<PadraoTreinamentoDispItem> result = new HashSet<PadraoTreinamentoDispItem>();

                for(PadraoTreinamentoDispItemConfDto dto : listPadraoTreinamentoDispItemAdaper.getAll()) {
                    result.add(dto.getPadraoTreinamentoDispItem());
                }

                return result;
            }
        }

        SaveTask st = new SaveTask();
        st.execute();

    }

    private void refreshDispositivoConfExercicios() {
        listPadraoTreinamentoDispItemAdaper.clear();

        class ListDispositivosConfiguradosTask extends AsyncTask<Void, Void, List<DispositivoConfExercicio>> {

            @Override
            protected List<DispositivoConfExercicio> doInBackground(Void... voids) {
                List<DispositivoConfExercicio> ds = DispositivoConfExercicioController.getInstance(getApplicationContext()).getAll();

                return ds;
            }

            @Override
            protected void onPostExecute(List<DispositivoConfExercicio> ds) {
                super.onPostExecute(ds);

                if(dataSource.getPadraoTreinamentoDispItems() == null) {
                    dataSource.setPadraoTreinamentoDispItems(new HashSet<PadraoTreinamentoDispItem>());
                }

                for(DispositivoConfExercicio dispConfEx : ds) {
                    PadraoTreinamentoDispItemConfDto dto = new PadraoTreinamentoDispItemConfDto();
                    dto.setDispositivoConfExercicio(dispConfEx);

                    PadraoTreinamentoDispItem ptdi = getPadraoTreinamentoDispItem(dispConfEx.getId());
                    dto.setPadraoTreinamentoDispItem(ptdi);

                    dataSource.getPadraoTreinamentoDispItems().add(ptdi);
                    listPadraoTreinamentoDispItemAdaper.add(dto);
                }

                resizeListViewDispConf();
            }

            private PadraoTreinamentoDispItem getPadraoTreinamentoDispItem(Long idDispositivoConfExercicio) {
                for (PadraoTreinamentoDispItem item : dataSource.getPadraoTreinamentoDispItems()) {
                    if (item.getIdDispositivoConfExercicio().equals(idDispositivoConfExercicio)) {
                        return item;
                    }
                }

                PadraoTreinamentoDispItem result = new PadraoTreinamentoDispItem();
                result.setIdDispositivoConfExercicio(idDispositivoConfExercicio);
                result.setAtivarBeep(true);
                result.setTempoVisualizacaoNumeroExercicio(0.0);
                return result;
            }




        }

        ListDispositivosConfiguradosTask newListDispositivosConfiguradosTask = new ListDispositivosConfiguradosTask();
        newListDispositivosConfiguradosTask.execute();

    }

    private void resizeListViewDispConf() {
        ListView listview = (ListView) findViewById(R.id.listViewPadraoTreinamentoDispositivoConfExerciciosItens);
        ListAdapter listadp = ((ListView) findViewById(R.id.listViewPadraoTreinamentoDispositivoConfExerciciosItens)).getAdapter();
        if (listadp != null) {
            int totalHeight = 0;
            for (int i = 0; i < listadp.getCount(); i++) {
                View listItem = listadp.getView(i, null, listview);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listview.getLayoutParams();
            params.height = totalHeight + (listview.getDividerHeight() * (listadp.getCount() - 1));
            listview.setLayoutParams(params);
            listview.requestLayout();
        }
    }



    private class ListPadraoTreinamentoDispItemAdaper extends ArrayAdapter<PadraoTreinamentoDispItemConfDto> {

        private DecimalFormat df2 = new DecimalFormat("#.##");
        private int layout;
        private View.OnClickListener eventClickListener;
        private Class<? extends AppCompatActivity> activityOpenButtonClick;
        private ListPadraoTreinamentoDispItemAdaper(Context context, int resource) {
            super(context, resource);
            this.layout = resource;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final PadraoTreinamentoDispItemConfDto padraoTreinamentoDispItemConfDto = getItem(position);

            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewItem viewDeviceItem = new ViewItem();
                viewDeviceItem.icon = (TextView) convertView.findViewById(R.id.textViewItemListEditPadraoTreinamentoIcon);
                viewDeviceItem.padraoTreinamentoNome = (TextView) convertView.findViewById(R.id.textViewItemListEditPadraoTreinamentoNome);
                viewDeviceItem.padraoTreinamentoTempoVisualizacaoNumero = (TextView) convertView.findViewById(R.id.textViewItemListEditPadraoTreinamentoTempoVisualizacaoNumero);
                viewDeviceItem.padraoTreinamentoIdentificacao = (TextView) convertView.findViewById(R.id.textViewItemListEditPadraoTreinamentoIdentificacao);
                viewDeviceItem.padraoTreinamentoAtivarBeep = (TextView) convertView.findViewById(R.id.textViewItemListEditPadraoTreinamentoAtivarBeep);

                viewDeviceItem.buttonAction = (Button) convertView.findViewById(R.id.imageButtonItemListEditPadraoTreinamentoAction);
                viewDeviceItem.padraoTreinamentoDispItemConfDto = padraoTreinamentoDispItemConfDto;
                convertView.setTag(viewDeviceItem);
            }

            final ViewItem mainViewDeviceItem = (ViewItem) convertView.getTag();
            mainViewDeviceItem.padraoTreinamentoDispItemConfDto = padraoTreinamentoDispItemConfDto;
            mainViewDeviceItem.padraoTreinamentoNome.setText(padraoTreinamentoDispItemConfDto.getDispositivoConfExercicio().getNameDevice() + "\n"
                    + padraoTreinamentoDispItemConfDto.getDispositivoConfExercicio().getAddressDevice());
            mainViewDeviceItem.padraoTreinamentoIdentificacao.setText(IdentificacaoDispositivoEnum.findByName(padraoTreinamentoDispItemConfDto.getDispositivoConfExercicio().getIdentificacaoDevice()).getLabel());
            mainViewDeviceItem.padraoTreinamentoTempoVisualizacaoNumero.setText("Tempo Visualização"+ "\n"+
                    df2.format(padraoTreinamentoDispItemConfDto.getPadraoTreinamentoDispItem().getTempoVisualizacaoNumeroExercicio() / 1000));
            mainViewDeviceItem.padraoTreinamentoAtivarBeep.setText("Beep " +(padraoTreinamentoDispItemConfDto.getPadraoTreinamentoDispItem().getAtivarBeep()
                                                                            ? "Ativado" : "Desativado"));


            if(activityOpenButtonClick != null) {
                mainViewDeviceItem.buttonAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent it = new Intent(EditPadraoTreinamentoActivity.this, activityOpenButtonClick);
                        it.putExtra("dataSource", padraoTreinamentoDispItemConfDto);
                        it.putExtra("index", position);
                        startActivityForResult(it, REQUEST_EDIT_ITEM_PADRAO_TREINAMENTO);

                        if(alertarAplicarPadraoConfDispositivos == null) {
                            alertarAplicarPadraoConfDispositivos = true;
                        }
                    }
                });
            } else {
                mainViewDeviceItem.buttonAction.setVisibility(View.GONE);
            }

            if(eventClickListener != null) {
                convertView.setOnClickListener(eventClickListener);
            }

            return convertView;
        }

        public void setOnItemClickListener(View.OnClickListener event) {
            this.eventClickListener = event;
        }

        public void setActivityOpenButtonClick(Class<? extends AppCompatActivity> activityOpenButtonClick) {
            this.activityOpenButtonClick = activityOpenButtonClick;
        }

        public List<PadraoTreinamentoDispItemConfDto> getAll() {
            List<PadraoTreinamentoDispItemConfDto> result = new ArrayList<PadraoTreinamentoDispItemConfDto>();

            for (int i=0; i<getCount(); i++){
                result.add(getItem(i));
            }

            return result;
        }

        public PadraoTreinamentoDispItemConfDto get(int index) {
            return getAll().get(index);
        }

        public void set(int index, PadraoTreinamentoDispItemConfDto dto) {
            getAll().set(index, dto);
        }

    }

    private class ViewItem {
        TextView icon;
        TextView padraoTreinamentoNome;
        TextView padraoTreinamentoTempoVisualizacaoNumero;
        TextView padraoTreinamentoIdentificacao;
        TextView padraoTreinamentoAtivarBeep;
        Button buttonAction;
        PadraoTreinamentoDispItemConfDto padraoTreinamentoDispItemConfDto;

    }



}
