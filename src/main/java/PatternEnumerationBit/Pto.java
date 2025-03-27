package PatternEnumerationBit;

import ClusteringGRindex.GridObject;

import java.util.*;

public class Pto {
    private Set<GridObject> objects;
    private long time;
    private long id;
    private Set<Long> objectsID;


    public Pto(long time, Set<GridObject> objects) {
        this.objects = objects;
        this.time = time;
        this.id = findMinId(objects); // 获取最小的ID
        this.objectsID = processObjectsID(objects);
    }

    public Pto(long time, Set<Long> objectsID, long oid, boolean isID) {
        this.objects = null;
        this.time = time;
        this.id = oid;
        this.objectsID = objectsID;
    }

    // 辅助方法，用于查找最小的ID
    private long findMinId(Set<GridObject> objects) {
        return objects.stream()
                .mapToLong(gridobject->gridobject.getPoint().getOid()) // 假设 GridObject 有一个 getId() 方法用于获取ID
                .min()
                .orElse(-1); // 如果集合为空，返回一个默认值
    }


    public long getId() {
        return id;
    }

    public void add(GridObject object) {
        objects.add(object);
    }

    public void addAll(Set<GridObject> objects) {
        this.objects.addAll(objects);
    }

    public Set<GridObject> getObjects() {
        return objects;
    }

    public Set<Long> processObjectsID(Set<GridObject> objects) {
        Set<Long> objectsID = new HashSet<>();
        for (GridObject object : objects) {
            objectsID.add((long) object.getPoint().getOid());
        }
        return objectsID;
    }

    public void removeObjectID(Long objectID) {
        objectsID.remove(objectID);
    }

    public Set<Long> getObjectsID() {
        return objectsID;
    }

    public int size() {
        return objects.size();
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "GridCluster [time=" + time + ", objects=" + objects + "]";
    }
}
