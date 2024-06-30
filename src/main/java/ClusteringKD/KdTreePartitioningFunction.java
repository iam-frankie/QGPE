package ClusteringKD;

import Model.Point;
import Model.SnapShot;
import org.apache.flink.api.common.functions.RichMapFunction;

import java.util.List;

public class KdTreePartitioningFunction extends RichMapFunction<SnapShot, List<Partition>> {
    private int partitionNum;
    private double epsilon;
    private double conversionFactor;
    private int isBeijing;

    public KdTreePartitioningFunction(int partitionNum, double eps, double conversionFactor, int isBeijing) {
        this.partitionNum = partitionNum;
        this.epsilon = eps;
        this.conversionFactor = conversionFactor;
        this.isBeijing = isBeijing;
    }

    @Override
    public List<Partition> map(SnapShot snapshot) throws Exception {
        // 在这里调用 KdTreePartitioning.divideIntoSubspaces 方法进行全局分区

        long start = System.currentTimeMillis();
        KdTreePartitioning kdTreePartitioning = new KdTreePartitioning(epsilon, conversionFactor, snapshot.getTS());
        List<Partition> partitions = kdTreePartitioning.divideIntoSubspaces(snapshot.getPoints(), null, partitionNum, snapshot.getTS(), isBeijing);

        long end = System.currentTimeMillis();
        System.out.println("Partitoning execution time: " + (end-start) + " milliseconds");

        return partitions;
    }
}