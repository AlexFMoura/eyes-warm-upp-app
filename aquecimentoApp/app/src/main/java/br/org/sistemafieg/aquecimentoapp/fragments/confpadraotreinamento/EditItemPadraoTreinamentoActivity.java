package br.org.sistemafieg.aquecimentoapp.fragments.confpadraotreinamento;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;

import br.org.sistemafieg.aquecimentoapp.R;
import br.org.sistemafieg.aquecimentoapp.components.picker.SecondsPicker;
import br.org.sistemafieg.aquecimentoapp.dto.PadraoTreinamentoDispItemConfDto;

public class EditItemPadraoTreinamentoActivity extends AppCompatActivity {

    private PadraoTreinamentoDispItemConfDto dataSource;

    private Switch switchAtivarBeep;
    private SecondsPicker secondsPickerTempoVisualizacaoNumero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item_padrao_treinamento);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        dataSource = (PadraoTreinamentoDispItemConfDto)bundle.get("dataSource");


        TextView textViewIdentificacaoDispositivo = (TextView) findViewById(R.id.textViewIdentificacaoDispositivo);
        textViewIdentificacaoDispositivo.setText(dataSource.getDispositivoConfExercicio().getNameDevice());

        switchAtivarBeep = (Switch) findViewById(R.id.switchAtivarBeep);
        switchAtivarBeep.setChecked(dataSource.getPadraoTreinamentoDispItem().getAtivarBeep());

        secondsPickerTempoVisualizacaoNumero = (SecondsPicker) findViewById(R.id.secondsPickerTempoVisualizacaoNumero);
        secondsPickerTempoVisualizacaoNumero.setValue(dataSource.getPadraoTreinamentoDispItem().getTempoVisualizacaoNumeroExercicio()
                != null ? dataSource.getPadraoTreinamentoDispItem().getTempoVisualizacaoNumeroExercicio().floatValue() : 0.0f);
    }

    @Override
    public void finish() {
        Intent it = new Intent();
        it.putExtra("dataSource", dataSource);
        setResult(RESULT_OK,it);
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

    @Override
    public void onBackPressed() {
        salvar();
        super.onBackPressed();
    }

    private void salvar() {
        final Float tempoVisualizacaoNumero = secondsPickerTempoVisualizacaoNumero.getValue();

        if (tempoVisualizacaoNumero == null || tempoVisualizacaoNumero == 0.0f) {
            secondsPickerTempoVisualizacaoNumero.getViewTextViewSeconds().setError("Campo obrigatório");
            secondsPickerTempoVisualizacaoNumero.getViewTextViewSeconds().requestFocus();
            return;
        }

        dataSource.getPadraoTreinamentoDispItem().setTempoVisualizacaoNumeroExercicio(tempoVisualizacaoNumero.doubleValue());
        dataSource.getPadraoTreinamentoDispItem().setAtivarBeep(switchAtivarBeep.isChecked());
    }


}
