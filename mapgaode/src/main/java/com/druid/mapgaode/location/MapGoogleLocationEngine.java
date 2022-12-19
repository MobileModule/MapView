package com.druid.mapgaode.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.utils.MapLog;
import com.druid.mapgaode.utils.LatLngUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.Executor;

public class MapGoogleLocationEngine {
    public static final String TAG = MapGoogleLocationEngine.class.getName();
    private Context context;
    private MapLocationChangedListener listener;

    public MapGoogleLocationEngine(Context context, long intervalTime) {
        this.context = context;
        if (intervalTime > 0) {
            this.DEFAULT_MAX_WAIT_TIME = intervalTime;
            this.DEFAULT_FASTEST_INTERVAL = intervalTime;
        }
    }

    public MapGoogleLocationEngine setLocationChangedListener(MapLocationChangedListener listener) {
        this.listener = listener;
        return this;
    }

    //位置更新之间的距离
    public static final float DEFAULT_DISPLACEMENT = 0f;//0.5m
    //位置更新的最大等待时间（以毫秒为单位）。
    private long DEFAULT_MAX_WAIT_TIME = 1 * 1000L;
    //位置更新的最快间隔（以毫秒为单位）
    private long DEFAULT_FASTEST_INTERVAL = 1 * 1000L;
    //位置更新之间的默认间隔
    public static final long DEFAULT_INTERVAL = 1 * 1000L;


    private LocationRequest getLocationRequest() {
        LocationRequest mLocationEngineRequest = new LocationRequest();
        mLocationEngineRequest.setInterval(DEFAULT_INTERVAL);
        //要求最准确的位置
        mLocationEngineRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //请求经过电池优化的粗略位置
//        mLocationEngineRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //要求粗略〜10 km的准确位置
//        mLocationEngineRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        //被动位置：除非其他客户端请求位置更新，否则不会返回任何位置
//        mLocationEngineRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        //设置位置更新之间的距离
        //  mLocationEngineRequest.setSmallestDisplacement(DEFAULT_DISPLACEMENT);
        //设置位置更新的最大等待时间（以毫秒为单位）。
        mLocationEngineRequest.setMaxWaitTime(DEFAULT_MAX_WAIT_TIME);
        //设置位置更新的最快间隔（以毫秒为单位）
        mLocationEngineRequest.setFastestInterval(DEFAULT_FASTEST_INTERVAL);
        mLocationEngineRequest.setNumUpdates(Integer.MAX_VALUE);
        //mLocationEngineRequest.setExpirationDuration(1000);
        return mLocationEngineRequest;
    }

    private FusedLocationProviderClient mLocationEngine;


    public void onResume() {
        mLocationEngine = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationEngine.requestLocationUpdates(getLocationRequest(), locationCallback, Looper.myLooper());
//        getLastLocation();
    }

    void getLastLocation() {
        if (mLocationEngine != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Task<Location> locationTask = mLocationEngine.getLastLocation();
            locationTask.addOnSuccessListener((Executor) this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location lastLocation) {
                    // Got last known location
                    callBackLastLocation(lastLocation);
                }
            });
        }
    }

    public void onPause() {
        if (mLocationEngine != null) {
            mLocationEngine.removeLocationUpdates(locationCallback);
        }
    }

    LocationCallback locationCallback = new LocationCallback() {
        public void onLocationResult(LocationResult locationResult) {
            Location lastLocation = locationResult.getLastLocation();
            callBackLastLocation(lastLocation);
        }
    };

    private void callBackLastLocation(Location lastLocation) {
        if (lastLocation != null) {
            MapLog.warning(TAG, "onSuccess:" + lastLocation.getLatitude() + "," + lastLocation.getLongitude());
            LatLngBean latLngBean = LatLngUtils.mapLatLngParse(lastLocation);
            if (listener != null) {
                LocationBean centerLocation = new LocationBean(latLngBean);
                centerLocation.accuracy = lastLocation.getAccuracy();
                listener.locationChanged(centerLocation);
            }
        } else {
            MapLog.error(TAG, "onFailure:" + "null");
        }
    }
}
