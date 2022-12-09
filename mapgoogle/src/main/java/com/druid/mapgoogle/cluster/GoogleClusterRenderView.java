package com.druid.mapgoogle.cluster;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.druid.mapgoogle.R;
import com.druid.mapgoogle.bean.GoogleClusterMarkerBean;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class GoogleClusterRenderView extends DefaultClusterRenderer<GoogleClusterMarkerBean> {
    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;
    private final ImageView mImageView;
    private final ImageView mClusterImageView;
    private final int mDimension;
    private Context context;

    public GoogleClusterRenderView(Context context, GoogleMap map, ClusterManager<GoogleClusterMarkerBean> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;

        mIconGenerator = new IconGenerator(context);
        mClusterIconGenerator = new IconGenerator(context);

        View multiProfile = View.inflate(context,R.layout.custom_cluster_view, null);
        mClusterIconGenerator.setContentView(multiProfile);
        mClusterImageView = multiProfile.findViewById(R.id.image);

        mImageView = new ImageView(context);
        mDimension = (int) context.getResources().getDimension(R.dimen.custom_profile_image);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        int padding = (int) context.getResources().getDimension(R.dimen.custom_profile_padding);
        mImageView.setPadding(padding, padding, padding, padding);
//        mIconGenerator.setContentView(mImageView);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull GoogleClusterMarkerBean googleMarker, MarkerOptions markerOptions) {
        // Draw a single GoogleMarker - show their profile photo and set the info window to show their name
        markerOptions
                .icon(getItemIcon(googleMarker))
              //  .anchor(0.5f, 0.5f)
                .title(googleMarker.getMarker().mTitle);
    }

    @Override
    protected void onClusterItemUpdated(@NonNull GoogleClusterMarkerBean googleMarker, Marker marker) {
        // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
//        marker.setIcon(getItemIcon(googleMarker));
//        marker.setAnchor(0.5f, 0.5f);
//        marker.setTitle(googleMarker.getMarker().mTitle);
    }

    /**
     * Get a descriptor for a single GoogleMarker (i.e., a marker outside a cluster) from their
     * profile photo to be used for a marker icon
     *
     * @param googleMarker to return an BitmapDescriptor for
     * @return the GoogleMarker's profile photo as a BitmapDescriptor
     */
    private BitmapDescriptor getItemIcon(GoogleClusterMarkerBean googleMarker) {
        /*mImageView.setImageResource(googleMarker.getMarker().icon);
        Bitmap icon = mIconGenerator.makeIcon();
        return BitmapDescriptorFactory.fromBitmap(icon);*/

        return BitmapDescriptorFactory.fromResource(googleMarker.getMarker().icon);
    }

    @Override
    protected void onBeforeClusterRendered(@NonNull Cluster<GoogleClusterMarkerBean> cluster, MarkerOptions markerOptions) {
        // Draw multiple people.
        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
        markerOptions.icon(getClusterIcon(cluster)).anchor(0.5f, 0.5f);
    }

    @Override
    protected void onClusterUpdated(@NonNull Cluster<GoogleClusterMarkerBean> cluster, Marker marker) {
        // Same implementation as onBeforeClusterRendered() (to update cached markers)
        marker.setIcon(getClusterIcon(cluster));
        marker.setAnchor(0.5f, 0.5f);
    }

    /**
     * Get a descriptor for multiple people (a cluster) to be used for a marker icon. Note: this
     * method runs on the UI thread. Don't spend too much time in here (like in this example).
     *
     * @param cluster cluster to draw a BitmapDescriptor for
     * @return a BitmapDescriptor representing a cluster
     */
    private BitmapDescriptor getClusterIcon(Cluster<GoogleClusterMarkerBean> cluster) {
       /* List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));
        int width = mDimension;
        int height = mDimension;

        for (GoogleMarker p : cluster.getItems()) {
            // Draw 4 at most.
            if (profilePhotos.size() == 4) break;
            Drawable drawable = context.getResources().getDrawable(p.getMarker().icon);
            drawable.setBounds(0, 0, width, height);
            profilePhotos.add(drawable);
        }
        MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
        multiDrawable.setBounds(0, 0, width, height);*/

        mClusterImageView.setImageDrawable(context.getDrawable(R.drawable.cluster_marker_default));
        mClusterIconGenerator.setBackground(null);
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        return BitmapDescriptorFactory.fromBitmap(icon);

//        return BitmapDescriptorFactory.fromResource(R.drawable.cluster_marker_default);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }
}
