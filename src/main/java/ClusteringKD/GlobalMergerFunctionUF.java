//package ClusteringKD;
//
//import Model.Point;
//import org.apache.flink.api.common.functions.RichFlatMapFunction;
//import org.apache.flink.util.Collector;
//
//import java.util.Set;
//
//public class GlobalMergerFunctionUF extends RichFlatMapFunction<Set<Cluster>, Set<Cluster>> {
//
//    private int M;
//
//    public GlobalMergerFunctionUF(int M) {
//        this.M = M;
//    }
//
//    @Override
//    public void flatMap(Set<Cluster> localClusters, Collector<Set<Cluster>> collector) throws Exception {
//        long start = System.currentTimeMillis();
//
//        // 创建 GlobalMerger 实例
//        System.out.println("合并前的簇数量" + localClusters.size());
//        GlobalMergerUF UFglobalMerger = new GlobalMergerUF(localClusters, M);
//
//        Set<Point> mergingObjectCandidates = UFglobalMerger.getMergingObjectCandidates(localClusters);   //从 localClusters 中获取合并对象的候选集
//
//        System.out.println("合并对象的候选集大小为" + mergingObjectCandidates.size() + "个");
//
//        // 获取合并后的全局聚类结果
//
//        Set<Cluster> globalClusters = UFglobalMerger.mergeClusters(mergingObjectCandidates); // 根据您的 GlobalMerger 结果获取全局聚类结果
//        System.out.println("Snapshot" + globalClusters.iterator().next().getTimestamp() + "合并之后的簇数量" + globalClusters.size() + "个");
//        long end = System.currentTimeMillis();
//        //  System.out.println("Merging execution time: " + (end-start) + " milliseconds");
//
//        // 发送合并后的全局聚类结果到下一个阶段
//        collector.collect(globalClusters);
//
//
//    }
//}