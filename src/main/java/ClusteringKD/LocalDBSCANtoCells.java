package ClusteringKD;

import Model.Point;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalDBSCANtoCells implements FlatMapFunction<Partition, Set<Cluster>> {

    private final double epsilon;
    private final int minPts;

    public LocalDBSCANtoCells(double eps, int minPts) {
        this.epsilon = eps;
        this.minPts = minPts;
    }
   
    @Override
    public void flatMap(Partition partition, Collector<Set<Cluster>> out) throws Exception {
        long start = System.currentTimeMillis();
        LocalDBSCAN localDBSCAN = new LocalDBSCAN(partition, minPts, epsilon);
        localDBSCAN.Clustering(partition.getPtID());
        Set<Cluster> localClusters = new HashSet<>(localDBSCAN.getClusters());
//        System.out.println("当前本地簇数量" + localClusters.size());
        out.collect(localClusters);
        long end = System.currentTimeMillis();
//        System.out.println(partition.hashCode() +  " " + "本地聚类时间：" + (end - start) + "ms");
    }
}
