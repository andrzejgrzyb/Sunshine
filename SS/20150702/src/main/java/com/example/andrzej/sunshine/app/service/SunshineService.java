package com.example.andrzej.sunshine.app.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by andrzej on 02.07.15.
 */
public class SunshineService extends IntentService {
    public SunshineService(String name) {
        super("SunshineService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
