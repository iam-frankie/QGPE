package PatternEnumerationBit;

import ClusteringGRindex.GridObject;
import org.mortbay.log.Log;

import java.util.*;

// 数据结构表示模式
public class Pattern {
    Set<GridObject> points;
    Long startTime;
    Long endTime;
    Set<Long> objectsID;
    TreeSet<Integer> timeSeries;
    // 新增的HashMap部分
    Map<Long, VariableBitSet> incrementalH;
    // 新增的Candidate部分
    Set<Long> incrementalC;



    /**
     * Pattern
     * 仅用于存储符合K、L、G时间上的约束要求的轨迹点，但点的存储形式根源为id
     * @param combination
//     * @param startTime
//     * @param endTime
     */
    public Pattern(Set<GridObject> combination) {
        this.points = combination;
        this.objectsID = processObjectsID(combination);
        this.timeSeries = new TreeSet<>();    // 用于记录连续的序列段
    }

    public Pattern(Set<Long> objectsID, boolean isID) {
        this.points = null;
        this.objectsID = objectsID;
        this.timeSeries = new TreeSet<>();
    }

//    public Pattern(Set<Integer> candidatePattern, boolean isID) {
//    }

    public void addPoint(GridObject point) {
        points.add(point);
    }

    public Set<GridObject> getPoints() {
        return points;
    }

    public void setPoints(Set<GridObject> points) {
        this.points = points;
    }

    public Set<Long> getObjectsID(){
        return this.objectsID;
    }

    public Set<Long> processObjectsID(Set<GridObject> points){
        Set<Long> res = new HashSet<>();
        for(GridObject point: points){
            res.add((long) point.getPoint().getOid());
        }
        return res;
    }

    public int size() {
        return points.size();
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }


    public int getEndTime() {
        return timeSeries.last();
    }

    public int getStartTime() {
        return timeSeries.first();
    }

//    public void setEndTime(Long iendTime) {
//        this.endTime = Math.max(iendTime, this.endTime);
//    }

//    public Integer setStartTime(Long startTime) {
//        return timeSeries.first();
//    }

    public int getDuration() {
        return timeSeries.last() - timeSeries.first();
    }

    public int getaccDuration(){
        return timeSeries.size();
    }

    public void addTimeSeries(int atime){
        timeSeries.add(atime);
    }

    public void addTimeSeries(List<Long> timeSeries){
        for(Long time: timeSeries){
            this.timeSeries.add(time.intValue());
        }
    }

    public int getTl() {
        if (timeSeries == null || timeSeries.size() < 2) {
            return 0;
        }

        Iterator<Integer> iterator = timeSeries.iterator();
        int prev = iterator.next();
        int current;
        int minSegmentLength = Integer.MAX_VALUE;
        int currentSegmentLength = 1;

        while (iterator.hasNext()) {
            current = iterator.next();
            if (current - prev == 1) {
                currentSegmentLength++;
            } else {
                minSegmentLength = Math.min(minSegmentLength, currentSegmentLength);
                currentSegmentLength = 1;
            }
            prev = current;
        }

        minSegmentLength = Math.min(minSegmentLength, currentSegmentLength);

        return minSegmentLength;
    }


    public void toStrings() {
        for (GridObject point : points) {
            System.out.print(point.toString() + " ");
        }
        System.out.println();
    }
}