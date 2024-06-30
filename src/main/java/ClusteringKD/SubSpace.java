package ClusteringKD;

import java.util.Arrays;

public class SubSpace {
    public double[] minBounds; // 最小边界坐标
    public double[] maxBounds; // 最大边界坐标
    public int pointCount; // 该子空间的点数

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    double time; // 该子空间的时间
    public String divloc; // 该子空间的划分位置

    public SubSpace(double[] minBounds, double[] maxBounds, int time) {
        this.time = time;
        this.minBounds = minBounds;
        this.maxBounds = maxBounds;
        this.pointCount = 0;
        divloc = "";
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
            this.pointCount = pointCount;
        }

    public String getBoundsString() {
        return Arrays.toString(minBounds) + Arrays.toString(maxBounds);
    }

    public String printSubSpace() {
        return " " + maxBounds[0] + " " + maxBounds[1] + " " + minBounds[0] + " " + minBounds[1];
    }

}