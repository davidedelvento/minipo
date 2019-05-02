package nl.naire.mipino;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

class MidiNumber {
    private static final String TAG = "MidiNumber";
    private final String unknownDevice;

    interface Listener {
        void onConnectedChanged(boolean connected, String name);
        void onNumber(int number);
    }

    MidiNumber(Context context, MidiManager manager) {
        this.manager = manager;
        framer = new MidiFramer(receiver);
        unknownDevice = context.getString(R.string.unknown_device);
    }

    void connect() {
        MidiDeviceInfo[] devices = manager.getDevices();
        for (MidiDeviceInfo device : devices) {
            if (device.getType() != MidiDeviceInfo.TYPE_VIRTUAL) {
                deviceInfo = device;
                openDevice();
                break;
            }
        }

        manager.registerDeviceCallback(deviceCallback, new Handler(Looper.getMainLooper()));
    }

    void disconnect() {
        manager.unregisterDeviceCallback(deviceCallback);
        if(port != null) {
            try {
                port.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(device != null) {
            try {
                device.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            device = null;
        }
    }

    void registerListener(Listener listener) {
        numberListener = listener;
    }

    private MidiManager.DeviceCallback deviceCallback = new MidiManager.DeviceCallback() {
        @Override
        public void onDeviceAdded(MidiDeviceInfo device) {
            if(device.getType() != MidiDeviceInfo.TYPE_VIRTUAL && deviceInfo == null) {
                deviceInfo = device;
                openDevice();
            }
        }

        @Override
        public void onDeviceRemoved(MidiDeviceInfo device) {
            if(deviceInfo.getId() == device.getId())
            {
                if(numberListener != null) {
                    numberListener.onConnectedChanged(false, null);
                }
                deviceInfo = null;
                port = null;
            }
        }
    };

    private void openDevice() {
        manager.openDevice(deviceInfo, onDeviceOpenedListener, new Handler(Looper.getMainLooper()));
    }

    private MidiManager.OnDeviceOpenedListener onDeviceOpenedListener = new MidiManager.OnDeviceOpenedListener() {
        @Override
        public void onDeviceOpened(MidiDevice midiDevice) {
            if(device != null) {
                try {
                    device.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            device = midiDevice;

            MidiDeviceInfo.PortInfo[] ports = device.getInfo().getPorts();
            for (MidiDeviceInfo.PortInfo port : ports) {
                if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                    portInfo = port;
                    break;
                }
            }

            port = device.openOutputPort(portInfo.getPortNumber());
            if(port != null) {
                port.connect(framer);

                if(numberListener != null) {
                    String name = deviceInfo.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
                    if(name == null) name = unknownDevice;
                    numberListener.onConnectedChanged(true, String.format("%s #%d", name, portInfo.getPortNumber()));
                }
            }
        }
    };

    private final MidiReceiver receiver = new MidiReceiver() {
        @Override
        public void onSend(byte[] msg, int offset, int count, long timestamp) {
            if (count != 3) return;

            if ((msg[0] & MidiConstants.STATUS_COMMAND_MASK) == MidiConstants.STATUS_NOTE_ON) {
                if (numberListener != null) {
                    numberListener.onNumber(msg[1]);
                }
            }
        }
    };


    private MidiManager manager;
    private MidiDeviceInfo deviceInfo;
    private MidiDevice device;
    private MidiDeviceInfo.PortInfo portInfo;
    private MidiOutputPort port;
    private MidiFramer framer;
    private Listener numberListener;
}
