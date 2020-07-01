package br.org.sistemafieg.aquecimentoapp.fragments.execucaotreinamento;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.org.sistemafieg.aquecimentoapp.MainActivity;
import br.org.sistemafieg.aquecimentoapp.R;
import br.org.sistemafieg.aquecimentoapp.components.swipe.IDrawButtons;
import br.org.sistemafieg.aquecimentoapp.components.swipe.SwipeController;
import br.org.sistemafieg.aquecimentoapp.components.swipe.SwipeControllerActions;
import br.org.sistemafieg.aquecimentoapp.controller.DispositivoConfExercicioController;
import br.org.sistemafieg.aquecimentoapp.controller.ParametrosConfExercicioController;
import br.org.sistemafieg.aquecimentoapp.dto.BluetoothDeviceDispConfDto;
import br.org.sistemafieg.aquecimentoapp.enums.IdentificacaoDispositivoEnum;
import br.org.sistemafieg.aquecimentoapp.model.DispositivoConfExercicio;
import br.org.sistemafieg.aquecimentoapp.model.PadraoTreinamento;
import br.org.sistemafieg.aquecimentoapp.model.ParametrosConfExercicio;
import br.org.sistemafieg.aquecimentoapp.services.BluetoothChatService;
import br.org.sistemafieg.aquecimentoapp.util.Constants;

public class ExecutarTreinamentoFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    //private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_PADRAO_TREINAMENTO = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private ListDevicesConfAdapter listAdaperDevicesConf;
    private ParametrosConfExercicio mParametrosConfExercicio;
    private SwipeController swipeController = null;

    // Layout Views
    private TextView textViewIdentificacaoPadraoTreinamento;
    private RecyclerView recyclerView;
    private Switch switchStatusConexao;
    private Button buttonAtivarAquecimento;
    private ProgressBar progressBarAtivaConexoes;
    private ProgressBar progressBarExecucaoTreinamento;

    private Handler handlerProgressBarTempoExecucaoTreinamento = new Handler();

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;


    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    private PadraoTreinamento mPadraoTreinamento = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        findConfigurations();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth está desabilitado", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_executar_treinamento, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Execução Treinamento");

        progressBarAtivaConexoes = (ProgressBar) getView().findViewById(R.id.progressBarAtivaConexoes);
        progressBarExecucaoTreinamento = (ProgressBar) getView().findViewById(R.id.progressBarExecucaoTreinamento);
        textViewIdentificacaoPadraoTreinamento = (TextView) getView().findViewById(R.id.textViewIdentificacaoPadraoTreinamento);

        listAdaperDevicesConf = new ListDevicesConfAdapter();
        refreshDispositivosConfigurados(null);
        setupRecyclerView();

        switchStatusConexao = (Switch) getView().findViewById(R.id.switchStatusConexao);
        switchStatusConexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgressBarAtivaConexoesVisibility(switchStatusConexao.isChecked());
            }
        });
        switchStatusConexao.setOnCheckedChangeListener(switchStatusConexaoOnCheckedChangeListener);

        buttonAtivarAquecimento = (Button) getView().findViewById(R.id.buttonAtivarAquecimento);
        buttonAtivarAquecimento.setEnabled(false);
        buttonAtivarAquecimento.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        buttonAtivarAquecimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAtivarAquecimento.setBackgroundColor(getResources().getColor(R.color.holo_orange_light));
                buttonAtivarAquecimento.setEnabled(false);
                startProgressBarTempoExecucaoTreinamento(mPadraoTreinamento.getTempoRepeticaoExercicioMillis().longValue());

                StartAquecimentoThreadTask task = new StartAquecimentoThreadTask(mPadraoTreinamento);
                task.execute();

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
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    private void desconectTreinamento() {
        switchStatusConexao.setChecked(false);
        setProgressBarAtivaConexoesVisibility(false);
        mChatService.stop();
        setStatus(getString(R.string.title_not_connected));
        buttonAtivarAquecimento.setEnabled(false);
        buttonAtivarAquecimento.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        listAdaperDevicesConf = new ListDevicesConfAdapter();
        setupRecyclerView();
        setVisibilityAtivarTreinamento(false);
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerViewExecutarTreinamento);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(listAdaperDevicesConf);

        swipeController = new SwipeController(getContext(), new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                removeDispositivoConfExercicio(listAdaperDevicesConf.getItem(position));
                listAdaperDevicesConf.getAllItems().remove(position);
                listAdaperDevicesConf.notifyItemRemoved(position);
                listAdaperDevicesConf.notifyItemRangeChanged(position, listAdaperDevicesConf.getItemCount());
            }

            @Override
            public void onLeftClicked(int position) {
                final DispositivoConfExercicio item = listAdaperDevicesConf.getAllItems().get(position);
                setProgressBarAtivaConexoesVisibility(true);
                connectDevice(item);
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

    private CompoundButton.OnCheckedChangeListener switchStatusConexaoOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                setStatus(getString(R.string.title_connecting));
                connectDevices();
            } else {
                limparPadraoTreinamento();
                desconectTreinamento();
            }
        }
    };

    private void setProgressBarAtivaConexoesVisibility(boolean value) {
        progressBarAtivaConexoes.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void setProgressBarExecucaoTreinamentoVisibility(boolean value) {
        progressBarExecucaoTreinamento.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void setVisibilityAtivarTreinamento(boolean value) {
        switchStatusConexao.setVisibility(value ? View.VISIBLE : View.GONE);
        textViewIdentificacaoPadraoTreinamento.setVisibility(value ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.title_dispositivos_configurados).setVisibility(value ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.viewAnimator2).setVisibility(value ? View.VISIBLE : View.GONE);
        buttonAtivarAquecimento.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void refreshDispositivosConfigurados(Long idPadraoTreinamento) {
        listAdaperDevicesConf.clear();
        listAdaperDevicesConf.notifyDataSetChanged();

        class ListDispositivosConfiguradosTask extends AsyncTask<Void, Void, List<DispositivoConfExercicio>> {

            @Override
            protected List<DispositivoConfExercicio> doInBackground(Void... voids) {
                List<DispositivoConfExercicio> ds = DispositivoConfExercicioController.getInstance(getContext()).listByIdPadraoTreinamento(idPadraoTreinamento);

                return ds;
            }

            @Override
            protected void onPostExecute(List<DispositivoConfExercicio> ds) {
                super.onPostExecute(ds);
                listAdaperDevicesConf.clear();
                listAdaperDevicesConf.addAll(ds);
                listAdaperDevicesConf.notifyDataSetChanged();

            }
        }

        if(idPadraoTreinamento != null) {
            ListDispositivosConfiguradosTask newListDispositivosConfiguradosTask = new ListDispositivosConfiguradosTask();
            newListDispositivosConfiguradosTask.execute();
        }

    }

    private void findConfigurations() {
        class GetConfigurationsTask extends AsyncTask<Void, Void, ParametrosConfExercicio> {

            @Override
            protected ParametrosConfExercicio doInBackground(Void... voids) {
                ParametrosConfExercicio ds = ParametrosConfExercicioController.getInstance(getContext()).find();
                return ds;
            }

            @Override
            protected void onPostExecute(ParametrosConfExercicio ds) {
                super.onPostExecute(ds);
                mParametrosConfExercicio = ds;
            }
        }

        GetConfigurationsTask newGetConfigurationsTask = new GetConfigurationsTask();
        newGetConfigurationsTask.execute();
    }

    private void removeDispositivoConfExercicio(final DispositivoConfExercicio dispositivoConfExercicio) {
        class RemoveDispositivoConfExercicioTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                DispositivoConfExercicioController.getInstance(getContext()).delete(dispositivoConfExercicio);
                return null;
            }

            @Override
            protected void onPostExecute(Void ds) {
                super.onPostExecute(ds);
                listAdaperDevicesConf.notifyDataSetChanged();
            }
        }

        RemoveDispositivoConfExercicioTask newDispositivoConfExercicioTask = new RemoveDispositivoConfExercicioTask();
        newDispositivoConfExercicioTask.execute();
    }


    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }

        ((TextView) getView().findViewById(R.id.textViewStatusBluetooth)).setText(subTitle);
    }

    /**
     * Establish connection with other device
     */
    private void connectDevices() {
        List<BluetoothDeviceDispConfDto> devices = new ArrayList<BluetoothDeviceDispConfDto>();

        for (DispositivoConfExercicio dispositivoConfExercicio : listAdaperDevicesConf.getAllItems()) {
            // Get the BluetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(dispositivoConfExercicio.getAddressDevice());
            IdentificacaoDispositivoEnum dispositivoEnum = IdentificacaoDispositivoEnum.findByName(dispositivoConfExercicio.getIdentificacaoDevice());

            // Attempt to connect to the device
            mChatService.connect(dispositivoEnum, device);
        }

        SuccessConnectedThreadTask task = new SuccessConnectedThreadTask();
        task.execute();
    }

    private void connectDevice(DispositivoConfExercicio dispositivoConfExercicio) {
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(dispositivoConfExercicio.getAddressDevice());

        IdentificacaoDispositivoEnum dispositivoEnum = IdentificacaoDispositivoEnum.findByName(dispositivoConfExercicio.getIdentificacaoDevice());
        mChatService.connect(dispositivoEnum, device);

        SuccessConnectedThreadTask task = new SuccessConnectedThreadTask(dispositivoEnum);
        task.execute();
    }

    private void limparPadraoTreinamento() {
        textViewIdentificacaoPadraoTreinamento.setText("");
        mPadraoTreinamento = null;
        setVisibilityAtivarTreinamento(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_execucao_treinamento, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_seleciona_padrao_treinamento: {
                limparPadraoTreinamento();
                desconectTreinamento();

                Intent serverIntent = new Intent(getActivity(), SelecionaPadraoTreinamentoActivity.class);
                startActivityForResult(serverIntent, REQUEST_PADRAO_TREINAMENTO);
                return true;
            }
            case R.id.menu_habilitar_bluetooth: {
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PADRAO_TREINAMENTO:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    PadraoTreinamento pt = (PadraoTreinamento) bundle.get("dataSource");
                    mPadraoTreinamento = pt;

                    textViewIdentificacaoPadraoTreinamento.setText(pt.getNome());
                    setVisibilityAtivarTreinamento(true);

                    refreshDispositivosConfigurados(pt.getId());
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(getActivity(), "R.string.bt_not_enabled_leaving", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }


    protected void setStatusIcon(String addressDevice, StatusIconEnum status) {
        final Handler handler = new Handler();

        for (int i = 0; i < listAdaperDevicesConf.getItemCount(); i++) {
            DispositivoConfExercicio device = listAdaperDevicesConf.getItem(i);
            if (device.getAddressDevice().equals(addressDevice)) {

                abstract class MyRunnable implements Runnable {
                    int index;
                }

                Runnable runnableCode = new MyRunnable() {

                    @Override
                    public void run() {

                        recyclerView.getLayoutManager().scrollToPosition(index);
                        final Handler handler2 = new Handler();
                        handler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ListDevicesConfAdapter.DevicesConfViewHolder viewDeviceItem = (ListDevicesConfAdapter.DevicesConfViewHolder) recyclerView.findViewHolderForAdapterPosition(index);

                                if(getContext() != null && viewDeviceItem != null) {
                                    Drawable bluetoothOn = getContext().getResources().getDrawable(status.idDrawable);
                                    viewDeviceItem.getIcon().setCompoundDrawablesRelativeWithIntrinsicBounds(bluetoothOn, null, null, null);

                                    if (StatusIconEnum.DEVICE_CONNECTED.equals(status)) {
                                        viewDeviceItem.getStatusDevice().setText(getString(R.string.title_connected));
                                        viewDeviceItem.getStatusDevice().setTextColor(getResources().getColor(R.color.enable_thumb_green));
                                    } else {
                                        viewDeviceItem.getStatusDevice().setText(getString(R.string.title_not_connected));
                                        //viewDeviceItem.getStatusDevice().setTextColor(Color.BLACK);
                                    }

                                }
                            }
                        }, 1000);


                    }

                };
                ((MyRunnable) runnableCode).index = i;

                handler.postDelayed(runnableCode, 1000);
            }
        }
    }

    private void habilitarButtonAtivarAquecimento() {
        if (mParametrosConfExercicio.getQtdDispositivoCliente().equals(mChatService.qtdConnected())) {
            buttonAtivarAquecimento.setEnabled(true);
            buttonAtivarAquecimento.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.msg_numero_dispositivo_conectados_incompleto))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void startProgressBarTempoExecucaoTreinamento(long tempoExecucao) {
        handlerProgressBarTempoExecucaoTreinamento = new Handler();
        handlerProgressBarTempoExecucaoTreinamento.postDelayed(new Runnable() {
            @Override
            public void run() {
                setProgressBarExecucaoTreinamentoVisibility(true);
                buttonAtivarAquecimento.setVisibility(View.GONE);

                CountDownTimer countDownTimer = new CountDownTimer(tempoExecucao, 1000) {
                    int i = 0;

                    @Override
                    public void onTick(long millisUntilFinished) {
                        i++;
                        progressBarExecucaoTreinamento.setProgress((int)i*100/((int)tempoExecucao/1000));
                    }

                    @Override
                    public void onFinish() {
                        i++;
                        progressBarExecucaoTreinamento.setProgress(100);
                        setProgressBarExecucaoTreinamentoVisibility(false);
                        buttonAtivarAquecimento.setVisibility(View.VISIBLE);
                    }
                };

                countDownTimer.start();

            }
        }, 3500);

    }

    enum StatusIconEnum {
        BLUETOOTH_ON(R.drawable.ic_bluetooth_on),
        BLUETOOTH_OF(R.drawable.ic_bluetooth_off),
        DEVICE_CONNECTED(R.drawable.ic_phonelink_on),
        DEVICE_DISCONNECTED(R.drawable.ic_phonelink_off);

        private final int idDrawable;

        private StatusIconEnum(int idDrawable) {
            this.idDrawable = idDrawable;
        }
    }

    class StartAquecimentoThreadTask extends AsyncTask<Void, Void, Boolean> {
        private PadraoTreinamento mPadraoTreinamento;

        public StartAquecimentoThreadTask(PadraoTreinamento padraoTreinamento) {
            mPadraoTreinamento = padraoTreinamento;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                mChatService.startAquecimentoOffline(mPadraoTreinamento);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(!result) {
                handlerProgressBarTempoExecucaoTreinamento.removeCallbacksAndMessages(null);
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    habilitarButtonAtivarAquecimento();
                }
            }, 2100);


        }
    }

    class SuccessConnectedThreadTask extends AsyncTask<Void, Void, Boolean> {

        private IdentificacaoDispositivoEnum dispositivoEnum;

        public SuccessConnectedThreadTask() {

        }

        public SuccessConnectedThreadTask(IdentificacaoDispositivoEnum dispositivoEnum) {
            this.dispositivoEnum = dispositivoEnum;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                while (true) {
                    Thread.sleep(1000);

                    if (dispositivoEnum == null && mChatService.isCompletedConnection()) {
                        return true;
                    } else if (dispositivoEnum != null && mChatService.isConnected(dispositivoEnum)) {
                        return true;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            setProgressBarAtivaConexoesVisibility(!result);
            setStatus(getString(R.string.title_connected));
            habilitarButtonAtivarAquecimento();
            mChatService.writeConf(mPadraoTreinamento.getId());

            if (mParametrosConfExercicio.getQtdDispositivoCliente().equals(mChatService.qtdConnected()) && !switchStatusConexao.isChecked()) {
                switchStatusConexao.setOnCheckedChangeListener(null);
                switchStatusConexao.setChecked(true);
                switchStatusConexao.setOnCheckedChangeListener(switchStatusConexaoOnCheckedChangeListener);
            }
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            //setStatus(getString(R.string.title_connecting));
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            //setStatus(getString(R.string.title_not_connected));
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    Toast.makeText(getActivity(), writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    Toast.makeText(getActivity(), readMessage, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    String disp = msg.getData().getString(Constants.DEVICE_ADDRESS);
                    setStatusIcon(disp, StatusIconEnum.DEVICE_CONNECTED);

                    if (null != activity) {
                        //Toast.makeText(activity, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_CONNECTION_LOST:
                    String dispConnectionLost = msg.getData().getString(Constants.DEVICE_ADDRESS);

                    setStatusIcon(dispConnectionLost, StatusIconEnum.DEVICE_DISCONNECTED);
                    setProgressBarAtivaConexoesVisibility(false);
                    setProgressBarExecucaoTreinamentoVisibility(false);

                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;

                case Constants.MESSAGE_CONNECTION_FALIED:
                    setProgressBarAtivaConexoesVisibility(false);
                    setProgressBarExecucaoTreinamentoVisibility(false);
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST), Toast.LENGTH_LONG).show();
                    }
                    break;


            }
        }
    };

    private class DrawButtonsRecyclerView implements IDrawButtons {

        @Override
        public Context getContext() {
            return ExecutarTreinamentoFragment.this.getContext();
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
            drawText("Conectar", c, leftButton, p, R.drawable.ic_bluetooth_connected_white);

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

    private class ListDevicesConfAdapter extends RecyclerView.Adapter<ListDevicesConfAdapter.DevicesConfViewHolder> {

        private List<DispositivoConfExercicio> items = new ArrayList<DispositivoConfExercicio>();

        public ListDevicesConfAdapter() {

        }

        @Override
        public DevicesConfViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_executar_treinamento, parent, false);

            return new DevicesConfViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(DevicesConfViewHolder holder, int position) {
            DispositivoConfExercicio item = this.items.get(position);
            holder.getNameDevice().setText(item.getNameDevice());
            holder.getIdentificacaoDevice().setText(IdentificacaoDispositivoEnum.findByName(item.getIdentificacaoDevice()).getLabel());
            holder.getStatusDevice().setText(ExecutarTreinamentoFragment.this.getContext().getString(R.string.title_not_connected));
        }

        @Override
        public int getItemCount() {
            return this.items.size();
        }

        public List<DispositivoConfExercicio> getAllItems() {
            return this.items;
        }

        public DispositivoConfExercicio getItem(int index) {
            return this.items.get(index);
        }

        public void clear() {
            this.items.clear();
        }

        public void addAll(List<DispositivoConfExercicio> values) {
            this.items.addAll(values);
        }

        public class DevicesConfViewHolder extends RecyclerView.ViewHolder {
            private TextView icon;
            private TextView nameDevice;
            private TextView statusDevice;
            private TextView identificacaoDevice;

            public DevicesConfViewHolder(View view) {
                super(view);
                setIcon((TextView) view.findViewById(R.id.textViewItemListConfDispositivosIcon));
                setNameDevice((TextView) view.findViewById(R.id.textViewItemListConfDispositivosNameDevice));
                setStatusDevice((TextView) view.findViewById(R.id.textViewItemListConfDispositivosStatusDevice));
                setIdentificacaoDevice((TextView) view.findViewById(R.id.textViewItemListConfDispositivosIdentificacao));
            }


            public TextView getIcon() {
                return icon;
            }

            public void setIcon(TextView icon) {
                this.icon = icon;
            }

            public TextView getNameDevice() {
                return nameDevice;
            }

            public void setNameDevice(TextView nameDevice) {
                this.nameDevice = nameDevice;
            }

            public TextView getStatusDevice() {
                return statusDevice;
            }

            public void setStatusDevice(TextView statusDevice) {
                this.statusDevice = statusDevice;
            }

            public TextView getIdentificacaoDevice() {
                return identificacaoDevice;
            }

            public void setIdentificacaoDevice(TextView identificacaoDevice) {
                this.identificacaoDevice = identificacaoDevice;
            }
        }


    }


}