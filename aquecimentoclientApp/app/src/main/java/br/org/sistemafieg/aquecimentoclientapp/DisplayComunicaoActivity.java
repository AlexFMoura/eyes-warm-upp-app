package br.org.sistemafieg.aquecimentoclientapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;

import br.org.sistemafieg.aquecimentoclientapp.enums.IdentificacaoDispositivoEnum;
import br.org.sistemafieg.aquecimentoclientapp.model.ConfDispositivo;
import br.org.sistemafieg.aquecimentoclientapp.services.BluetoothClientService;
import br.org.sistemafieg.aquecimentoclientapp.util.Constants;


public class DisplayComunicaoActivity extends AppCompatActivity {

    private Menu menu;
    private TextView textViewDisplay;
    private MediaPlayer mPlayer;

    private BluetoothClientService mBluetoothClientService = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceName = null;
    private IdentificacaoDispositivoEnum mDispositivo;
    private JSONObject mJsonConf;
    private final Handler mHandlerDisplay = new Handler();
    private ToneGenerator toneGen1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_comunicao);

        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Activity activity = this;
            Toast.makeText(activity, "Bluetooth está desabilitado", Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }

        Bundle bundle = getIntent().getExtras();
        ConfDispositivo dataSource = (ConfDispositivo)bundle.get("dataSource");
        mDispositivo = IdentificacaoDispositivoEnum.findByName(dataSource.getIdentificacaoDevice());
        getSupportActionBar().setTitle(getString(R.string.lb_nome_app) + " - " + mDispositivo.getLabel());

        textViewDisplay = (TextView) findViewById(R.id.textViewDisplay);
        textViewDisplay.setTextColor(Color.BLACK);

        ConstraintLayout layoutMain = findViewById(R.id.layoutDisplayComunicacao);
        layoutMain.setBackgroundColor(Color.parseColor(mDispositivo.getColor()));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(mDispositivo.getColor())));

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(mDispositivo.getColor()));



        /*
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                Drawable drawable = menu.getItem(2).getIcon();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                }
            }
        }, 4000); */

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
        } else if (mBluetoothClientService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothClientService != null) {
            mBluetoothClientService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mBluetoothClientService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothClientService.getState() == Constants.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothClientService.start(mDispositivo);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    //connectDevice(data, true);
                }
                break;
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Activity activity = this;
                    Toast.makeText(activity, getString(R.string.msg_bluetooth_desativado), Toast.LENGTH_LONG).show();
                    activity.finish();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_display_comunicacao, menu);
        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_mudar_dispositivo: {
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
                return false;
            }
            case R.id.menu_reconectar: {
                mBluetoothClientService.start(mDispositivo);
                Toast.makeText(this, "Reconectando ...", Toast.LENGTH_LONG).show();

                final CountDownTimer timer = new CountDownTimer(1500, 1500) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // Nothing to do
                    }

                    @Override
                    public void onFinish() {
                        menu.findItem(R.id.menu_conectando).setVisible(true);
                        menu.findItem(R.id.menu_desconectado).setVisible(false);
                        menu.findItem(R.id.menu_conectado).setVisible(false);
                    }
                };
                timer.start();

                return false;
            }
            default:
                playBeep();

        }
        return false;
    }


    private void playConnected() {
        MediaPlayer mp = MediaPlayer.create(DisplayComunicaoActivity.this, R.raw.startup);
        mp.start();

    }

    private void playBeep() {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,100);
    }

    private void setTextDisplay(final String msg) {
        try {
            final double tempoVisualizacaoNumeroExercicioMilis= Double.valueOf((String) mJsonConf.get("tempoVisualizacaoNumeroExercicio"));
            final boolean ativarBeep = Boolean.valueOf((String) mJsonConf.get("ativarBeep"));


            long duration = Double.valueOf(tempoVisualizacaoNumeroExercicioMilis).longValue();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textViewDisplay.setText("");
                    ((ConstraintLayout) findViewById(R.id.layoutDisplayComunicacao)).setBackgroundColor(ContextCompat.getColor(DisplayComunicaoActivity.this, R.color.colorPrimary));
                }
            }, duration);

            if(ativarBeep) {
                playBeep();
            }

            ((ConstraintLayout) findViewById(R.id.layoutDisplayComunicacao)).setBackgroundColor(ContextCompat.getColor(DisplayComunicaoActivity.this, R.color.colorWhite));
            textViewDisplay.setText(msg);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setTextDisplayOffline(final JSONObject json) {
        try {
            final double tempoVisualizacaoNumeroExercicioMilis = Double.valueOf((String) mJsonConf.get("tempoVisualizacaoNumeroExercicio"));
            final long duration = Double.valueOf(tempoVisualizacaoNumeroExercicioMilis).longValue();
            final boolean ativarBeep = Boolean.valueOf((String) mJsonConf.get("ativarBeep"));

            final long tempoMilisegundosExecucao = Long.valueOf(json.get("tempoMilisegundosExecucao").toString());
            final long intervaloMilissegundos    = Long.valueOf(json.get("intervaloMilissegundos").toString());
            final LinkedList<String> ordemExecucao = new LinkedList<String>();
            ordemExecucao.addAll(Arrays.asList(((String) json.get("ordemExecucao")).split(",")));

            final LinkedList<String> sequenciaCaracter = new LinkedList<String>();
            sequenciaCaracter.addAll(Arrays.asList(((String) json.get("sequenciaCaracter")).split(",")));

            abstract class MyRunnable implements Runnable {
                Long ordemAnterior;
            }

            final long ordemConexaoDevice = Long.valueOf(json.get("ordemConexaoDevice").toString());
            final Handler handlerStart = new Handler();
            handlerStart.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Handler handler = new Handler();
                    Runnable runnableCode = new MyRunnable() {

                        @Override
                        public void run() {

                            final Handler handler2 = new Handler();
                            handler2.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    textViewDisplay.setText("");
                                    ((ConstraintLayout) findViewById(R.id.layoutDisplayComunicacao)).setBackgroundColor(Color.parseColor(mDispositivo.getColor()));
                                }
                            }, duration);

                            if(ativarBeep) {
                                playBeep();
                            }

                            ((ConstraintLayout) findViewById(R.id.layoutDisplayComunicacao)).setBackgroundColor(ContextCompat.getColor(DisplayComunicaoActivity.this, R.color.colorWhite));
                            textViewDisplay.setText(sequenciaCaracter.poll());

                            Long ordem = Long.valueOf(ordemExecucao.poll());
                            long sleep = (ordem-ordemAnterior) * intervaloMilissegundos;
                            handler.postDelayed(this, sleep);
                            ordemAnterior = ordem;
                        }

                    };

                    Long ordem = Long.valueOf(ordemExecucao.poll());
                    ((MyRunnable) runnableCode).ordemAnterior = ordem;
                    handler.postDelayed(runnableCode, ordem * intervaloMilissegundos);


                    final Handler handlerTempoEx = new Handler();
                    handlerTempoEx.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handler.removeCallbacksAndMessages(null);
                        }
                    }, tempoMilisegundosExecucao);



                }
            }, 3500-(ordemConexaoDevice*500));
            /*
            try{
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            abstract class MyRunnable implements Runnable {
                Long ordemAnterior;
            }


            final Handler handler = new Handler();
            Runnable runnableCode = new MyRunnable() {

                @Override
                public void run() {

                    final Handler handler2 = new Handler();
                    handler2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            textViewDisplay.setText("");
                            ((ConstraintLayout) findViewById(R.id.layoutDisplayComunicacao)).setBackgroundColor(Color.parseColor(mCorLayoutEnum.getColor()));
                        }
                    }, duration);

                    if(ativarBeep) {
                        playBeep();
                    }

                    ((ConstraintLayout) findViewById(R.id.layoutDisplayComunicacao)).setBackgroundColor(ContextCompat.getColor(DisplayComunicaoActivity.this, R.color.colorWhite));
                    textViewDisplay.setText(sequenciaCaracter.poll());

                    Long ordem = Long.valueOf(ordemExecucao.poll());
                    long sleep = (ordem-ordemAnterior) * intervaloMilissegundos;
                    handler.postDelayed(this, sleep);
                    ordemAnterior = ordem;
                }

            };

            Long ordem = Long.valueOf(ordemExecucao.poll());
            ((MyRunnable) runnableCode).ordemAnterior = ordem;
            handler.postDelayed(runnableCode, ordem * intervaloMilissegundos);


            final Handler handlerTempoEx = new Handler();
            handlerTempoEx.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.removeCallbacksAndMessages(null);
                }
            }, tempoMilisegundosExecucao);

            */

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        TextView textView = (TextView) findViewById(R.id.textViewStatusBluetooth);
        if(textView != null) {
            textView.setText(subTitle);
        }
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothClientService = new BluetoothClientService(this, mHandler);
    }



    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = DisplayComunicaoActivity.this;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            menu.findItem(R.id.menu_conectando).setVisible(false);
                            menu.findItem(R.id.menu_desconectado).setVisible(false);
                            menu.findItem(R.id.menu_conectado).setVisible(true);

                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            playConnected();
                            break;
                        case Constants.STATE_CONNECTING:
                            setStatus(getString(R.string.title_connecting));
                            break;
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
                            if(menu != null) {
                                menu.findItem(R.id.menu_conectando).setVisible(false);
                                menu.findItem(R.id.menu_desconectado).setVisible(true);
                                menu.findItem(R.id.menu_conectado).setVisible(false);
                            }

                            setStatus(getString(R.string.title_not_connected));
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    Toast.makeText(activity, writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    try {
                        String jsonConf = new String((byte[]) msg.obj);
                        JSONObject json = new JSONObject(jsonConf);
                        setTextDisplayOffline(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //setTextDisplay(readMessage);
                    break;
                case Constants.MESSAGE_CONF_INITIAL:
                    try {
                        String jsonConf = new String((byte[]) msg.obj);
                        mJsonConf = new JSONObject(jsonConf);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };



}
