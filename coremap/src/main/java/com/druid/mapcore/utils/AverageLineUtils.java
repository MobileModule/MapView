package com.druid.mapcore.utils;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.utils.turf.TurfConstantsUtils;
import com.druid.mapcore.utils.turf.TurfMeasurementUtils;

import java.util.ArrayList;
import java.util.List;

public class AverageLineUtils {
    public static ArrayList<LatLngBean> getArrowLine(ArrayList<LatLngBean> route){
        return resetRouteSource(route,20, TurfConstantsUtils.UNIT_KILOMETERS);
    }

    public static ArrayList<LatLngBean> resetRouteSource(ArrayList<LatLngBean> route, int nstep, String units) {
        ArrayList<LatLngBean> newroute = new ArrayList<>();
        double lineDistance = TurfMeasurementUtils.length(route, units);
        double nDistance = lineDistance / nstep;
        for (int i = 0; i < route.size() - 1; i++) {
            LatLngBean from = route.get(i);
            LatLngBean to = route.get(i + 1);
            double lDistance = TurfMeasurementUtils.distance(from, to, units);
            if (i == 0) {
                newroute.add(route.get(0));
            }
            if (lDistance > nDistance) {
                List<LatLngBean> rings = lineMore(from, to, lDistance, nDistance, units);
                newroute.addAll(rings);
            } else {
                newroute.add(route.get(i + 1));
            }
        }
        return newroute;
    }

    public static ArrayList<LatLngBean> lineMore(LatLngBean from, LatLngBean to, double distance, double splitLength, String units) {
        ArrayList<LatLngBean> route = new ArrayList<>();
        route.add(from);
        route.add(to);
        //
        int step = (int) (distance / splitLength);
        double leftLength = distance - step * splitLength;
        ArrayList<LatLngBean> rings = new ArrayList<>();
        for (int i = 1; i <= step; i++) {
            double nlength = i * splitLength;
            LatLngBean pnt = TurfMeasurementUtils.along(route, nlength, units);
            rings.add(pnt);
        }
        if (leftLength > 0) {
            rings.add(to);
        }
        return rings;
    }
}
