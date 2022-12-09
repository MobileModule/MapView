package com.druid.map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.druid.mapcore.utils.PermissionListener;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.ImageRegisterBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.MapEngineType;
import com.druid.mapcore.capture.CaptureDrawLayerApi;
import com.druid.mapcore.circle.CircleDrawLayerApi;
import com.druid.mapcore.cluster.ClusterLayer;
import com.druid.mapcore.fence.api.FenceListLayerApi;
import com.druid.mapcore.fence.api.circle.FenceDrawCircleLayerApi;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByAutoLocationLayer;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByHandLayer;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByInputLayer;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByManualLocationLayer;
import com.druid.mapcore.fence.api.rectangle.FenceDrawRectangleLayerApi;
import com.druid.mapcore.heatmap.HeatMapLayerApi;
import com.druid.mapcore.interfaces.ClusterStatusChangedListener;
import com.druid.mapcore.interfaces.LineTrackMoveListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.line.LineDrawLayerApi;
import com.druid.mapcore.navigate.NavigateLayerApi;
import com.druid.mapcore.utils.BuildConfigUtils;
import com.druid.mapcore.utils.MapLog;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class UIMapView extends BaseMapView implements View.OnClickListener,
        DruidMapView, ClusterStatusChangedListener, LineTrackMoveListener {
    public static final String TAG = UIMapView.class.getName();

    public UIMapView(Context context) {
        super(context);
        initView();
    }

    public UIMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTypedArray(context, attrs);
        initView();
    }

    public UIMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs);
        initView();
    }

    boolean toolboxMenuVisible = false;
    boolean clusterChangeMenuVisible = false;
    boolean cameraMoveMenuVisible = true;
    boolean mineLocationMenuVisible = true;
    boolean mapZoomMenuVisible = true;
    boolean mapSourceMenuVisible = true;

    int mapEngineType = MapEngineType.MapBox.getValue();

    void initTypedArray(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UIMapView);
            toolboxMenuVisible = a.getBoolean(R.styleable.UIMapView_visibleToolBoxMenu, toolboxMenuVisible);
            clusterChangeMenuVisible = a.getBoolean(R.styleable.UIMapView_visibleClusterMenu, clusterChangeMenuVisible);
            cameraMoveMenuVisible = a.getBoolean(R.styleable.UIMapView_visibleCameraMoveMenu, cameraMoveMenuVisible);
            mineLocationMenuVisible = a.getBoolean(R.styleable.UIMapView_visibleMineLocationMenu, mineLocationMenuVisible);
            mapZoomMenuVisible = a.getBoolean(R.styleable.UIMapView_visibleZoomMenu, mapZoomMenuVisible);
            mapSourceMenuVisible = a.getBoolean(R.styleable.UIMapView_visibleSourceMenu, mapSourceMenuVisible);
        }
    }

    FrameLayout mapLoaderContainer;
    public DruidMapView druidMapView;
    ImageView img_map_source_switch, img_map_toolbox, img_map_switch_cluster, img_map_camera_move_bounds;
    View view_toolbox_menu;
    public ImageView img_line_router_player, img_camera_to_mine, img_map_zoom_out, img_map_zoom_in;
    public LinearLayout ll_toolbox_menu_cluster, ll_toolbox_menu_point, ll_toolbox_menu_heatmap, ll_toolbox_menu_line, ll_map_zoom;
    public ImageView img_toolbox_menu_cluster, img_toolbox_menu_point, img_toolbox_menu_heatmap, img_toolbox_menu_line;
    public View view_map_support, view_map_toolbox;
    TextView tv_map_not_support;
    private int trackerStartRsd = R.drawable.icon_map_tracker_start;
    private int trackerStopRsd = R.drawable.icon_map_tracker_stop;
    private boolean isTrackering = false;

    void initView() {
        inflate(getContext(), R.layout.layout_ui_mapview, this);
        mapLoaderContainer = findViewById(R.id.mapLoaderContainer);
        img_map_source_switch = findViewById(R.id.img_map_source_switch);
        img_map_source_switch.setOnClickListener(this);
        img_map_toolbox = findViewById(R.id.img_map_toolbox);
        img_map_toolbox.setOnClickListener(this);
        view_toolbox_menu = findViewById(R.id.view_toolbox_menu);
        img_map_switch_cluster = findViewById(R.id.img_map_switch_cluster);
        img_map_switch_cluster.setOnClickListener(this);
        img_map_camera_move_bounds = findViewById(R.id.img_map_camera_move_bounds);
        img_map_camera_move_bounds.setOnClickListener(this);
        img_line_router_player = findViewById(R.id.img_line_router_player);
        img_line_router_player.setOnClickListener(this);
        img_camera_to_mine = findViewById(R.id.img_camera_to_mine);
        img_camera_to_mine.setOnClickListener(this);
        img_map_zoom_out = findViewById(R.id.img_map_zoom_out);
        img_map_zoom_out.setOnClickListener(this);
        img_map_zoom_in = findViewById(R.id.img_map_zoom_in);
        img_map_zoom_in.setOnClickListener(this);
        view_map_toolbox = findViewById(R.id.view_map_toolbox);
        view_map_support = findViewById(R.id.view_map_support);
        view_map_support.setVisibility(GONE);
        tv_map_not_support = findViewById(R.id.tv_map_not_support);
        ll_toolbox_menu_cluster = findViewById(R.id.ll_toolbox_menu_cluster);
        ll_toolbox_menu_cluster.setOnClickListener(this);
        ll_toolbox_menu_point = findViewById(R.id.ll_toolbox_menu_point);
        ll_toolbox_menu_point.setOnClickListener(this);
        ll_toolbox_menu_heatmap = findViewById(R.id.ll_toolbox_menu_heatmap);
        ll_toolbox_menu_heatmap.setOnClickListener(this);
        ll_toolbox_menu_line = findViewById(R.id.ll_toolbox_menu_line);
        ll_toolbox_menu_line.setOnClickListener(this);
        ll_map_zoom = findViewById(R.id.ll_map_zoom);
        img_toolbox_menu_cluster = findViewById(R.id.img_toolbox_menu_cluster);
        img_toolbox_menu_point = findViewById(R.id.img_toolbox_menu_point);
        img_toolbox_menu_heatmap = findViewById(R.id.img_toolbox_menu_heatmap);
        img_toolbox_menu_line = findViewById(R.id.img_toolbox_menu_line);
        setViewVisible();
        //
        findViewById(R.id.ll_toolbox_menu_cluster).setOnClickListener(this);
    }

    public boolean loadMapEngine(MapEngineType mapEngineTypeEnum) {
        this.mapEngineType = mapEngineTypeEnum.getValue();
        return loadMapEngine();
    }

    private boolean loadMapEngine() {
        View mapView = null;
        Class mapViewClass;
        Class parameter = Context.class;
        Constructor constructor = null;
        if (mapEngineType == 0) {//googleMap
            try {
                mapViewClass = Class.forName("com.druid.mapbox.MapBoxView");
                constructor = mapViewClass.getConstructor(parameter);
                mapView = (View) constructor.newInstance(getContext());
            } catch (Exception ex) {
                ex.printStackTrace();
                MapLog.error(TAG, ex.getMessage());
                loadMapEngineError(ex.getMessage());
                return false;
            }
        } else {
            if (BuildConfigUtils.buildChina(getContext())) {
                String message = getResources().getText(R.string.china_build_error).toString();
                loadMapEngineError(message);
                //todo error
                return false;
            } else {
                try {
                    mapViewClass = Class.forName("com.druid.mapgoogle.MapGoogleView");
                    constructor = mapViewClass.getConstructor(parameter);
                    mapView = (View) constructor.newInstance(getContext());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MapLog.error(TAG, ex.getMessage());
                    loadMapEngineError(ex.getMessage());
                    return false;
                }
            }
        }
        if (mapView != null) {
            mapLoaderContainer.addView(mapView);
            if (mapView instanceof DruidMapView) {
                this.druidMapView = (DruidMapView) mapView;
            }
        }
        return true;
    }

    private void loadMapEngineError(String message) {
        view_map_support.setVisibility(VISIBLE);
        tv_map_not_support.setText(message);
        MapLog.warning(TAG, message);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(getResources().getString(R.string.dialog_tips))
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        alertDialog.show();
        (alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)).setTextColor(getResources().getColor(R.color.fence_polyline));
    }

    void setViewVisible() {
        if (mapSourceMenuVisible) {
            img_map_source_switch.setVisibility(VISIBLE);
        } else {
            img_map_source_switch.setVisibility(View.GONE);
        }
        if (toolboxMenuVisible) {
            img_map_toolbox.setVisibility(View.VISIBLE);
        } else {
            img_map_toolbox.setVisibility(View.GONE);
        }
        if (clusterChangeMenuVisible) {
            img_map_switch_cluster.setVisibility(View.VISIBLE);
        } else {
            img_map_switch_cluster.setVisibility(View.GONE);
        }
        if (cameraMoveMenuVisible) {
            img_map_camera_move_bounds.setVisibility(View.VISIBLE);
        } else {
            img_map_camera_move_bounds.setVisibility(View.GONE);
        }
        if (mineLocationMenuVisible) {
            img_camera_to_mine.setVisibility(View.VISIBLE);
        } else {
            img_camera_to_mine.setVisibility(View.GONE);
        }
        if (mapZoomMenuVisible) {
            img_map_zoom_in.setVisibility(View.VISIBLE);
            img_map_zoom_out.setVisibility(View.VISIBLE);
        } else {
            img_map_zoom_in.setVisibility(View.GONE);
            img_map_zoom_out.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int clickId = v.getId();
        if (clickId == R.id.img_map_source_switch) {
            changeMapSource();
        }
        if (clickId == R.id.img_map_toolbox) {
            showToolBoxMenu();
        }
        if (clickId == R.id.img_map_switch_cluster) {
            changClusterStyle();
        }
        if (clickId == R.id.img_map_camera_move_bounds) {
            changeClusterCamera();
        }
        if (clickId == R.id.img_line_router_player) {
            if (lineDrawLayerApi != null) {
                if (isTrackering) {
                    //正在播放，暂停
                    lineDrawLayerApi.playTracker(false);
                    img_line_router_player.setImageResource(trackerStopRsd);
                } else {
                    //暂停中，开始播放
                    lineDrawLayerApi.playTracker(true);
                    img_line_router_player.setImageResource(trackerStartRsd);
                }
                isTrackering = !isTrackering;
            }
        }
        if (clickId == R.id.img_camera_to_mine) {
            cameraMineLocation();
        }
        if (clickId == R.id.img_map_zoom_out) {
               zoomMap(true);
        }
        if (clickId == R.id.img_map_zoom_in) {
             zoomMap(false);
        }
    }

    @Override
    public void lineTrackMoveStop() {
        isTrackering = false;
        img_line_router_player.setImageResource(trackerStopRsd);
    }

    public LinearLayout getMapZoomView() {
        return ll_map_zoom;
    }

    private boolean isVectorMapSource = true;

    void changeMapSource() {
        if (isVectorMapSource) {
            if (druidMapView != null)
                druidMapView.changeMapSourceStyleSatellite();
            img_map_source_switch.setImageResource(R.drawable.icon_map_source_vector);
        } else {
            if (druidMapView != null)
                druidMapView.changeMapSourceStyleStreet();
            img_map_source_switch.setImageResource(R.drawable.icon_map_source_satellite);
        }
        isVectorMapSource = !isVectorMapSource;
    }

    private void showToolBoxMenu() {
        if (!toolboxMenuVisible) {
            closeAnimToolboxMenuView();
            img_map_toolbox.setImageResource(R.drawable.icon_map_toolbox);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    view_toolbox_menu.setVisibility(View.GONE);
                }
            }, 100);
        } else {
            view_toolbox_menu.setVisibility(View.VISIBLE);
            openAnimToolboxMenuView();
            img_map_toolbox.setImageResource(R.drawable.icon_map_toolbox_press);
        }
        toolboxMenuVisible = !toolboxMenuVisible;
    }

    void openAnimToolboxMenuView() {
        Animation scaleAnimation = new ScaleAnimation(0.1f, 1.0f, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(200);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(800);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        view_toolbox_menu.startAnimation(animationSet);
    }

    void closeAnimToolboxMenuView() {
        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.1f, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.1f);
        scaleAnimation.setDuration(200);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.1f);
        alphaAnimation.setDuration(150);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        view_toolbox_menu.startAnimation(animationSet);
    }

    void changClusterStyle() {
        if (druidMapView != null)
            if (druidMapView.drawClusterLayer().changeMarkerCluster()) {

            }
    }

    @Override
    public void clusterStatusChanged(boolean isCluster) {
        int resId = R.drawable.icon_cluster_status_part;
        if (!isCluster) {
            resId = R.drawable.icon_cluster_status_together;
        }
        img_map_switch_cluster.setImageResource(resId);
    }

    boolean isClusterCameraToBounds = true;

    void changeClusterCamera() {
        int resId = R.drawable.icon_camera_to_mine;
        if (isClusterCameraToBounds) {
            cameraMineLocation();
            resId = R.drawable.icon_camera_to_mine;
        } else {
            cameraToLayer();
            resId = R.drawable.icon_camera_to_bounds;
        }
        img_map_camera_move_bounds.setImageResource(resId);
        isClusterCameraToBounds = !isClusterCameraToBounds;
    }

    private void cameraToLayer() {
        if (clusterLayer != null) {
            clusterLayer.cameraToLayer();
        }
    }

    @Override
    public void registerMapMarkerImages(ArrayList<ImageRegisterBean> images) {
        if (druidMapView != null) {
            druidMapView.registerMapMarkerImages(images);
        }
    }

    @Override
    public void loadingMap(MapLoadedListener loadedListener) {
        if (druidMapView != null)
            druidMapView.loadingMap(loadedListener);
    }

    @Override
    public void enableGesture(boolean enable) {
        if (druidMapView != null) {
            druidMapView.enableGesture(enable);
        }
    }

    @Override
    public LatLngBean getCameraTarget() {
        if (druidMapView != null)
            return druidMapView.getCameraTarget();
        return null;
    }

    @Override
    public void setMapLanguage(String lan) {
        if (druidMapView != null)
            druidMapView.setMapLanguage(lan);
    }

    private boolean firstLocationMine=false;

    @Override
    public void locationMineEnable(String permissionId, PermissionListener permissionListener,boolean firstLocationMine) {
        this.firstLocationMine=firstLocationMine;
        addLocationPermission(permissionId, permissionListener);
        boolean permission= checkLocationPermissions(TAG, locationEnablePermission);
    }

    PermissionListener locationEnablePermission = new PermissionListener() {
        @Override
        public void permissionAgree(int code) {
            if (druidMapView != null) {
                druidMapView.locationMineEnable("", null,firstLocationMine);
            }
        }

        @Override
        public void permissionRefuse(int code) {

        }

        @Override
        public void permissionRefuseAndNotNotify(int code) {

        }
    };

    @Override
    public boolean cameraMineLocation() {
        boolean permission = checkLocationPermissions(TAG, locationEnablePermission);
        if (permission) {
            if (druidMapView != null)
                druidMapView.cameraMineLocation();
        }
        return permission;
    }

    @Override
    public void locationEngineEnable(long intervalTime) {
        boolean permission = checkLocationPermissions(TAG, locationEnablePermission);
        if (permission) {
            if (druidMapView != null)
                druidMapView.locationEngineEnable(intervalTime);
        }
    }

    @Override
    public void cameraAnimationLocationLatLng(LatLngBean latLng) {
        if (druidMapView != null)
            druidMapView.cameraAnimationLocationLatLng(latLng);
    }

    @Override
    public void changeMapSourceStyleStreet() {
        if (druidMapView != null)
            druidMapView.changeMapSourceStyleStreet();
    }

    @Override
    public void changeMapSourceStyleSatellite() {
        if (druidMapView != null)
            druidMapView.changeMapSourceStyleSatellite();
    }

    @Override
    public boolean getStatusMapTile() {
        if (druidMapView != null)
            return druidMapView.getStatusMapTile();
        return false;
    }

    @Override
    public void zoomMap(boolean isOut) {
        if (druidMapView != null)
            druidMapView.zoomMap(isOut);
    }

    @Override
    public void resetMarkerMainClickEvent() {
        if (druidMapView != null)
            druidMapView.resetMarkerMainClickEvent();
    }

    public HeatMapLayerApi heatMapLayer;

    @Override
    public HeatMapLayerApi drawHeatMapLayer() {
        if (druidMapView != null) {
            heatMapLayer = druidMapView.drawHeatMapLayer();
            return heatMapLayer;
        }
        return null;
    }

    public ClusterLayer clusterLayer;

    @Override
    public ClusterLayer drawClusterLayer() {
        if (druidMapView != null) {
            if (clusterLayer == null) {
                clusterLayer = druidMapView.drawClusterLayer();
                clusterLayer.setClusterStatusChangedListener(this);
            }
            return clusterLayer;
        }
        return null;
    }

    @Override
    public FenceDrawPolygonByHandLayer drawFencePolygonByHandLayer() {
        if (druidMapView != null)
            return druidMapView.drawFencePolygonByHandLayer();
        return null;
    }

    @Override
    public FenceDrawPolygonByAutoLocationLayer drawFencePolygonByAutoLocationLayer() {
        if (druidMapView != null)
            return druidMapView.drawFencePolygonByAutoLocationLayer();
        return null;
    }

    @Override
    public FenceDrawPolygonByManualLocationLayer drawFencePolygonByManualLocationLayer() {
        if (druidMapView != null)
            return druidMapView.drawFencePolygonByManualLocationLayer();
        return null;
    }

    @Override
    public FenceDrawPolygonByInputLayer drawFencePolygonByInputLayer() {
        if (druidMapView != null)
            return druidMapView.drawFencePolygonByInputLayer();
        return null;
    }

    @Override
    public FenceDrawCircleLayerApi drawFenceCircleLayer() {
        if (druidMapView != null)
            return druidMapView.drawFenceCircleLayer();
        return null;
    }

    @Override
    public FenceDrawRectangleLayerApi drawFenceRectangleLayer() {
        if (druidMapView != null)
            return druidMapView.drawFenceRectangleLayer();
        return null;
    }

    @Override
    public FenceListLayerApi drawFenceListLayer() {
        if (druidMapView != null)
            return druidMapView.drawFenceListLayer();
        return null;
    }

    public LineDrawLayerApi lineDrawLayerApi;

    @Override
    public LineDrawLayerApi drawLineLayer() {
        if (druidMapView != null) {
            lineDrawLayerApi = druidMapView.drawLineLayer();
            lineDrawLayerApi.setPlayTrackListener(this);
            return lineDrawLayerApi;
        }
        return null;
    }

    @Override
    public NavigateLayerApi drawNavigateLayer() {
        if (druidMapView != null)
            return druidMapView.drawNavigateLayer();
        return null;
    }

    @Override
    public CircleDrawLayerApi drawCircleLayer() {
        if (druidMapView != null)
            return druidMapView.drawCircleLayer();
        return null;
    }

    @Override
    public CaptureDrawLayerApi drawCaptureLayer() {
        if (druidMapView != null)
            return druidMapView.drawCaptureLayer();
        return null;
    }

    @Override
    public void onStart() {
        if (druidMapView != null)
            druidMapView.onStart();
    }

    @Override
    public void onResume() {
        if (druidMapView != null)
            druidMapView.onResume();
    }

    @Override
    public void onPause() {
        if (druidMapView != null)
            druidMapView.onPause();
    }

    @Override
    public void onStop() {
        if (druidMapView != null)
            druidMapView.onStop();
    }

    @Override
    public void onCreate(Bundle outState) {
        if (druidMapView != null) {
            druidMapView.onCreate(outState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (druidMapView != null) {
            druidMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        if (druidMapView != null)
            druidMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        if (druidMapView != null)
            druidMapView.onLowMemory();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

}
