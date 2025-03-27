package ClusteringKD;
import Model.Point;
import ch.hsr.geohash.GeoHash;
import org.apache.kafka.common.protocol.types.Field;

import java.util.List;

public class Partition {
    private List<Point> points;  // 存储点的集合
    private long timestamp;     // 时间戳信息
    private String ptID;           // 分区的ID
    private GeoHash geoHash;     // 分区的GeoHash编码

    // 构造方法
    public Partition(List<Point> points, long timestamp, String ptID) {
        this.points = points;
        this.timestamp = timestamp;
        this.ptID = ptID;
    }

    // Getter方法
    public List<Point> getPoints() {
        return points;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setter方法
    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isEmpty() {
    	return points.isEmpty();
    }

    public int size() {
    	return points.size();
    }

    public void addPoint(Point point) {
    	points.add(point);
    }

    public void removePoint(Point point) {
    	points.remove(point);
    }

    public void clear() {
    	points.clear();
    }

    public Point getPoint(int index) {
    	return points.get(index);
    }

    public void setPoint(int index, Point point) {
    	points.set(index, point);
    }

    public void addAll(List<Point> points) {
    	this.points.addAll(points);
    }

    public void setPtID(String ptID) {
    	this.ptID = ptID;
    }

    public String getPtID() {
    	return ptID;
    }

    public void setGeoHash(GeoHash geoHash) {
    	this.geoHash = geoHash;
    }

    public GeoHash getGeoHash() {
    	return geoHash;
    }

}
