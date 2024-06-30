package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *  created by Frankie on 8/28/2023
 *  description:该类用于存储每个时间戳的快照信息
 */

public class SnapShot implements Serializable {
    private static final long serialVersionUID = 4597954049135884174L;

    private int timestamp;
    private HashMap<Integer, Point> object_positions;

    public SnapShot(int ts) {
        timestamp = ts;
        object_positions = new HashMap<Integer, Point>();
    }

    public void addObject(int oid, Point tp) {
        // You might want to handle the case when the same oid is added again
        object_positions.put(oid, tp);
    }

    public Set<Integer> getObjects() {
        return object_positions.keySet();
    }

    public List<Point> getPoints() {
        return new ArrayList<>(object_positions.values());
    }

    public int getTS() {
        return timestamp;
    }

    /**
     * each snapshot has a unique timestamp
     */
    @Override
    public boolean equals(Object snapshot2) {
        if (snapshot2 instanceof SnapShot) {
            return ((SnapShot) snapshot2).timestamp == this.timestamp;
        }
        return false;
    }

    public void mergeWith(SnapShot sp) {
        if (sp.timestamp != timestamp) {
            // You can throw a specific exception here
            System.err.println("[ERR]: cannot merge Snapshots with different timestamps, expect "
                    + timestamp + " , merged with " + sp.timestamp);
            throw new IllegalArgumentException("Cannot merge Snapshots with different timestamps");
        } else {
            object_positions.putAll(sp.object_positions);
        }
    }

    @Override
    public String toString() {
        return "<" + timestamp + ">: " + object_positions.keySet();
    }

    public Point getPoint(int obj) {
        return object_positions.get(obj);
    }

    @Override
    public SnapShot clone() {
        SnapShot nss = new SnapShot(timestamp);
        for (int key : object_positions.keySet()) {
            nss.addObject(key, object_positions.get(key)); // Make sure to clone Point
        }
        return nss;
    }
}
