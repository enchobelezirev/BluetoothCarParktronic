package belezirev.encho.bluetoothcarparktronic;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.ParcelUuid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class BluetoothProxy {

    private BluetoothAdapter adapter;
    private Handler handler;

    public BluetoothProxy(BluetoothAdapter adapter, Handler handler){
        this.adapter = adapter;
        this.handler = handler;
    }

    public void init() throws IOException {
        List<BluetoothDevice> pairedDevices = new ArrayList<>(adapter.getBondedDevices());
        BluetoothDevice pairedDevice = pairedDevices.get(1);

        BluetoothSocket pairedDeviceSocket = getBluetoothSocket(pairedDevice);
        pairedDeviceSocket.connect();

        ConnectionThread connectionThread = new ConnectionThread(pairedDeviceSocket, handler);
        connectionThread.start();
    }

    private BluetoothSocket getBluetoothSocket(BluetoothDevice pairedDevice){
        try{
            Class<?> clazz = pairedDevice.getClass();
            Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
            Object[] params = new Object[] {Integer.valueOf(1)};
            BluetoothSocket pairedDeviceSocket  = (BluetoothSocket) m.invoke(pairedDevice, params);
            return pairedDeviceSocket;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public class ConnectionThread extends Thread{

        private InputStream socketInputStream;
        private Handler bytesHandler;

        public ConnectionThread(BluetoothSocket socket, Handler bytesHandler){
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) { }

            socketInputStream = tmpIn;
            this.bytesHandler = bytesHandler;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[512];
            while(true){
                try{
                    int bytesRead = socketInputStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytesRead);
                    bytesHandler.obtainMessage(0, bytesRead, -1, readMessage).sendToTarget();
                    Thread.sleep(202);
                }catch (IOException | InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

}
