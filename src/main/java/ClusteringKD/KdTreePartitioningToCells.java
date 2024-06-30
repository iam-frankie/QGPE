package ClusteringKD;

import Model.Point;
import Model.SnapShot;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.List;

public class KdTreePartitioningToCells implements FlatMapFunction<SnapShot, Partition> {
    private int partitionNum;
    private double eps;
    private double convertionFactor = 1;
    private int isBeijing = 0;

    public KdTreePartitioningToCells(int partitionNum, double epsilon, double convertionFactor, int isBeijing) {
        this.partitionNum = partitionNum;
        this.eps = epsilon;
        this.convertionFactor = convertionFactor;
        this.isBeijing = isBeijing;
    }


    @Override
    public void flatMap(SnapShot snapshot, Collector<Partition> out) throws Exception {
        long start = System.currentTimeMillis();
        KdTreePartitioning kdTreePartitioning = new KdTreePartitioning(eps, convertionFactor, snapshot.getTS());
        System.out.println("该快照的点数"+snapshot.getObjects().size());
        List<Partition> partitions = kdTreePartitioning.divideIntoSubspaces(snapshot.getPoints(), null, partitionNum, snapshot.getTS(), isBeijing);
        long end = System.currentTimeMillis();
//        System.out.println("Partitoning execution time: " + (end-start) + " milliseconds");

        int count = 0;
        for(Partition partition: partitions) {
//            System.out.println("Partition: " + partition.getPtID() + " has " + partition.getPoints().size() + " points");
            count+=partition.getPoints().size();
            out.collect(partition);

        }
//        System.out.println("总点数"+count);
    }
}