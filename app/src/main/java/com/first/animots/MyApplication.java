package com.first.animots;

import android.app.Application;
import androidx.lifecycle.ProcessLifecycleOwner;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppLifecycleObserver lifecycleObserver = new AppLifecycleObserver(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
    }
}
