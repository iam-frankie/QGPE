package Tools;


import static Conf.Constants.*;
import Model.Point;

/**
 *  created by Frankie on 2023/8/31
 *
 **/

public class DistanceOracle {
    /**
     *
     * given the two point's coordinate in latitude and longitude, we wish to
     * find the distance in meters. Here we do not need to use ``haversine''
     * formula because the points are near, and they can be viewed as a triangle.
     * @param p1 first point
     * @param p2 second point
     * @return distance in meters
     * 给定两点的经纬度坐标，我们希望求出以米为单位的距离。
     * 这里我们不需要使用“haversine”公式，因为这些点很近，它们可以被看作一个三角形。
     */

    // 地球半径，单位为米
    private static final double EARTH_RADIUS_KM = 6371000.0;

    // 将角度从度数转换为弧度
    private static double toRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }

    // 计算两个经纬度坐标点之间的 Haversine 距离
    public static double compEarthDistance(Point p1, Point p2) {
        if(p1 == null || p2 == null) {
            return Double.MAX_VALUE;
        }
        double lat1 = p1.getX();
        double lat2 = p2.getX();
        double lon1 = p1.getY();
        double lon2 = p2.getY();

        // 将经纬度坐标从度数转换为弧度
        lat1 = toRadians(lat1);
        lon1 = toRadians(lon1);
        lat2 = toRadians(lat2);
        lon2 = toRadians(lon2);

        // Haversine 公式
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离
        double distance = EARTH_RADIUS_KM * c;

        return distance;
    }


    // 计算两个经纬度坐标点之间的 Haversine 距离
    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将经纬度坐标从度数转换为弧度
        lat1 = toRadians(lat1);
        lon1 = toRadians(lon1);
        lat2 = toRadians(lat2);
        lon2 = toRadians(lon2);

        // Haversine 公式
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离
        double distance = EARTH_RADIUS_KM * c;

        return distance;
    }

    // 一个是地球距离（以经纬度为单位的距离）,因为地球的形状不是完美的球体，地球两点之间的最短路径是一条圆弧线
    public static double compEarthDistance2(Point p1, Point p2) {
        if(p1 == null || p2 == null) {
            return Double.MAX_VALUE;
        }
        double lat1 = p1.getX();
        double lat2 = p2.getX();
        double lont1 = p1.getY();
        double lont2 = p2.getY();

        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);

        double delta_phi = Math.toRadians(lat2-lat1);
        double delta_lambda = Math.toRadians(lont2-lont1);

        double a = Math.pow(Math.sin(delta_phi)/2,2) +
                Math.cos(phi1)*Math.cos(phi2) * Math.pow(Math.sin(delta_lambda/2),2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        System.out.println(c * EARTH_RADIUS * 1000);
        return c * EARTH_RADIUS * 1000;
    }

    // 欧氏距离（直线距离），这个距离是在二维平面上的两点之间的直线距离，精确度相比地球距离要低
    public static double compEuclidianDistance(Point p1, Point p2) {
        if(p1 == null || p2 == null) {
            return Double.MAX_VALUE;
        }
        double x1 = p1.getX(), y1 = p1.getY();
        double x2 = p2.getX(), y2 = p2.getY();
        double xdiff = Math.floor(x2 - x1);
        double ydiff = Math.floor(y2 - y1);
        return Math.floor(Math.sqrt(xdiff * xdiff + ydiff * ydiff)); //to meters;
    }
}