package com.druid.mapbox.location;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.druid.mapbox.utils.LocationParseUtils;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.utils.MapLog;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;

public class MapBoxLocationEngine implements LocationEngineCallback<LocationEngineResult> {
    public static final String TAG = MapBoxLocationEngine.class.getName();
    private Context context;
    private MapLocationChangedListener listener;

    public MapBoxLocationEngine(Context context, long intervalTime) {
        this.context = context;
        if (intervalTime > 0) {
            this.DEFAULT_MAX_WAIT_TIME = intervalTime;
            this.DEFAULT_FASTEST_INTERVAL = intervalTime;
            this.DEFAULT_INTERVAL = intervalTime;
        }
    }

    public MapBoxLocationEngine setLocationChangedListener(MapLocationChangedListener listener) {
        this.listener = listener;
        return this;
    }

    //位置更新之间的距离
    public static final float DEFAULT_DISPLACEMENT = 0f;//0.5m
    //位置更新的最大等待时间（以毫秒为单位）。
    private long DEFAULT_MAX_WAIT_TIME = 5 * 1000L;
    //位置更新的最快间隔（以毫秒为单位）
    private long DEFAULT_FASTEST_INTERVAL = 5 * 1000L;
    //位置更新之间的默认间隔
    public long DEFAULT_INTERVAL = 2 * 1000L;

    private LocationEngineRequest mLocationEngineRequest = new LocationEngineRequest.Builder(DEFAULT_INTERVAL)
            //要求最准确的位置
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            //请求经过电池优化的粗略位置
//            .setPriority(LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            //要求粗略〜10 km的准确位置
//            .setPriority(LocationEngineRequest.PRIORITY_LOW_POWER)
            //被动位置：除非其他客户端请求位置更新，否则不会返回任何位置
//            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
            //设置位置更新之间的距离
            .setDisplacement(DEFAULT_DISPLACEMENT)
            //设置位置更新的最大等待时间（以毫秒为单位）。
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
            //设置位置更新的最快间隔（以毫秒为单位）
            .setFastestInterval(DEFAULT_FASTEST_INTERVAL)
            .build();

    private LocationEngine mLocationEngine;


    public void onResume() {
        mLocationEngine = LocationEngineProvider.getBestLocationEngine(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationEngine.requestLocationUpdates(mLocationEngineRequest, this, Looper.getMainLooper());
    }

    public void onPause() {
        if (mLocationEngine != null) {
            mLocationEngine.removeLocationUpdates(this);
        }
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        Location lastLocation = result.getLastLocation();
        MapLog.warning(TAG, "onSuccess:" + lastLocation.getLatitude() + "," + lastLocation.getLongitude());
        if (lastLocation != null) {
            if (listener != null) {
                LocationBean centerLocation = LocationParseUtils.getLocation(lastLocation);
                listener.locationChanged(centerLocation);
            }
        }
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
        MapLog.error(TAG, "onFailure:" + exception.getMessage());
    }

}
