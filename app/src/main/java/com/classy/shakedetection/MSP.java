package com.classy.shakedetection;

import android.content.Context;
import android.content.SharedPreferences;


import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.gson.reflect.TypeToken;


import com.google.gson.Gson;

import java.io.IOException;


import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;


public class MSP {
    public interface KEYS {

        final String TIME_ARRAY = "TIME_ARRAY";

    }

    private static MSP instance;
    private SharedPreferences prefs;

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public static MSP getInstance() {
        return instance;
    }

    private MSP(Context context, boolean isEncrypted) {

        try {
            if (!isEncrypted) {
                prefs = context.getApplicationContext().getSharedPreferences("APP_SPV", Context.MODE_PRIVATE);
            } else {
                String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                prefs = EncryptedSharedPreferences.create(
                        "APP_SPV",
                        masterKeyAlias,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            }
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * init the library
     * @param context of the application -try to init with Application context
     * @param isEncrypted true for encrypted file, default = false
     */
    public static void initHelper(Context context, boolean isEncrypted) {
        if (instance == null) {
            context = context.getApplicationContext();
            instance = new MSP(context, isEncrypted);
        }
    }

    /**
     * init the library, default Encrypted = false
     * @param context of the application -try to init with Application context
     */
    public static void initHelper(Context context) {
        initHelper(context, false);
    }

    public void deleteAll(){
        prefs.edit().clear().commit();
    }

    public <T> void putArray(String KEY, ArrayList<Date> array) {




        String json = new Gson().toJson(array);
        prefs.edit().putString(KEY, json).apply();
    }

    public <T> void putArray1(String KEY, ArrayList<Date> array) {




        String json = new Gson().toJson(array);
        prefs.edit().putString(KEY, json).apply();
    }


    public <T> ArrayList<T> getArray(String KEY, TypeToken typeToken) {
        ArrayList<T> arr = null;
        try {
            arr = new Gson().fromJson(prefs.getString(KEY, ""), typeToken.getType());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return arr;
    }

}