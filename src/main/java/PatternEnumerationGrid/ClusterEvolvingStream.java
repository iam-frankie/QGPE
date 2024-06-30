package PatternEnumerationGrid;

import ClusteringKD.Cluster;

import java.util.*;

public class ClusterEvolvingStream {
    private Map<Long, Cluster> clusterMap;
    private long lastUpdateTime; // The timestamp of the last update

    public ClusterEvolvingStream() {
        this.clusterMap = new HashMap<>();
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void addCluster(long timestamp, Cluster cluster) {
        this.clusterMap.put(timestamp, cluster);
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public Map<Long, Cluster> getClusterMap() {
        return this.clusterMap;
    }

    public void setClusterMap(Map<Long, Cluster> clusterMap) {
        this.clusterMap = clusterMap;
    }

    public void addCluster(Long timestamp, Cluster cluster) {
        this.clusterMap.put(timestamp, cluster);
    }
}