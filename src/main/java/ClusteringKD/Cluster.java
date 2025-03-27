package ClusteringKD;
import Model.Point;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/** description of class Cluster in package ClusteringKD
 *  Created by Frankie on 23.09.12
 */

public class Cluster {
    private Set<Point> points; // 点的集合
    private long timestamp;   // 时间戳信息
    private Set<Integer> pointIDs; // 点的ID集合
    private Set<Point> borderPoints; // 边界点集合


    private String clusterID; // 簇的ID
    private boolean isVisited = false; // 是否被访问过=>聚类阶段
    public List<String> ptIDs = new ArrayList<>(); // 簇中点的ID集合  =>分区挖掘阶段
    public boolean iaActivate = false; // 是否处于休眠状态, 默认为false, 激活后才会进行分区的挖掘

    public Cluster(Long ts) {
        this.points = new HashSet<>();
        this.timestamp = ts; // 默认为当前时间
        this.pointIDs = new HashSet<>();
        this.borderPoints = new HashSet<>();
    }

    public void setActivate(boolean activate){
        this.iaActivate = activate;
    }

    public boolean isActivate(){
        return iaActivate;
    }

    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(int clusterID) {
        this.clusterID = this.clusterID = "t-" + timestamp + "-c-" + clusterID;
    }

    public void setClusterIDKD(String clusterID) {
        this.clusterID = clusterID;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public Set<Point> getPoints() {
        return points;
    }

    public Set<Point> getBorderPoints() {
        return borderPoints;
    }

    public void setPoints(Set<Point> points) {
        this.points = points;
        for(Point p : points){
            pointIDs.add(p.getOid());
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Set<Integer> getPointIDs(){
        return pointIDs;
    }

    // 添加一个点到集合中
    public void addPoint(Point point) {
        if(!pointIDs.contains(point.getOid())) {
            points.add(point);
            pointIDs.add(point.getOid());
            if (point.getBorder())
                borderPoints.add(point);
        }
    }

//    public boolean contains(Point point) {
//        return points.contains(point);
//    }

    public boolean contains(Point point) {
        return pointIDs.contains(point.getOid());
    }

    public boolean contains(Cluster cluster) {
        return this.pointIDs.containsAll(cluster.getPointIDs());
    }



    // 从集合中移除一个点
    public void removePoint(Point point) {
        pointIDs.remove(point.getOid());
        points.remove(point);
    }

    public int size() {
        return pointIDs.size();
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "points=" + pointIDs.toString() +
                ", timestamp=" + timestamp +
                '}';
    }

//    public void PointClear() {
//
//    }


}
