package belezirev.encho.bluetoothcarparktronic;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;

public class BluetoothValidator {

    public BluetoothValidationStatus valudatedBluetooth(BluetoothAdapter adapter){
        BluetoothValidationStatus status = validateBluetoothAvailability(adapter);
        if (status != BluetoothValidationStatus.OK){
            return status;
        }

        return  validateState(adapter);
    }

    private BluetoothValidationStatus validateState(BluetoothAdapter adapter) {
        if (!adapter.isEnabled()) {
            return BluetoothValidationStatus.NOT_ACTIVATED;
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            activitiSupplier.get().startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        return BluetoothValidationStatus.OK;
    }

    private BluetoothValidationStatus validateBluetoothAvailability(BluetoothAdapter adapter)  {
        if (adapter == null){
           return BluetoothValidationStatus.NOT_SUPPORTED;
        }

        return BluetoothValidationStatus.OK;
    }

    public enum BluetoothValidationStatus{
        OK, NOT_SUPPORTED, NOT_ACTIVATED
    }

}
