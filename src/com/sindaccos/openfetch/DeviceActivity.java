
 /*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sindaccos.openfetch;

import java.util.UUID;

import com.sindaccos.openfetch.R;
import com.sindaccos.openfetch.util.BleUtil;
import com.sindaccos.openfetch.util.BleUuid;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceActivity extends Activity implements View.OnClickListener {
	private static final String TAG = "BLEDevice";

	public static final String EXTRA_BLUETOOTH_DEVICE = "BT_DEVICE";
	private BluetoothAdapter mBTAdapter;
	private BluetoothDevice mDevice;
	private BluetoothGatt mConnGatt;
	private int mStatus;

	private Button mSilentKeyFinderButton;
	private Button mLoudKeyFinderButton;
	private Button mStopKeyFinderButton;

	private final BluetoothGattCallback mGattcallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				mStatus = newState;
				mConnGatt.discoverServices();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				mStatus = newState;
				runOnUiThread(new Runnable() {
					public void run() {
						mSilentKeyFinderButton.setEnabled(false);
						mLoudKeyFinderButton.setEnabled(false);
						mStopKeyFinderButton.setEnabled(false);
					};
				});
			}
		};

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			for (BluetoothGattService service : gatt.getServices()) {
				if ((service == null) || (service.getUuid() == null)) {
					continue;
				}
				
				if (BleUuid.SERVICE_IMMEDIATE_ALERT.equalsIgnoreCase(service
						.getUuid().toString())) {
					runOnUiThread(new Runnable() {
						public void run() {
							mLoudKeyFinderButton.setEnabled(true);
							mSilentKeyFinderButton.setEnabled(true);
							mStopKeyFinderButton.setEnabled(false);
						};
					});
					mLoudKeyFinderButton.setTag(service
							.getCharacteristic(UUID
									.fromString(BleUuid.CHAR_ALERT_LEVEL)));
					mSilentKeyFinderButton.setTag(service
							.getCharacteristic(UUID
									.fromString(BleUuid.CHAR_ALERT_LEVEL)));
					mStopKeyFinderButton.setTag(service
							.getCharacteristic(UUID
									.fromString(BleUuid.CHAR_ALERT_LEVEL)));
				}
			}

			runOnUiThread(new Runnable() {
				public void run() {
					setProgressBarIndeterminateVisibility(false);
				};
			});
		};

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.i(TAG, "BluetoothGatt connected!");

			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {

			runOnUiThread(new Runnable() {
				public void run() {
					setProgressBarIndeterminateVisibility(false);
				};
			});
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_device);

		// state
		mStatus = BluetoothProfile.STATE_DISCONNECTED;
		
		mSilentKeyFinderButton = (Button) findViewById(R.id.silent_keyfinder_button);
		mSilentKeyFinderButton.setOnClickListener(this);
		mLoudKeyFinderButton = (Button) findViewById(R.id.loud_keyfinder_button);
		mLoudKeyFinderButton.setOnClickListener(this);
		mStopKeyFinderButton = (Button) findViewById(R.id.stop_keyfinder_button);
		mStopKeyFinderButton.setOnClickListener(this);
		
		
	}

	@Override
	protected void onResume() {
		super.onResume();

		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mConnGatt != null) {
			if ((mStatus != BluetoothProfile.STATE_DISCONNECTING)
					&& (mStatus != BluetoothProfile.STATE_DISCONNECTED)) {
				mConnGatt.disconnect();
			}
			mConnGatt.close();
			mConnGatt = null;
		}
	}

	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        getMenuInflater().inflate(R.menu.fetch, menu);
	        return true;
	    }

	   
	    @Override
	    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	        int itemId = item.getItemId();
	        if (itemId == android.R.id.home) {
	            // ignore
	            return true;
	        } else if (itemId == R.id.menu_about) {
	            showAbout();
	            return true;
	        } else if (itemId == R.id.menu_exit) {
	        	finish();
	            System.exit(0);
	            return true;
	        }
	        return super.onMenuItemSelected(featureId, item);
	    }
	    
	    protected void showAbout() {
	        // Inflate the about message contents
	        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);
	 
	        // When linking text, force to always use default color. This works
	        // around a pressed color state bug.
	        TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
	        int defaultColor = textView.getTextColors().getDefaultColor();
	        textView.setTextColor(defaultColor);
	 
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setIcon(R.drawable.ic_launcher);
	        builder.setTitle(R.string.app_name);
	        builder.setView(messageView);
	        builder.create();
	        builder.show();
	    }
	    
	@Override
	 public void onClick(View v) {
        if (v.getId() == R.id.silent_keyfinder_button) {
        	 if ((v.getTag() != null)
                     && (v.getTag() instanceof BluetoothGattCharacteristic)) {
             BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v
                             .getTag();
             ch.setValue(new byte[] { (byte) 0x01 });
             if (mConnGatt.writeCharacteristic(ch)) {
                     setProgressBarIndeterminateVisibility(true);
                     mStopKeyFinderButton.setEnabled(true);
             }
     }
        } else if (v.getId() == R.id.loud_keyfinder_button) {
        	 if ((v.getTag() != null)
                     && (v.getTag() instanceof BluetoothGattCharacteristic)) {
             BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v
                             .getTag();
             ch.setValue(new byte[] { (byte) 0x02 });
             if (mConnGatt.writeCharacteristic(ch)) {
                     setProgressBarIndeterminateVisibility(true);
                     mStopKeyFinderButton.setEnabled(true);
             }
     }

        } else if (v.getId() == R.id.stop_keyfinder_button) {
                if ((v.getTag() != null)
                                && (v.getTag() instanceof BluetoothGattCharacteristic)) {
                        BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v
                                        .getTag();
                        ch.setValue(new byte[] { (byte) 0x00 });
                        if (mConnGatt.writeCharacteristic(ch)) {
                                setProgressBarIndeterminateVisibility(true);
                                mStopKeyFinderButton.setEnabled(false);
                        }
                }
        }
}
		
	

	
	private void init() {
		// BLE check
		if (!BleUtil.isBLESupported(this)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

		// BT check
		BluetoothManager manager = BleUtil.getManager(this);
		if (manager != null) {
			mBTAdapter = manager.getAdapter();
		}
		if (mBTAdapter == null) {
			Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

		// check BluetoothDevice
		if (mDevice == null) {
			mDevice = getBTDeviceExtra();
			if (mDevice == null) {
				finish();
				return;
			}
		}

		// button disable
		
		mSilentKeyFinderButton.setEnabled(false);
		mLoudKeyFinderButton.setEnabled(false);
		mStopKeyFinderButton.setEnabled(false);

		// connect to Gatt
		if ((mConnGatt == null)
				&& (mStatus == BluetoothProfile.STATE_DISCONNECTED)) {
			// try to connect
			mConnGatt = mDevice.connectGatt(this, false, mGattcallback);
			mStatus = BluetoothProfile.STATE_CONNECTING;
			
		} else {
			if (mConnGatt != null) {
				// re-connect and re-discover Services
				mConnGatt.connect();
				mConnGatt.discoverServices();
			} else {
				Log.e(TAG, "state error");
				finish();
				return;
			}
		}
		setProgressBarIndeterminateVisibility(true);
	}

	private BluetoothDevice getBTDeviceExtra() {
		Intent intent = getIntent();
		if (intent == null) {
			return null;
		}

		Bundle extras = intent.getExtras();
		if (extras == null) {
			return null;
		}

		return extras.getParcelable(EXTRA_BLUETOOTH_DEVICE);
	}

}
