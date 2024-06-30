package PatternEnumerationGrid;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class SubMap{
    private Map<Integer, TreeSet<Long>> subMap;
    private long timestamp;

    public SubMap(Map<Integer, TreeSet<Long>> subMap, long timestamp) {
        this.subMap = subMap;
        this.timestamp = timestamp;
    }

    public Map<Integer, TreeSet<Long>> getSubMap() {
        return subMap;
    }

    public long getTimestamp() {
        return timestamp;
    }
}