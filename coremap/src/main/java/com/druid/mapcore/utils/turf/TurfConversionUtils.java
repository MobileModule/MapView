package com.druid.mapcore.utils.turf;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class TurfConversionUtils {
    private static final Map<String, Double> FACTORS;

    static {
        FACTORS = new HashMap<>();
        FACTORS.put(TurfConstantsUtils.UNIT_MILES, 3960d);
        FACTORS.put(TurfConstantsUtils.UNIT_NAUTICAL_MILES, 3441.145d);
        FACTORS.put(TurfConstantsUtils.UNIT_DEGREES, 57.2957795d);
        FACTORS.put(TurfConstantsUtils.UNIT_RADIANS, 1d);
        FACTORS.put(TurfConstantsUtils.UNIT_INCHES, 250905600d);
        FACTORS.put(TurfConstantsUtils.UNIT_YARDS, 6969600d);
        FACTORS.put(TurfConstantsUtils.UNIT_METERS, 6373000d);
        FACTORS.put(TurfConstantsUtils.UNIT_CENTIMETERS, 6.373e+8d);
        FACTORS.put(TurfConstantsUtils.UNIT_KILOMETERS, 6373d);
        FACTORS.put(TurfConstantsUtils.UNIT_FEET, 20908792.65d);
        FACTORS.put(TurfConstantsUtils.UNIT_CENTIMETRES, 6.373e+8d);
        FACTORS.put(TurfConstantsUtils.UNIT_METRES, 6373000d);
        FACTORS.put(TurfConstantsUtils.UNIT_KILOMETRES, 6373d);
    }

    private TurfConversionUtils() {
        // Private constructor preventing initialization of this class
    }

    public static double lengthToRadians(double distance) {
        return lengthToRadians(distance, TurfConstantsUtils.UNIT_DEFAULT);
    }

    /**
     * Convert a distance measurement (assuming a spherical Earth) from a real-world unit into
     * radians.
     */
    public static double lengthToRadians(double distance, @NonNull @TurfConstantsUtils.TurfUnitCriteria String units) {
        return distance / FACTORS.get(units);
    }

    public static double radiansToLength(double radians, @NonNull @TurfConstantsUtils.TurfUnitCriteria String units) {
        return radians * FACTORS.get(units);
    }
}
