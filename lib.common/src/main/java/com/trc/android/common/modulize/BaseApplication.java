package com.trc.android.common.modulize;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private List<ModuleInterface> moduleInstanceList = Collections.EMPTY_LIST;

    protected Class<? extends ModuleInterface>[] moduleClasses() {
        return new Class[0];
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initModuleInstance();
        invokeModuleOnCreate();
    }

    private void initModuleInstance() {
        Class<? extends ModuleInterface>[] classes = moduleClasses();
        moduleInstanceList = new ArrayList<>(classes.length);
        try {
            for (Class<? extends ModuleInterface> clazz : classes) {
                moduleInstanceList.add(clazz.newInstance());
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void invokeModuleOnCreate() {
        for (ModuleInterface moduleInterface : moduleInstanceList) {
            moduleInterface.onApplicationCreate(this);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
