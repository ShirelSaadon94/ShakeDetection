package com.classy.shakedetection;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.reflect.TypeToken;


import java.util.ArrayList;
import java.util.Date;


public class ShakeService extends Service  {
    private static final long MIN_ALERT_DELAY = 3000;
    private long lastTimeStamp = 0;
    private static final String TAG = ShakeService.class.getSimpleName();
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String PAUSE_FOREGROUND_SERVICE = "PAUSE_FOREGROUND_SERVICE";
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";
    public static int NOTIFICATION_ID = 1192;
    private int lastShownNotificationId = -1;
    public static String CHANNEL_ID = "com.classy.shakedetection.background.CHANNEL_ID_FOREGROUND";
    public static String MAIN_ACTION = "com.classy.shakedetection.ShakeService.action.main";

    private NotificationCompat.Builder notificationBuilder;
    private boolean isServiceRunningRightNow = false;

    public static PowerManager.WakeLock wakeLock ;
    public static PowerManager powerManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        Log.d("pttt", "onCreate Thread: " + Thread.currentThread().getName());
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                longOperation();
            }
        }).start();

        String action = intent.getAction();

        if (action.equals(START_FOREGROUND_SERVICE)) {
            if (isServiceRunningRightNow) {
                return START_STICKY;
            }
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mAccelerometer = mSensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(sensorEventListener, mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI);

            isServiceRunningRightNow = true;
            notifyToUserForForegroundService();
            powerManager=(PowerManager)getSystemService(POWER_SERVICE);
            wakeLock=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,TAG);
            wakeLock.acquire();
            return START_STICKY;
        } else if (action.equals(PAUSE_FOREGROUND_SERVICE)) {

        } else if (action.equals(STOP_FOREGROUND_SERVICE)) {
            releaseWakeLock();
            mSensorManager.unregisterListener(sensorEventListener,mAccelerometer);
            stopForeground(true);
            stopSelf();
            isServiceRunningRightNow = false;

            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private void releaseWakeLock() {

        if(wakeLock!=null)
            if(wakeLock.isHeld())
                wakeLock.release();
    }


    private void longOperation() {
        Log.d("pttt", "longOperation " + Thread.currentThread().getName());
        int y = 0;
        for (int i = 0; i < 100000; i++) {
            y = 0;
            for (int j = 0; j < 20000; j++) {
                int x = j;
                y = 3 + x;
            }
        }
        Log.d("pttt", "B");
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;

            mAccel = mAccel * 0.9f + delta; // perform low-cut filter



            if (System.currentTimeMillis() > lastTimeStamp + MIN_ALERT_DELAY) {
                if (mAccel > 3) {
                    toast("GUY");
                    lastTimeStamp = System.currentTimeMillis();
                    saveSpeed();
                    Log.d(TAG, "onSensorChanged: "+mAccel);
                    newLocationDetected();
                }

            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    };



    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void saveSpeed() {
        TypeToken typeToken = new TypeToken<ArrayList<Date>>() {
        };

        Date date=new Date();
        ArrayList<Date> dates;
        try {
                dates = MSP.getInstance().getArray(MSP.KEYS.TIME_ARRAY, typeToken);
                dates.add(date);
                MSP.getInstance().putArray(MSP.KEYS.TIME_ARRAY, dates);
                Log.d("ptt", "speeds != null, add value" + dates.toString());
            } catch (Exception e) {
                dates = new ArrayList<>();
                dates.add(date);
                MSP.getInstance().putArray(MSP.KEYS.TIME_ARRAY, dates);
                Log.d("ptt", "speeds == null, add first value" + date.toString());
            }

    }




    private void newLocationDetected() {
        Intent intent = new Intent(MainActivity.BROADCAST_NEW_SHAKE_DETECTED);
        intent.putExtra("EXTRA_SHAKE", mAccel);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        notificationBuilder.setContentText(""+ mAccel);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    @Override
    public void onDestroy() {
        Log.d("pttt", "onDestroy");
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    private void notifyToUserForForegroundService() {
        // On notification click
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = getNotificationBuilder(this,
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        notificationBuilder.setContentIntent(pendingIntent) // Open activity
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_baseline_emoji_people_24)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_grade_24))
                .setContentTitle("Accelerometer in recording")
                .setContentText("Screen")
        ;

        Notification notification = notificationBuilder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        if (NOTIFICATION_ID != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = NOTIFICATION_ID;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String notifications_channel_description = "Cycling map channel";
        String description = notifications_channel_description;
        final NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        if (nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

}