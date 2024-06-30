package Model;
import ClusteringGRindex.Clustering;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *  * Created by Frankie on 2023/8/28.
 *  定义一个点的类，用于存储经纬度
 **/


public class Point implements Serializable {
    private static final long serialVersionUID = 101161731896673554L;
    private int oid = -1;
    private final double X;
    private final double Y;
    private final double rawLat;
    private final double rawLon;
    private String direction;
    private double speed;
    private final int hashcode;

    private static final double Min_Dif = 1e-7; // 1e-7 = 0.0000001
    private int clusterID = -1;



    private boolean isCore = false;
    private boolean isBorder = false;
    private HashSet<Integer> neighbors;
    private double pivotDistance = 0.0;

    private boolean visited = false;
    private int pointStatus = 0; // 0: unclassified, -1: noise, 1: part of a cluster



    public Point(int id, double X, double Y, double rawLon, double rawLat) {
        this.oid = id;
        this.X = X;     // 经度转换
        this.Y = Y;     // 纬度转换
        this.rawLat = rawLat;   // 纬度
        this.rawLon = rawLon;   // 经度
        int result = 1;
        long latBits = Double.doubleToLongBits(X);
        result = 31 * result + (int) (latBits ^ (latBits >>> 32));
        long lontBits = Double.doubleToLongBits(Y);
        result = 31 * result + (int) (lontBits ^ (lontBits >>> 32));
        hashcode = result;
        this.neighbors = new HashSet<Integer>();
    }

    public int getOid() {
        return oid;
    }


    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    public double getRawLat() {
        return rawLat;
    }

    public double getRawLon() {
        return rawLon;
    }

    public double getSpeed() {
    	return speed;
    }

    public void setSpeed(double speed) {
    	this.speed = speed;
    }

    public String getDirection() {
    	return direction;
    }

    public void setDirection(String direction) {
    	this.direction = direction;
    }


    public void addNeighbor(int neighbor) {
        this.neighbors.add(neighbor);
    }

    public void addNeighbor(Point neighbor) {
        this.neighbors.add(neighbor.getOid());
    }

    public void addNeighbors(Set<Integer> neighbors) {
        this.neighbors.addAll(neighbors);
    }

    public void addNeighbors(Set<Point> neighbors, boolean isPoint) {
        for(Point neighbor: neighbors) {
            this.neighbors.add(neighbor.getOid());
        }
    }

    public HashSet<Integer> getNeighbors() {
        return this.neighbors;
    }

    // getCoordinates  返回经纬度
    public double[] getCoordinates() {
        return new double[]{getX(), getY()};
    }


    /**
     * 比较两个点是否相同
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof Point) {
            Point other = (Point) obj;
            return Math.abs(other.getX() - X) < Min_Dif && Math.abs(other.getY() - Y) < Min_Dif;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    public void setCore(boolean isCore) {
    	this.isCore = isCore;
    }

    public boolean getCore() {
    	return isCore;
    }

    public void setBorder(boolean isBorder) {
    	this.isBorder = isBorder;
    }

    public boolean getBorder() {
    	return isBorder;
    }



    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public int getPointStatus() {
        return pointStatus;
    }

    public void setPointStatus(int pointStatus) {
        this.pointStatus = pointStatus;
    }



    public void setPivotDistance(double pivotDistance) {
        this.pivotDistance = pivotDistance;
    }

    public double getPivotDistance() {
        return pivotDistance;
    }


}
