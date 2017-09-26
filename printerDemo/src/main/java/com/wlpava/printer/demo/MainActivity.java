package com.wlpava.printer.demo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import printpp.printpp_yt.PrintPP_CPCL;

public class MainActivity extends AppCompatActivity {
    private ImageView searchImageView;
    private TextView searchTextView;
    private ListView printerListView;
    List<HashMap<String, String>> printerList;
    ArrayList<String> printerNameList;
    ArrayList<String> printerAddressList;
    BluetoothAdapter btAdapter;
    private String printerName;
    private String printerAddress;
    private PrintPP_CPCL iPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchImageView = (ImageView) findViewById(R.id.searchImageView);
        searchTextView = (TextView) findViewById(R.id.searchTextView);
        printerListView = (ListView) findViewById(R.id.printerListView);

        iPrinter = new PrintPP_CPCL();

        searchTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                updateDevices();
                Toast.makeText(view.getContext(), "搜索新设备完成", Toast.LENGTH_SHORT).show();
            }
        });

        printerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                printerName = printerList.get(position).get("name");
                printerAddress = printerList.get(position).get("address");
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), PrintActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("printerName", printerName);
                bundle.putString("printerAddress", printerAddress);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        InputStream is = null;
        try {
            is = getAssets().open("search.png");
            Bitmap searchPic = BitmapFactory.decodeStream(is);
            searchImageView.setImageBitmap(searchPic);
        } catch (IOException e) {
            e.printStackTrace();
        }

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(this, "No Bluetooth Device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!btAdapter.isEnabled()) {
            btAdapter.enable();
            Toast.makeText(this, "Bluetooth Opened", Toast.LENGTH_SHORT).show();
        }

        updateDevices();


    }

    private static final String DISCONNECTED = "android.bluetooth.device.action.ACL_DISCONNECTED";
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter dynamic_filter = new IntentFilter();
        dynamic_filter.addAction(DISCONNECTED);
        registerReceiver(dynamicReceiver, dynamic_filter);
    }

    private BroadcastReceiver dynamicReceiver = new BroadcastReceiver() { //动态广播的Receiver
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DISCONNECTED)){
                iPrinter.disconnect();
            }
        }
    };

    /**
     * 搜索新的设备
     */
    public void updateDevices() {
        printerList = new ArrayList<HashMap<String, String>>();
        printerNameList = new ArrayList<String>();
        printerAddressList = new ArrayList<String>();
        Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
        for (Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext(); ) {
            BluetoothDevice device = iterator.next();
            if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) {
                HashMap<String, String> printer = new HashMap<String, String>();
                ParcelUuid[] uuids = device.getUuids();
                String uuidStr = "";
                for (ParcelUuid uuid : uuids) {
                    uuidStr += uuid;
                }
                if (device.getName().toUpperCase().startsWith("QR380A")) {
                    printer.put("name", device.getName());
                    printer.put("address", device.getAddress());
                    printerList.add(printer);

                    printerNameList.add(device.getName());
                    printerAddressList.add(device.getAddress());
                }
            }
        }
        if (printerList.size() == 0) {
            Toast.makeText(this, "没有搜索到新的设备", Toast.LENGTH_SHORT).show();
        }

        SimpleAdapter adapter = new SimpleAdapter(this, printerList, R.layout.printer_list, new String[]{"name", "address"}, new int[]{R.id.name, R.id.address});
        printerListView.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //setContentView(R.layout.file_list_landscape); //横向
        } else {
            //setContentView(R.layout.file_list); //竖向
        }
    }

}
