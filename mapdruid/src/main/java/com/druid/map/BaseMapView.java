package com.druid.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.druid.mapcore.notify.PermissionsTips;
import com.druid.mapcore.utils.PermissionCode;
import com.druid.mapcore.utils.PermissionListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class BaseMapView extends LinearLayout {
    public BaseMapView(Context context) {
        super(context);
    }

    public BaseMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BaseMapView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private HashMap<String, PermissionListener> locationPermissions = new HashMap<>();

    protected void addLocationPermission(String permissionId, PermissionListener gpsPermissionListener) {
        if (gpsPermissionListener == null) {
            return;
        }
        if (TextUtils.isEmpty(permissionId)) {
            return;
        }
        if (!locationPermissions.containsKey(permissionId)) {
            locationPermissions.put(permissionId, gpsPermissionListener);
        }
    }

    protected boolean checkLocationPermissions(String permissionId, PermissionListener gpsPermissionListener) {
        addLocationPermission(permissionId, gpsPermissionListener);
        int flag = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (PackageManager.PERMISSION_GRANTED != flag) {
            showGPSPermissionDialog(getContext());
            return false;
        } else {
            Iterator<Map.Entry<String, PermissionListener>> it = locationPermissions.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, PermissionListener> entry = it.next();
                if (entry.getValue() != null) {
                    entry.getValue().permissionAgree(PermissionCode.LOCATION);
                }
            }
            return true;
        }
    }

    PermissionsTips permissionsTips;

    private void showGPSPermissionDialog(Context context) {
        if (permissionsTips == null) {
            String tips = "";
            tips += getResources().getString(R.string.permission_gps_title_1) + "\n";
            tips += getResources().getString(R.string.permission_gps_title_2) + "\n";
            tips += getResources().getString(R.string.permission_gps_title_3);
            permissionsTips = new PermissionsTips(context);
            permissionsTips.setTitle(getResources().getString(R.string.location_permission_title));
            permissionsTips.setContentTips(
                    getResources().getString(R.string.permission_gps_description));
            permissionsTips.setContent(tips);
            permissionsTips.setContentGravity(Gravity.LEFT);
            permissionsTips.setAttention(getResources().getString(R.string.permission_gps_notice));
            permissionsTips.setCanceledOnTouchOutside(false);
            permissionsTips.setCancelable(false);
        }
        PermissionsTips.PermissionClickListener listener = new PermissionsTips.PermissionClickListener() {
            @Override
            public void clickPermissionCancel() {
                Iterator<Map.Entry<String, PermissionListener>> it = locationPermissions.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, PermissionListener> entry = it.next();
                    if (entry.getValue() != null) {
                        entry.getValue().permissionRefuse(PermissionCode.LOCATION);
                    }
                }
            }

            @Override
            public void clickPermissionConfirm() {
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    }, PermissionCode.LOCATION);
                }
            }
        };
        permissionsTips.setListener(listener);
        permissionsTips.show();
    }
}
