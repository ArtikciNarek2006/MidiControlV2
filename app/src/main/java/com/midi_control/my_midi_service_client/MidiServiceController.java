package com.midi_control.my_midi_service_client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.midi_control.util.ML;
import com.midi_control.util.MidiUtil;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Set;

@SuppressLint("ObsoleteSdkInt")
@RequiresApi(api = Build.VERSION_CODES.M)
class Controller_API_23 {
    public static final String TAG = "Controller_API_23";
    public int status = MidiServiceController.STATUS_UNINITIALIZED;
    private MidiManager midiManager;
    private MidiServiceClientPresenter midiServiceClientPresenter;
    private MidiDeviceInfo[] midiDeviceInfos;
    private ArrayList<Integer> openedDevicesIds = new ArrayList<Integer>();

    private void init_device(int info_i) {
        MidiDeviceInfo info = midiDeviceInfos[info_i];
        if (openedDevicesIds.contains(info.getId())) {
            ML.log(TAG, "init_device() -> device all ready inited");
            return;
        }


        int numInputs = info.getInputPortCount();
        int numOutputs = info.getOutputPortCount();

        if (numInputs == 0 && numOutputs == 0) {
            ML.warn(TAG, "init_device(" + info_i + ") -> info_midi_id:" + info.getId() + "; empty input output device");
            return;
        }

        midiManager.openDevice(info, new MidiManager.OnDeviceOpenedListener() {
            @Override
            public void onDeviceOpened(MidiDevice device) {
                if (device == null) {
                    ML.err(TAG, "init_device(" + info_i + ") -> null device");
                    return;
                }

                MidiDeviceInfo.PortInfo[] portInfos = device.getInfo().getPorts();

                for (MidiDeviceInfo.PortInfo portInfo : portInfos) {
                    if (portInfo.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                        if (device.getInfo().getType() != MidiDeviceInfo.TYPE_VIRTUAL) {
                            // to prevent controlling virtual devices;
                            // TODO: add device portInput implementation
                        }
                    } else {
                        // portType == OUTPUT

                        // sending data to presenter
                        device.openOutputPort(
                                portInfo.getPortNumber()
                        ).connect(new MidiFramer(
                                        new MidiReceiver() {
                                            @Override
                                            public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {

                                            }
                                        }
                                )
                        );
                    }
                }

            }
        }, new Handler(Looper.getMainLooper()));
    }

    public void check_for_devices() {
        midiDeviceInfos = midiManager.getDevices();

        if (midiDeviceInfos.length > 0) {
            ML.log(TAG, midiDeviceInfos.length + " devices connected.");
            for (int i = 0; i < midiDeviceInfos.length; i++) {
                init_device(i);
            }
        } else {
            ML.log(TAG, "No devices connected.");
        }
    }

    private void registerHotPlugListener() {
        midiManager.registerDeviceCallback(new MidiManager.DeviceCallback() {

            @Override
            public void onDeviceAdded(MidiDeviceInfo device) {
                if (openedDevicesIds.contains(device.getId())) {
                    ML.err(TAG, "registerHotPlugListener() -> opened device reconnected");
                }
                check_for_devices();
            }

            @Override
            public void onDeviceRemoved(MidiDeviceInfo device) {
                openedDevicesIds.remove((Integer) device.getId());
                check_for_devices();
            }
        }, new Handler(Looper.getMainLooper()));
    }

    public Controller_API_23(@NonNull Context ctx, MidiServiceClientPresenter mscp) {
        midiManager = (MidiManager) ctx.getSystemService(Context.MIDI_SERVICE);
        status = (midiManager == null) ? MidiServiceController.STATUS_NULL_MIDI_MANAGER : MidiServiceController.STATUS_OK;
        midiServiceClientPresenter = mscp;
        if (status == MidiServiceController.STATUS_OK) {
            check_for_devices();
            registerHotPlugListener();
        }
    }
}

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
class Controller_API_33 {
    public static final String TAG = "Controller_API_33";
    public int status = MidiServiceController.STATUS_UNINITIALIZED;
    private MidiManager midiManager;
    private Set<MidiDeviceInfo> midiDeviceInfos;

    private MidiServiceClientPresenter midiServiceClientPresenter;

    public void check_for_devices() {
        midiDeviceInfos = midiManager.getDevicesForTransport(MidiManager.TRANSPORT_UNIVERSAL_MIDI_PACKETS);
    }

    public Controller_API_33(@NonNull Context ctx, MidiServiceClientPresenter mscp) {
        midiManager = (MidiManager) ctx.getSystemService(Context.MIDI_SERVICE);
        status = (midiManager == null) ? MidiServiceController.STATUS_NULL_MIDI_MANAGER : MidiServiceController.STATUS_OK;
        midiServiceClientPresenter = mscp;
    }
}

public class MidiServiceController {
    public static final String TAG = "MidiServiceController";
    public static final int STATUS_UNINITIALIZED = 5, STATUS_NULL_MIDI_MANAGER = 6, STATUS_OK = 1, STATUS_UNSUPPORTED_API = 2;
    public static final int API_23 = 25, API_33 = 26, API_UNKNOWN = 1;
    private static MidiServiceController instance;


    @Nullable
    public static synchronized MidiServiceController getInstance(Context ctx, MidiServiceClientPresenter midiServiceClientPresenter) {
        if (MidiUtil.midiSupported(ctx)) {
            if (instance == null) {
                instance = new MidiServiceController(ctx, midiServiceClientPresenter);
            }
            return (instance.status == STATUS_OK) ? instance : null;
        } else {
            ML.warn(TAG, "Midi feature not supported.");
            return null;
        }
    }


    // NON STATIC
    public int status = STATUS_UNINITIALIZED;
    private Controller_API_23 controllerApi23;
    private Controller_API_33 controllerApi33;
    private MidiServiceClientPresenter midiServiceClientPresenter;


    @SuppressLint("ObsoleteSdkInt")
    private MidiServiceController(Context ctx, MidiServiceClientPresenter mscp) {
        midiServiceClientPresenter = mscp;
        status = STATUS_UNSUPPORTED_API;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && false) {
            controllerApi33 = new Controller_API_33(ctx, midiServiceClientPresenter);
            status = controllerApi33.status;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            controllerApi23 = new Controller_API_23(ctx, midiServiceClientPresenter);
            status = controllerApi23.status;
        }

        if (status == STATUS_UNSUPPORTED_API) {
            ML.err(TAG, "unsupported device api level");
        } else if (status == STATUS_NULL_MIDI_MANAGER) {
            ML.err(TAG, "Null MidiManager");
        } else if (status == STATUS_OK) {
            ML.log(TAG, "Normally initialized.");
        }
    }
}
