package com.druid.mapcore.utils;

import static com.druid.mapcore.utils.MathUtil.EARTH_RADIUS;
import static com.druid.mapcore.utils.MathUtil.arcHav;
import static com.druid.mapcore.utils.MathUtil.havDistance;
import static com.druid.mapcore.utils.MathUtil.wrap;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import com.druid.mapcore.bean.LatLngBean;

import java.util.ArrayList;
import java.util.List;

public class MapUtils {

    /**
     * Returns the heading from one LatLng to another LatLng. Headings are
     * expressed in degrees clockwise from North within the range [-180,180).
     *
     * @return The heading in degrees clockwise from north.
     */
    public static double computeHeading(LatLngBean from, LatLngBean to) {
        // http://williams.best.vwh.net/avform.htm#Crs
        double fromLat = toRadians(from.getLat());
        double fromLng = toRadians(from.getLng());
        double toLat = toRadians(to.getLat());
        double toLng = toRadians(to.getLng());
        double dLng = toLng - fromLng;
        double heading = atan2(
                sin(dLng) * cos(toLat),
                cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(dLng));
        return wrap(toDegrees(heading), -180, 180);
    }

    /**
     * Returns the LatLng resulting from moving a distance from an origin
     * in the specified heading (expressed in degrees clockwise from north).
     *
     * @param from     The LatLng from which to start.
     * @param distance The distance to travel.
     * @param heading  The heading in degrees clockwise from north.
     */
    public static LatLngBean computeOffset(LatLngBean from, double distance, double heading) {
        distance /= EARTH_RADIUS;
        heading = toRadians(heading);
        // http://williams.best.vwh.net/avform.htm#LL
        double fromLat = toRadians(from.getLat());
        double fromLng = toRadians(from.getLng());
        double cosDistance = cos(distance);
        double sinDistance = sin(distance);
        double sinFromLat = sin(fromLat);
        double cosFromLat = cos(fromLat);
        double sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * cos(heading);
        double dLng = atan2(
                sinDistance * cosFromLat * sin(heading),
                cosDistance - sinFromLat * sinLat);
        return new LatLngBean(from.point_location,toDegrees(asin(sinLat)), toDegrees(fromLng + dLng));
    }

    /**
     * Returns the location of origin when provided with a LatLng destination,
     * meters travelled and original heading. Headings are expressed in degrees
     * clockwise from North. This function returns null when no solution is
     * available.
     *
     * @param to       The destination LatLng.
     * @param distance The distance travelled, in meters.
     * @param heading  The heading in degrees clockwise from north.
     */
    public static LatLngBean computeOffsetOrigin(LatLngBean to, double distance, double heading) {
        heading = toRadians(heading);
        distance /= EARTH_RADIUS;
        // http://lists.maptools.org/pipermail/proj/2008-October/003939.html
        double n1 = cos(distance);
        double n2 = sin(distance) * cos(heading);
        double n3 = sin(distance) * sin(heading);
        double n4 = sin(toRadians(to.getLat()));
        // There are two solutions for b. b = n2 * n4 +/- sqrt(), one solution results
        // in the lat outside the [-90, 90] range. We first try one solution and
        // back off to the other if we are outside that range.
        double n12 = n1 * n1;
        double discriminant = n2 * n2 * n12 + n12 * n12 - n12 * n4 * n4;
        if (discriminant < 0) {
            // No real solution which would make sense in LatLng-space.
            return null;
        }
        double b = n2 * n4 + sqrt(discriminant);
        b /= n1 * n1 + n2 * n2;
        double a = (n4 - n2 * b) / n1;
        double fromLatRadians = atan2(a, b);
        if (fromLatRadians < -PI / 2 || fromLatRadians > PI / 2) {
            b = n2 * n4 - sqrt(discriminant);
            b /= n1 * n1 + n2 * n2;
            fromLatRadians = atan2(a, b);
        }
        if (fromLatRadians < -PI / 2 || fromLatRadians > PI / 2) {
            // No solution which would make sense in LatLng-space.
            return null;
        }
        double fromLngRadians = toRadians(to.getLng()) -
                atan2(n3, n1 * cos(fromLatRadians) - n2 * sin(fromLatRadians));
        return new LatLngBean(to.point_location,toDegrees(fromLatRadians), toDegrees(fromLngRadians));
    }

    /**
     * Returns the LatLng which lies the given fraction of the way between the
     * origin LatLng and the destination LatLng.
     *
     * @param from     The LatLng from which to start.
     * @param to       The LatLng toward which to travel.
     * @param fraction A fraction of the distance to travel.
     * @return The interpolated LatLng.
     */
    public static LatLngBean interpolate(LatLngBean from, LatLngBean to, double fraction) {
        // http://en.wikipedia.org/wiki/Slerp
        double fromLat = toRadians(from.getLat());
        double fromLng = toRadians(from.getLng());
        double toLat = toRadians(to.getLat());
        double toLng = toRadians(to.getLng());
        double cosFromLat = cos(fromLat);
        double cosToLat = cos(toLat);

        // Computes Spherical interpolation coefficients.
        double angle = computeAngleBetween(from, to);
        double sinAngle = sin(angle);
        if (sinAngle < 1E-6) {
            return from;
        }
        double a = sin((1 - fraction) * angle) / sinAngle;
        double b = sin(fraction * angle) / sinAngle;

        // Converts from polar to vector and interpolate.
        double x = a * cosFromLat * cos(fromLng) + b * cosToLat * cos(toLng);
        double y = a * cosFromLat * sin(fromLng) + b * cosToLat * sin(toLng);
        double z = a * sin(fromLat) + b * sin(toLat);

        // Converts interpolated vector back to polar.
        double lat = atan2(z, sqrt(x * x + y * y));
        double lng = atan2(y, x);
        return new LatLngBean(0,toDegrees(lat), toDegrees(lng));
    }

    /**
     * Returns distance on the unit sphere; the arguments are in radians.
     */
    private static double distanceRadians(double lat1, double lng1, double lat2, double lng2) {
        return arcHav(havDistance(lat1, lat2, lng1 - lng2));
    }

    /**
     * Returns the angle between two LatLngs, in radians. This is the same as the distance
     * on the unit sphere.
     */
    static double computeAngleBetween(LatLngBean from, LatLngBean to) {
        return distanceRadians(toRadians(from.getLat()), toRadians(from.getLng()),
                toRadians(to.getLat()), toRadians(to.getLng()));
    }

    /**
     * Returns the distance between two LatLngs, in meters.
     * m
     */
    public static double computeDistanceBetween(LatLngBean from, LatLngBean to) {
        return computeAngleBetween(from, to) * EARTH_RADIUS;
    }

    /**
     * Returns the length of the given path, in meters, on Earth.
     */
    public static double computeLength(List<LatLngBean> path) {
        if (path.size() < 2) {
            return 0;
        }
        double length = 0;
        LatLngBean prev = path.get(0);
        double prevLat = toRadians(prev.getLat());
        double prevLng = toRadians(prev.getLng());
        for (LatLngBean point : path) {
            double lat = toRadians(point.getLat());
            double lng = toRadians(point.getLng());
            length += distanceRadians(prevLat, prevLng, lat, lng);
            prevLat = lat;
            prevLng = lng;
        }
        return length * EARTH_RADIUS;
    }

    /**
     * Returns the area of a closed path on Earth.
     *
     * @param path A closed path.
     * @return The path's area in square meters.
     */
    public static double computeArea(List<LatLngBean> path) {
        return abs(computeSignedArea(path));
    }

    /**
     * Returns the signed area of a closed path on Earth. The sign of the area may be used to
     * determine the orientation of the path.
     * "inside" is the surface that does not contain the South Pole.
     *
     * @param path A closed path.
     * @return The loop's area in square meters.
     */
    public static double computeSignedArea(List<LatLngBean> path) {
        return computeSignedArea(path, EARTH_RADIUS);
    }

    /**
     * Returns the signed area of a closed path on a sphere of given radius.
     * The computed area uses the same units as the radius squared.
     * Used by SphericalUtilTest.
     */
    static double computeSignedArea(List<LatLngBean> path, double radius) {
        int size = path.size();
        if (size < 3) {
            return 0;
        }
        double total = 0;
        LatLngBean prev = path.get(size - 1);
        double prevTanLat = tan((PI / 2 - toRadians(prev.getLat())) / 2);
        double prevLng = toRadians(prev.getLng());
        // For each edge, accumulate the signed area of the triangle formed by the North Pole
        // and that edge ("polar triangle").
        for (LatLngBean point : path) {
            double tanLat = tan((PI / 2 - toRadians(point.getLat())) / 2);
            double lng = toRadians(point.getLng());
            total += polarTriangleArea(tanLat, lng, prevTanLat, prevLng);
            prevTanLat = tanLat;
            prevLng = lng;
        }
        return total * (radius * radius);
    }

    /**
     * Returns the signed area of a triangle which has North Pole as a vertex.
     * Formula derived from "Area of a spherical triangle given two edges and the included angle"
     * as per "Spherical Trigonometry" by Todhunter, page 71, section 103, point 2.
     * See http://books.google.com/books?id=3uBHAAAAIAAJ&pg=PA71
     * The arguments named "tan" are tan((pi/2 - lat)/2).
     */
    private static double polarTriangleArea(double tan1, double lng1, double tan2, double lng2) {
        double deltaLng = lng1 - lng2;
        double t = tan1 * tan2;
        return 2 * atan2(t * sin(deltaLng), 1 + t * cos(deltaLng));
    }

    /**
     * 获取凹不规则多边形重心点
     */
    public static LatLngBean getCenterOfGravityPoint(ArrayList<LatLngBean> mPoints) {
        double area = 0.0;//多边形面积
        double Gx = 0.0, Gy = 0.0;// 重心的x、y
        for (int i = 1; i <= mPoints.size(); i++) {
            double iLat = mPoints.get(i % mPoints.size()).getLat();
            double iLng = mPoints.get(i % mPoints.size()).getLng();
            double nextLat = mPoints.get(i - 1).getLat();
            double nextLng = mPoints.get(i - 1).getLng();
            double temp = (iLat * nextLng - iLng * nextLat) / 2.0;
            area += temp;
            Gx += temp * (iLat + nextLat) / 3.0;
            Gy += temp * (iLng + nextLng) / 3.0;
        }
        Gx = Gx / area;
        Gy = Gy / area;
        return new LatLngBean(0,Gx, Gy);
    }

    public static boolean validLatLng(double lat, double lng) {
        if (lat != -200 || lat != 0 || lat != 200) {//latitude must be between -90 and 90
            if (lat >= -90 && lat <= 90) {
                if (lng != -200 || lng != 0 || lng != 200) {//longitude must be between -180 and 180
                    if (lng >= -180 && lng <= 180) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static float getAngleY(double[] firstPoint, LatLngBean nextLatLng) {
        LatLngBean firstLatLng = new LatLngBean(0,firstPoint[0], firstPoint[1]);
        return -getAngleY(firstLatLng, nextLatLng)-180;
    }

    public static float getAngleY(LatLngBean firstPoint, LatLngBean nextPoint) {
        double angle = Math.atan2((nextPoint.getLng() - firstPoint.getLng()),
                (nextPoint.getLat() - firstPoint.getLat()));
        double theta = angle * (180 / Math.PI);
        return Float.parseFloat(theta + "");
    }

    /**
     * 已知两个点获取，中心点经纬度
     */
    public static double[] getCenterLatLng(LatLngBean latLng1, LatLngBean latLng2) {
        int total = 2;
        double X = 0;
        double Y = 0;
        double Z = 0;
        //
        double lat = latLng1.getLat() * Math.PI / 180;
        double lon = latLng1.getLng() * Math.PI / 180;
        X += Math.cos(lat) * Math.cos(lon);
        Y += Math.cos(lat) * Math.sin(lon);
        Z += Math.sin(lat);
        //
        lat = latLng2.getLat() * Math.PI / 180;
        lon = latLng2.getLng() * Math.PI / 180;
        X += Math.cos(lat) * Math.cos(lon);
        Y += Math.cos(lat) * Math.sin(lon);
        Z += Math.sin(lat);
        //
        X = X / total;
        Y = Y / total;
        Z = Z / total;
        double lon2 = Math.atan2(Y, X);
        double hyp = Math.sqrt(X * X + Y * Y);
        double lat2 = Math.atan2(Z, hyp);
        double lng = lon2 * 180 / Math.PI;
        double latitude = lat2 * 180 / Math.PI;
        return new double[]{latitude, lng};
    }
}
