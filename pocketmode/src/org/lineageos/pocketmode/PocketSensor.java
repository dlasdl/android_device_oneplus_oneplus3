/*
 * Copyright (c) 2016 The CyanogenMod Project
 *               2018-2019 The LineageOS Project
 *
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

package org.lineageos.pocketmode;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.FileUtils;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;

public class PocketSensor implements SensorEventListener {

    private static final boolean DEBUG = false;
    private static final String TAG = "OnePlusPocketMode-PocketSensor";

    private static final String FPC_FILE = "/sys/devices/soc/soc:fpc_fpc1020/proximity_state";

    private Context mContext;
    private ExecutorService mExecutorService;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    public PocketSensor(Context context) {
        mContext = context;
        mExecutorService = Executors.newSingleThreadExecutor();
        mSensorManager = mContext.getSystemService(SensorManager.class);
        mSensor = findSensorWithType("com.oneplus.sensor.pocket");
    }

    private Future<?> submit(Runnable runnable) {
        return mExecutorService.submit(runnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean isNear = event.values[0] == 1;
        try {
            FileUtils.stringToFile(FPC_FILE, isNear ? "1" : "0");
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to " + FPC_FILE, e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* Empty */
    }

    protected void enable() {
        if (DEBUG) Log.d(TAG, "Enabling");
        submit(() -> {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        });
    }

    protected void disable() {
        if (DEBUG) Log.d(TAG, "Disabling");
        submit(() -> {
            mSensorManager.unregisterListener(this, mSensor);
        });
    }

    protected Sensor findSensorWithType(String type) {
        for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
            if (type.equals(sensor.getStringType())) {
                return sensor;
            }
        }

        return null;
    }
}
