package Model;

import java.util.ArrayList;

/**
 * 确定的轨迹形式
 *
 * @author Frankie
 */

public class Trajectory extends ArrayList<TemporalPoint> {
    private static final long serialVersionUID = 6700721732576703598L;
    private static int IDS = 0;
    private final int objectId;
    private double x1 = Double.POSITIVE_INFINITY, y1 = Double.POSITIVE_INFINITY,
            x2 = Double.NEGATIVE_INFINITY, y2 = Double.NEGATIVE_INFINITY;

    /**
     * 创建一个空的轨迹
     */
    public Trajectory() {
        objectId = IDS++;
    }

    /**
     * 向当前轨迹中插入 TemporalPoint（按时间顺序）
     *
     * @param tp
     * @return
     */
    public boolean insertPoint(TemporalPoint tp) {
        if (size() == 0 || tp.getTime() > get(size() - 1).getTime()) {
            add(tp);
            updateBoundingBox(tp.getX(), tp.getY());
            return true;
        }
        return false;
    }

    @Override
    public boolean add(TemporalPoint tp) {
        boolean added = super.add(tp);
        if (added) {
            updateBoundingBox(tp.getX(), tp.getY());
        }
        return added;
    }

    private void updateBoundingBox(double latitude, double longitude) {
        if (latitude > x2) {
            x2 = latitude;
        } else if (latitude < x1) {
            x1 = latitude;
        }
        if (longitude > y2) {
            y2 = longitude;
        } else if (longitude < y1) {
            y1 = longitude;
        }
    }

    public double[] getBoundingBox() {
        return new double[]{x1, y1, x2, y2};
    }

    public int getObjectId() {
        return objectId;
    }

    /**
     * 获取当前轨迹长度
     *
     * @return
     */
    public int getLength() {
        return size();
    }

    @Override
    public String toString() {
        return objectId + "(" + size() + "):" + super.toString();
    }
}
