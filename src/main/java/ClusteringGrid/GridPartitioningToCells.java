package ClusteringGrid;


import ClusteringKD.KdTreePartitioning;
import ClusteringKD.Partition;
import Model.SnapShot;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.List;

public class GridPartitioningToCells implements FlatMapFunction<SnapShot, Partition> {
    private int geoHashPrecision;
    private double eps;
    private double convertionFactor = 1;

    public GridPartitioningToCells(int geoHashPrecision, double epsilon, double convertionFactor) {
        this.geoHashPrecision = geoHashPrecision;
        this.eps = epsilon;
        this.convertionFactor = convertionFactor;
    }


    @Override
    public void flatMap(SnapShot snapshot, Collector<Partition> out) throws Exception {
        long start = System.currentTimeMillis();
        GridPartitioning gridPartitioning = new GridPartitioning(eps, convertionFactor, snapshot.getTS());
        List<Partition> partitions = gridPartitioning.divideIntoSubspacesQuadTree(snapshot.getPoints(), null, geoHashPrecision, snapshot.getTS());
        long end = System.currentTimeMillis();
//        System.out.println("Partitoning execution time: " + (end-start) + " milliseconds");

        int count = 0;
        for(Partition partition: partitions){
            count+=partition.getPoints().size();
//            System.out.println("分区的基本大小" + partition.getPoints().size());
//            System.out.println("分区的ID" + partition.getPtID());
            out.collect(partition);
        }
        System.out.println("总共的点数"+snapshot.getPoints().size()+"-复制的基本点数"+count+"-如果大小等于全局数据，说明没做边界点复制" + count);
    }

}