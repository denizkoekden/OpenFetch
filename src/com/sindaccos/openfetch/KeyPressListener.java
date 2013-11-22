package com.sindaccos.openfetch;


import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.IBinder;

public class KeyPressListener extends Service {
    public KeyPressListener() {
    }
    
    private BluetoothGatt mBluetoothGatt;
    BluetoothGattCharacteristic characteristic;
    boolean enabled;
    
    
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
