package com.classy.shakedetection;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    public static final String BROADCAST_NEW_SHAKE_DETECTED = "com.classy.shakedetection.NEW_SHAKE_DETECTED";
    private static final String TAG = "main";
    MaterialButton main_BTN_START_Service;
    MaterialButton main_BTN_STOP_Service;
    MaterialButton main_BTN_history;
    MaterialButton main_BTN_restart;

    TextView main_TXT_history;

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("pttt", "onReceive" + intent.getFloatExtra("EXTRA_SHAKE", -1));




        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        initViews();




    }

    private void initViews() {

        main_BTN_START_Service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                actionToService(ShakeService.START_FOREGROUND_SERVICE);

            }
        });
        main_BTN_STOP_Service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionToService(ShakeService.STOP_FOREGROUND_SERVICE);
            }
        });
        main_BTN_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showData();

            }
        });
        main_BTN_restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTheExistsArr();
            }
        });
    }

    private void deleteTheExistsArr() {

            MSP.getInstance().deleteAll();
            Toast.makeText(MainActivity.this, "RESTART DATA", Toast.LENGTH_LONG).show();

            main_TXT_history.setText("LIST IS EMPTY");
    }





    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: ");
        IntentFilter intentFilter = new IntentFilter(BROADCAST_NEW_SHAKE_DETECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, intentFilter);
        registerReceiver(myReceiver, intentFilter);
//        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }


    @Override
    protected void onPause() {
        
        super.onPause();
        Log.d(TAG, "onPause: ");

       // LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
    }





    private void actionToService(String action) {
        Intent startIntent = new Intent(MainActivity.this, ShakeService.class);
        startIntent.setAction(action);
        // makeAlarm();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "actionToService: j");
            //startForegroundService(startIntent);
            // or
            ContextCompat.startForegroundService(this, startIntent);
        } else {
            Log.d(TAG, "actionToService: 99");
            startService(startIntent);
        }
    }



    @SuppressLint("SetTextI18n")
    private void showData() {
        TypeToken typeToken = new TypeToken<ArrayList<Date>>() {
        };
        ArrayList<Date> arr = MSP.getInstance().getArray(MSP.KEYS.TIME_ARRAY, typeToken);

        if(arr!=null) {
            for (int i = 0; i < arr.size(); i++) {

                main_TXT_history.setText(main_TXT_history.getText() + "" + (i + 1) + ".  " + arr.get(i) + "  " + "" + "\n");
            }
        }
        else
        {
            main_TXT_history.setText("LIST IS EMPTY");
        }
    }

    private void findViews() {
        main_BTN_START_Service = findViewById(R.id.main_BTN_START_service);
        main_BTN_STOP_Service = findViewById(R.id.main_BTN_STOP_Service);
        main_BTN_history = findViewById(R.id.main_BTN_history);
        main_TXT_history = findViewById(R.id.main_TXT_history);
        main_BTN_restart = findViewById(R.id.main_BTN_restart);
    }
}