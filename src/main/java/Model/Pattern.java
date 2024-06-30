package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * representing trajectory patterns
 * created by Frankie on 2023/8/27.
 *
 */
public class Pattern<T> implements Serializable {
    /**
     * 每个类的 serialVersionUID 应该是唯一的，并且在不同版本的类之间应该保持不同。这是因为在 Java 序列化机制中，serialVersionUID 用于标识类的序列化版本，以确保在反序列化时类的版本一致性。
     * 如果两个不同的类具有相同的 serialVersionUID 值，那么 Java 在反序列化时可能会认为它们是同一个类，即使它们实际上可能具有不同的结构。这会导致严重的问题，因为反序列化可能无法正确地还原对象。
     */
    private static final long serialVersionUID = -2625008459728444860L;
    public HashSet<T> objectIdSet;
    private TreeSet<Integer> timestamps_set; // tss is thus ordered
    private int start, end; // the [start,end] interval of the given pattern

    /**
     * create a new placeholder for pattern which consists of two sets Object_IDs for
     * objects and TSS for time sequence
     */
    public Pattern() {
        initialize();
    }

    private void initialize() {
        objectIdSet = new HashSet<>();
        timestamps_set = new TreeSet<>();
        start = Integer.MAX_VALUE;
        end = Integer.MIN_VALUE;
    }

    /**
     * this constructor is used for test purpose
     * @param objects
     * @param timestamps
     *
     */
    public Pattern(T[] objects, int[] timestamps) {
        initialize();
        for (T i : objects) {
            objectIdSet.add(i);
        }
        for (int t : timestamps) {
            insertTime(t);
        }
    }

    public Pattern(Iterable<T> object_set, Iterable<Integer> timestamps) {
        initialize();
        for (T i : object_set) {
            objectIdSet.add(i);
        }
        for (int t : timestamps) {
            insertTime(t);
        }
    }

    /**
     * get the size of the pattern
     * @return the size of the pattern
     */
    public int getObjectSize() {
        return objectIdSet.size();
    }

    /**
     * get the size of the time sequence
     * @return the size of the time sequence
     */
    public int getTimeSize() {
        return timestamps_set.size();
    }

    /**
     * Insert an object into the pattern.
     * @param objectId The object to insert.
     */
    public void insertObject(T objectId) {
        objectIdSet.add(objectId);
    }

    /**
     * Insert a set of objects.
     * @param objects The objects to insert.
     */
    public void insertObjects(Set<T> objects) {
        objectIdSet.addAll(objects);
    }

    /**
     * insert a time stamp into the pattern
     * @param timestamp
     */
    public void insertTime(int timestamp) {
        if(start > timestamp) {
            start = timestamp;
        }
        if(end < timestamp) {
            end = timestamp;
        }
        timestamps_set.add(timestamp);
    }

    /**
     * get the start time of the pattern
     * @return
     */
    @Override
    public String toString() {
        return "["+start+","+ end + "]|<" + objectIdSet + ">,<" + timestamps_set + ">|";
    }



    /**
     * insert object set and time set into the pattern
     * @param objects
     * @param times
     */
    public void insertPattern(Set<T> objects, Iterable<Integer> times) {
        objectIdSet.addAll(objects);
        for (Integer t_sequence : times) {
            this.insertTime(t_sequence);
        }
    }

    /**
     * Get the object set of this pattern.
     * @return The object set.
     */
    public Set<T> getObjectSet() {
        return objectIdSet;
    }

    /**
     * using native method to get time set, and translate it into an ordered
     * ArrayList
     *
     * @return
     */
    public List<Integer> getTimeSet() {
        return new ArrayList<Integer>(timestamps_set);
    }

    /**
     * get the latest time stamp of this pattern
     * @return
     */
    public int getLatestTS() {
        assert timestamps_set.last() == end;
        return end;
    }


    /**
     * get the time stamp set
     */
    @Override
    public boolean equals(Object p) {
        if (p instanceof Pattern) {
            Pattern pp = (Pattern) p;
            return pp.objectIdSet.equals(objectIdSet);
        }
        return false;
    }

    /**
     * get the hash code of the pattern
     */
    @Override
    public int hashCode() {
        return objectIdSet.hashCode();
    }

    /**
     * get the start time stamp of the pattern
     * @return
     */
    public int getEarlyTS() {
        assert timestamps_set.first() == start;
        return start;
    }

}
