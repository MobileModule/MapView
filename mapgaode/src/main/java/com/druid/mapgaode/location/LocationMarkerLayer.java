package com.druid.mapgaode.location;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.druid.mapbaidu.R;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.layer.Layer;
import com.druid.mapcore.layer.LayerApi;
import com.druid.mapgaode.utils.LatLngUtils;
import com.druid.mapgaode.utils.MapConstantUtils;
import com.druid.mapgaode.utils.MapImageSettingUtils;


public class LocationMarkerLayer implements LayerApi<MapView, AMap>, MapLoadedListener,
        MapLocationChangedListener {
    private SensorManager mSensorManager = null;
    private Context context;
    private MapView mapView = null;
    private AMap googleMap = null;

    public LocationMarkerLayer(Context context) {
        this.context = context;
        initSensor();
    }

    @Override
    public void bindMap(MapView mapView_, AMap map) {
        this.mapView = mapView_;
        this.googleMap = map;
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {

    }

    private boolean mapReady = true;

    @Override
    public void mapReadyLoad(boolean reloaded) {
        mapReady = true;
    }

    private void initSensor() {
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(sensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    protected double mCurrentLat = 0.0;
    protected double mCurrentLon = 0.0;
    private float mCurrentAccracy;

    private SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            double x = sensorEvent.values[SensorManager.DATA_X];
            if (Math.abs(x - lastX) > 1.0) {
                mCurrentDirection = (int) x;
                updateLocationMarkerDirection(mCurrentDirection);
            }
            lastX = x;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //自定义location-layer
    private Marker locationMarker = null;
    private Circle locationCircle = null;

    private void addCustomLocationLayer(LocationBean location) {
        if(!mapReady){
            return;
        }
        LatLng center = LatLngUtils.mapLatLngFrom(location.position);
        if (locationMarker == null) {
            int resId = R.drawable.map_location_marker_follow;
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(resId);
            MarkerOptions markerOption = new MarkerOptions().icon(descriptor)
                    .position(center)
                    .title("")
                    .draggable(false)
                    .zIndex(MapConstantUtils.ZINDEX_MARKER_LOCATION)
                    .rotateAngle(mCurrentDirection);
            locationMarker = googleMap.addMarker(markerOption);
            locationMarker.setFlat(true);
            locationMarker.setAnchor(0.5f, 0.5f);
            locationMarker.hideInfoWindow();
        }
        if (locationCircle == null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(center);
            circleOptions.radius(location.accuracy);
            circleOptions.strokeWidth(2);
            circleOptions.strokeColor(MapImageSettingUtils.getLocationMarkerBorderColor(context));
            circleOptions.fillColor(MapImageSettingUtils.getLocationMarkerFillColor(context));
            locationCircle = googleMap.addCircle(circleOptions);
        }
    }

    private LocationBean location = null;

    public LatLngBean getLocationPosition(){
        if(location!=null){
            return location.position;
        }
        return null;
    }

    @Override
    public void locationChanged(LocationBean location) {
        if (location != null) {
            this.location = location;
            updateLocationMarkerPosition(location);
        }
    }

    public MapLocationChangedListener getMapLocationChangedListener(){
        return this;
    }

    private void updateLocationMarkerPosition(LocationBean location) {
        LatLng center = LatLngUtils.mapLatLngFrom(location.position);
        if (locationMarker == null || locationCircle == null) {
            addCustomLocationLayer(location);
            return;
        }
        if (locationMarker != null) {
            locationMarker.setPosition(center);
        }
        if (locationCircle != null) {
            locationCircle.setCenter(center);
            locationCircle.setRadius(location.accuracy);
        }
    }

    private void updateLocationMarkerDirection(int direction) {
        if (locationMarker != null) {
            locationMarker.setRotateAngle(direction);
        }
    }

    @Override
    public MapLoadedListener getMapLoadedListener() {
        return this;
    }

    @Override
    public MapClickListener getMapClickListener() {
        return null;
    }

    @Override
    public MapOnScaleListener getMapOnScaleListener() {
        return null;
    }

    @Override
    public MapCameraIdleListener getMapCameraIdleListener() {
        return null;
    }

    @Override
    public MapMarkerClickListener getMapMarkerClickListener() {
        return null;
    }

    @Override
    public MapInfoWindowClickListener getMapInfoWindowClickListener() {
        return null;
    }

    @Override
    public boolean setLayerVisible(boolean visible) {
        return false;
    }

    @Override
    public void cameraToLayer() {

    }

    @Override
    public void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(sensorListener);
        }
        if (locationMarker != null) {
            locationMarker.remove();
            locationMarker = null;
        }
        if (locationCircle != null) {
            locationCircle.remove();
            locationCircle.remove();
        }
    }
}
