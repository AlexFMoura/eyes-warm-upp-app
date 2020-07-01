package br.org.sistemafieg.aquecimentoapp.fragments.confpadraotreinamento;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.org.sistemafieg.aquecimentoapp.MainActivity;
import br.org.sistemafieg.aquecimentoapp.R;
import br.org.sistemafieg.aquecimentoapp.components.swipe.IDrawButtons;
import br.org.sistemafieg.aquecimentoapp.components.swipe.SwipeController;
import br.org.sistemafieg.aquecimentoapp.components.swipe.SwipeControllerActions;
import br.org.sistemafieg.aquecimentoapp.controller.PadraoTreinamentoController;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamento;


public class ConfPadraoTreinamentoFragment extends Fragment {
    // Intent request codes
    private static final int REQUEST_EDIT_PADRAO_TREINAMENTO= 1;

    /**
     * Member fields
     */
    private ListPadraoTreinamentoAdapter listPadraoTreinamentoAdapter;

    // Layout Views
    private RecyclerView recyclerView;
    private SwipeController swipeController = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conf_padrao_treinamento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Conf. Treinamento");

        listPadraoTreinamentoAdapter = new ListPadraoTreinamentoAdapter();
        refreshPadraoTreinamentos();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerViewConfPadraoTreinamento);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(listPadraoTreinamentoAdapter);

        swipeController = new SwipeController(getContext(), new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                final PadraoTreinamento item = listPadraoTreinamentoAdapter.getAllItems().get(position);
                removePadraoTreinamento(item);
                listPadraoTreinamentoAdapter.getAllItems().remove(position);
                listPadraoTreinamentoAdapter.notifyItemRemoved(position);
                listPadraoTreinamentoAdapter.notifyItemRangeChanged(position, listPadraoTreinamentoAdapter.getItemCount());
            }

            @Override
            public void onLeftClicked(int position) {
                final PadraoTreinamento item = listPadraoTreinamentoAdapter.getAllItems().get(position);
                Intent it = new Intent(getActivity(), EditPadraoTreinamentoActivity.class);
                it.putExtra("dataSource", item);
                startActivityForResult(it, REQUEST_EDIT_PADRAO_TREINAMENTO);
            }
        }, new DrawButtonsRecyclerView());

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    private void refreshPadraoTreinamentos() {
        class ListPadraoTreinamentoTask extends AsyncTask<Void, Void, List<PadraoTreinamento>> {

            @Override
            protected List<PadraoTreinamento> doInBackground(Void... voids) {
                List<PadraoTreinamento> ds = PadraoTreinamentoController.getInstance(getContext()).getAll();

                return ds;
            }

            @Override
            protected void onPostExecute(List<PadraoTreinamento> ds) {
                super.onPostExecute(ds);
                listPadraoTreinamentoAdapter.clear();
                listPadraoTreinamentoAdapter.addAll(ds);
                listPadraoTreinamentoAdapter.notifyDataSetChanged();

            }
        }

        ListPadraoTreinamentoTask newListPadraoTreinamentoTask = new ListPadraoTreinamentoTask();
        newListPadraoTreinamentoTask.execute();

    }

    private void removePadraoTreinamento(final PadraoTreinamento padraoTreinamento) {
        class RemovePadraoTreinamentoTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                PadraoTreinamentoController.getInstance(getContext()).delete(padraoTreinamento);
                return null;
            }

            @Override
            protected void onPostExecute(Void ds) {
                super.onPostExecute(ds);
                listPadraoTreinamentoAdapter.notifyDataSetChanged();
            }
        }

        RemovePadraoTreinamentoTask newRemovePadraoTreinamentoTask = new RemovePadraoTreinamentoTask();
        newRemovePadraoTreinamentoTask.execute();
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_conf_padrao_treinamento, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;

            case R.id.menu_conf_padrao_treinamento_adicionar:
                Intent it = new Intent(getActivity(), EditPadraoTreinamentoActivity.class);
                it.putExtra("dataSource", new PadraoTreinamento());
                startActivityForResult(it, REQUEST_EDIT_PADRAO_TREINAMENTO);

                return true;

        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_EDIT_PADRAO_TREINAMENTO:
                if (resultCode == Activity.RESULT_OK) {
                    refreshPadraoTreinamentos();
                }
                break;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class DrawButtonsRecyclerView implements IDrawButtons {

        @Override
        public Context getContext() {
            return ConfPadraoTreinamentoFragment.this.getContext();
        }

        @Override
        public RectF drawButtonLeft(Canvas c, RecyclerView.ViewHolder viewHolder) {
            float buttonWidthWithoutPadding = buttonWidth - 20;
            float corners = 16;

            View itemView = viewHolder.itemView;
            Paint p = new Paint();

            RectF leftButton = new RectF(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + buttonWidthWithoutPadding, itemView.getBottom());
            p.setColor(getContext().getResources().getColor(R.color.colorPrimary));
            c.drawRoundRect(leftButton, corners, corners, p);
            drawText("Editar", c, leftButton, p, R.drawable.ic_edit_white);

            return leftButton;
        }

        @Override
        public RectF drawButtonRight(Canvas c, RecyclerView.ViewHolder viewHolder) {
            float buttonWidthWithoutPadding = buttonWidth - 20;
            float corners = 16;

            View itemView = viewHolder.itemView;
            Paint p = new Paint();

            RectF rightButton = new RectF(itemView.getRight() - buttonWidthWithoutPadding, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            p.setColor(getContext().getResources().getColor(R.color.colorAccent));
            c.drawRoundRect(rightButton, corners, corners, p);
            drawText("Remover", c, rightButton, p, R.drawable.ic_delete_white);

            return rightButton;
        }
    }

    private class ListPadraoTreinamentoAdapter extends RecyclerView.Adapter<ListPadraoTreinamentoAdapter.PadraoTreinamentoConfViewHolder> {

        private List<PadraoTreinamento> items = new ArrayList<PadraoTreinamento>();

        public ListPadraoTreinamentoAdapter() {

        }

        @Override
        public PadraoTreinamentoConfViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_padrao_treinamento, parent, false);

            return new PadraoTreinamentoConfViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(PadraoTreinamentoConfViewHolder holder, int position) {
            PadraoTreinamento item = this.items.get(position);
            holder.getNome().setText(item.getNome());
            holder.getDescricao().setText(item.getDescricao());

            int hour = (int)(item.getTempoRepeticaoExercicioMillis() / 3600000);
            int minutes = (int)(item.getTempoRepeticaoExercicioMillis() % 3600000) / 60000;
            int seconds = (int)(item.getTempoRepeticaoExercicioMillis() % 60000) / 1000;
            holder.getDuracao().setText("Duração " +String.format("%02d", hour)+":"+String.format("%02d",minutes)+":"+String.format("%02d", seconds));

        }

        @Override
        public int getItemCount() {
            return this.items.size();
        }

        public List<PadraoTreinamento> getAllItems() {
            return this.items;
        }

        public PadraoTreinamento getItem(int index) {
            return this.items.get(index);
        }

        public void clear() {
            this.items.clear();
        }

        public void addAll(List<PadraoTreinamento> values) {
            this.items.addAll(values);
        }

        public class PadraoTreinamentoConfViewHolder extends RecyclerView.ViewHolder {
            private TextView icon;
            private TextView nome;
            private TextView duracao;
            private TextView descricao;

            public PadraoTreinamentoConfViewHolder(View view) {
                super(view);
                setIcon((TextView) view.findViewById(R.id.textViewItemListPadraoTreinamentoIcon));
                setNome((TextView) view.findViewById(R.id.textViewItemListPadraoTreinamentoNome));
                setDuracao((TextView) view.findViewById(R.id.textViewItemListPadraoTreinamentoDuracao));
                setDescricao((TextView) view.findViewById(R.id.textViewItemListPadraoTreinamentoDescricao));
            }


            public TextView getIcon() {
                return icon;
            }

            public void setIcon(TextView icon) {
                this.icon = icon;
            }

            public TextView getNome() {
                return nome;
            }

            public void setNome(TextView nome) {
                this.nome = nome;
            }

            public TextView getDuracao() {
                return duracao;
            }

            public void setDuracao(TextView duracao) {
                this.duracao = duracao;
            }

            public TextView getDescricao() {
                return descricao;
            }

            public void setDescricao(TextView descricao) {
                this.descricao = descricao;
            }
        }


    }

}