package com.druid.mapcore.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FencePolygonPointsBean implements Serializable {
    private TreeMap<Integer, LatLngBean> points = new TreeMap<>();

    public void addFencePoints(List<LatLngBean> source) {
        for (int i = 0; i < source.size(); i++) {
            points.put(i, source.get(i));
        }
    }

    public int size() {
        return points.size();
    }

    public LatLngBean getFencePoint(int index) {
        if (points.containsKey(index)) {
            return points.get(index);
        }
        return null;
    }

    public void addFencePoint(LatLngBean point) {
        points.put(points.size(), point);
    }

    public boolean addFencePoint(int index, LatLngBean point) {
        if (points.containsKey(index)) {
            return false;
        } else {
            points.put(index, point);
            return true;
        }
    }

    public LatLngBean removeFencePoint(int index) {
        if (points.containsKey(index)) {
            return points.remove(index);
        }
        return null;
    }

    public void clearFencePoints() {
        points.clear();
    }

    public List<LatLngBean> getFencePointList() {
        List<LatLngBean> pointList = new ArrayList<>();
        Iterator<TreeMap.Entry<Integer, LatLngBean>> it = points.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, LatLngBean> entry = it.next();
            pointList.add(entry.getValue());
        }
        return pointList;
    }

    public List<LatLngBean> getFencePointList(int index, LatLngBean latLng) {
        List<LatLngBean> pointList = getFencePointList();
        if (index != -1) {
            pointList.add(index, latLng);
        }
        return pointList;
    }

    public TreeMap<Integer, LatLngBean> getFencePointMap() {
        return points;
    }

    public TreeMap<Integer, LatLngBean> getFencePointMapClone(int index, LatLngBean latLng) {
        TreeMap<Integer, LatLngBean> pointsClone = (TreeMap<Integer, LatLngBean>) points.clone();
        if (index != -1) {
            pointsClone.put(index, latLng);
        }
        return pointsClone;
    }
}
