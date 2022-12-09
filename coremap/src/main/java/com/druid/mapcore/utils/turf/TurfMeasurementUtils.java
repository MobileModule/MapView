package com.druid.mapcore.utils.turf;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

import com.druid.mapcore.bean.LatLngBean;

import java.util.List;

public class TurfMeasurementUtils {
    @NonNull
    public static LatLngBean destination(@NonNull LatLngBean point, @FloatRange(from = 0) double distance,
                                         @FloatRange(from = -180, to = 180) double bearing,
                                         @NonNull @TurfConstantsUtils.TurfUnitCriteria String units) {

        double longitude1 = degreesToRadians(point.getLng());
        double latitude1 = degreesToRadians(point.getLat());
        double bearingRad = degreesToRadians(bearing);

        double radians = TurfConversionUtils.lengthToRadians(distance, units);

        double latitude2 = Math.asin(Math.sin(latitude1) * Math.cos(radians)
                + Math.cos(latitude1) * Math.sin(radians) * Math.cos(bearingRad));
        double longitude2 = longitude1 + Math.atan2(Math.sin(bearingRad)
                        * Math.sin(radians) * Math.cos(latitude1),
                Math.cos(radians) - Math.sin(latitude1) * Math.sin(latitude2));

        return new LatLngBean(point.point_location,radiansToDegrees(latitude2),radiansToDegrees(longitude2));
    }

    public static double degreesToRadians(double degrees) {
        double radians = degrees % 360;
        return radians * Math.PI / 180;
    }

    /**
     * Converts an angle in radians to degrees.
     *
     * @param radians angle in radians
     * @return degrees between 0 and 360 degrees
     * @since 3.0.0
     */
    public static double radiansToDegrees(double radians) {
        double degrees = radians % (2 * Math.PI);
        return degrees * 180 / Math.PI;
    }

    public static LatLngBean along(@NonNull List<LatLngBean> coords, @FloatRange(from = 0) double distance,
                              @NonNull @TurfConstantsUtils.TurfUnitCriteria String units) {

        double travelled = 0;
        for (int i = 0; i < coords.size(); i++) {
            if (distance >= travelled && i == coords.size() - 1) {
                break;
            } else if (travelled >= distance) {
                double overshot = distance - travelled;
                if (overshot == 0) {
                    return coords.get(i);
                } else {
                    double direction = bearing(coords.get(i), coords.get(i - 1)) - 180;
                    return destination(coords.get(i), overshot, direction, units);
                }
            } else {
                travelled += distance(coords.get(i), coords.get(i + 1), units);
            }
        }

        return coords.get(coords.size() - 1);
    }

    public static double bearing(@NonNull LatLngBean point1, @NonNull LatLngBean point2) {

        double lon1 = degreesToRadians(point1.getLng());
        double lon2 = degreesToRadians(point2.getLng());
        double lat1 = degreesToRadians(point1.getLat());
        double lat2 = degreesToRadians(point2.getLat());
        double value1 = Math.sin(lon2 - lon1) * Math.cos(lat2);
        double value2 = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(lon2 - lon1);

        return radiansToDegrees(Math.atan2(value1, value2));
    }

    public static double distance(@NonNull LatLngBean point1, @NonNull LatLngBean point2,
                                  @NonNull @TurfConstantsUtils.TurfUnitCriteria String units) {
        double difLat = degreesToRadians((point2.getLat() - point1.getLat()));
        double difLon = degreesToRadians((point2.getLng() - point1.getLng()));
        double lat1 = degreesToRadians(point1.getLat());
        double lat2 = degreesToRadians(point2.getLat());

        double value = Math.pow(Math.sin(difLat / 2), 2)
                + Math.pow(Math.sin(difLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);

        return TurfConversionUtils.radiansToLength(
                2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value)), units);
    }

    public static double length(List<LatLngBean> coords, String units) {
        double travelled = 0;
        LatLngBean prevCoords = coords.get(0);
        LatLngBean curCoords;
        for (int i = 1; i < coords.size(); i++) {
            curCoords = coords.get(i);
            travelled += distance(prevCoords, curCoords, units);
            prevCoords = curCoords;
        }
        return travelled;
    }
}
