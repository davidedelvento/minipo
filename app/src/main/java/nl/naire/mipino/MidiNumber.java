package nl.naire.mipino;

import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

public class MidiNumber {
    interface Listener {
        void onConnectedChanged(boolean connected, String name);
        void onNumber(int number);
    }

    MidiNumber(MidiManager manager) {
        this.manager = manager;
        framer = new MidiFramer(receiver);
    }

    public void connect() {
        MidiDeviceInfo[] devices = manager.getDevices();
        for(int i = 0; i < devices.length; i++) {
            if(devices[i].getType() != MidiDeviceInfo.TYPE_VIRTUAL) {
                deviceInfo = devices[i];
                openDevice();
                break;
            }
        }

        manager.registerDeviceCallback(deviceCallback, new Handler(Looper.getMainLooper()));
    }

    public void disconnect() {
        manager.unregisterDeviceCallback(deviceCallback);
        if(device != null) {
            try {
                device.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            device = null;
        }
    }

    public void registerListener(Listener listener) {
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
            if(deviceInfo == device)
            {
                if(numberListener != null) {
                    numberListener.onConnectedChanged(false, null);
                }
                deviceInfo = null;
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
            for(int i = 0; i < ports.length; i++)
            {
                if(ports[i].getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                    portInfo = ports[i];
                    break;
                }
            }

            MidiOutputPort port = device.openOutputPort(portInfo.getPortNumber());
            if(port != null) {
                port.connect(framer);

                if(numberListener != null) {
                    numberListener.onConnectedChanged(true, "#" + String.valueOf(portInfo.getPortNumber()));
                }
            }
        }
    };

    private final MidiReceiver receiver = new MidiReceiver() {
        @Override
        public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
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
    private MidiFramer framer;
    private Listener numberListener;
}
