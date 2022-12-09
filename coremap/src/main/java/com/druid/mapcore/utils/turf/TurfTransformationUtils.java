package com.druid.mapcore.utils.turf;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.druid.mapcore.bean.LatLngBean;

import java.util.ArrayList;
import java.util.List;

public class TurfTransformationUtils {
    private static final int DEFAULT_STEPS = 64;

    private TurfTransformationUtils() {
        // Empty constructor to prevent class initialization
    }

    private List<List<LatLngBean>> getCircleGeometry(LatLngBean point, double radius) {
        List<List<LatLngBean>> polygon = TurfTransformationUtils.circle(point, radius, TurfConstantsUtils.UNIT_METERS);
        return polygon;
    }

    public static List<List<LatLngBean>> circle(@NonNull LatLngBean center, double radius,
                                 @TurfConstantsUtils.TurfUnitCriteria String units) {
        return circle(center, radius, DEFAULT_STEPS, units);
    }

    public static List<List<LatLngBean>>  circle(@NonNull LatLngBean center, double radius, @IntRange(from = 1) int steps,
                                 @TurfConstantsUtils.TurfUnitCriteria String units) {
        List<LatLngBean> coordinates = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            coordinates.add(TurfMeasurementUtils.destination(center, radius, i * 360d / steps, units));
        }

        if (coordinates.size() > 0) {
            coordinates.add(coordinates.get(0));
        }
        List<List<LatLngBean>> coordinate = new ArrayList<>();
        coordinate.add(coordinates);
        return coordinate;
    }
}
