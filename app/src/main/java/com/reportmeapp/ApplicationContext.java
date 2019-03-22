package com.reportmeapp;

import android.app.Application;


import net.gotev.uploadservice.UploadService;

/**
 * Created by BHUSRI on 9/7/2017.
 */

public class ApplicationContext extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
    }
}
