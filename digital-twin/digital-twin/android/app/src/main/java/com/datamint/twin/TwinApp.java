package com.datamint.twin;

import android.app.Application;
import android.content.Context;

public class TwinApp extends Application {
    private static Context ctx;

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getApplicationContext();
    }

    public static Context ctx() { return ctx; }
}
