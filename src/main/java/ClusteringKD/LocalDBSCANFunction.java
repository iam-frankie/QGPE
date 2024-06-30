package ClusteringKD;
import Model.Point;
import org.apache.flink.api.common.functions.MapFunction;


import java.util.*;

//public class LocalDBSCANFunction extends RichFlatMapFunction<List<List<Point>>, Set<Set<Point>>> {
//    @Override
//    public void flatMap(List<List<Point>> dataset, Collector<Set<Set<Point>>> collector) throws Exception {
////        List<Point> dataset = dataset; // 从 subspaces 中获取子空间对应的数据点
////        List<Point> pivotSet = null; // 从 subspaces 中获取 pivotSet（如果需要的话）
//        LocalDBSCAN localDBSCAN = new LocalDBSCAN(dataset);
//        // 在这里调用 LocalDBSCAN.divideIntoSubspaces 方法进行局部聚类
//        localDBSCAN.Clustering();
//
//        // 将局部聚类结果发送到下一个阶段
//        collector.collect(localDBSCAN.getClusters());
//    }
//}

//// 实现局部聚类的FlatMapFunction
//public class LocalDBSCANFunction2 implements FlatMapFunction<List<List<Point>>, Set<Set<Point>>> {
//    @Override
//    public void flatMap(List<List<Point>> partition, Collector<Set<Point>> out) {
//        // 使用LocalDBSCAN进行局部聚类并输出所有局部聚类结果
//        LocalDBSCAN localDBSCAN = new LocalDBSCAN(partition);
//        localDBSCAN.Clustering();
//        Set<Set<Point>> localClusters = localDBSCAN.getClusters();
//        for (Set<Point> localCluster : localClusters) {
//            out.collect(localCluster);
//        }
//    }
//}

// LocalDBSCANFunction 类的实现
public class LocalDBSCANFunction implements MapFunction<List<Partition>, Set<Cluster>> {

    private final double epsilon;
    private final int minPts;

    public LocalDBSCANFunction(double epsilon, int minPts) {
        this.epsilon = epsilon;
        this.minPts = minPts;
    }
    
    @Override
    public Set<Cluster> map(List<Partition> partitionedDatas) throws Exception {
        long start = System.currentTimeMillis();

        Set<Cluster> localClusters = new HashSet<>();

        for (Partition partition : partitionedDatas) {
            // 执行 LocalDBSCAN 算法，将每个分区的数据集 dataset 转换为局部聚类结果，并添加到 localClusters 中
            // ...
            LocalDBSCAN localDBSCAN = new LocalDBSCAN(partition, minPts, epsilon);
            localDBSCAN.Clustering(partition.getPtID());
//            System.out.println("当前本地簇数量" + localDBSCAN.getClusters().size());
            localClusters.addAll(localDBSCAN.getClusters());
        }
        long end = System.currentTimeMillis();
        System.out.println("合并前总簇数量" + localClusters.size());
        System.out.println("Clustering execution time: " + (end-start) + " milliseconds");
        return localClusters;
    }
}