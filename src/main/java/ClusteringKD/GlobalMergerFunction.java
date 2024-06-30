package ClusteringKD;

import Model.Point;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.Set;

public class GlobalMergerFunction extends RichFlatMapFunction<Set<Cluster>,Cluster> {

    private int M;
    private int isTest;

    public GlobalMergerFunction(int M, int isTest) {
        this.M = M;
        this.isTest = isTest;
    }

    @Override
    public void flatMap(Set<Cluster> localClusters, Collector<Cluster> collector) throws Exception {
//        long start = System.currentTimeMillis();

        // 创建 GlobalMerger 实例
        System.out.println("合并前的簇数量" + localClusters.size());
        if (localClusters.size() == 0) {
            return;
        }
        GlobalMerger globalMerger = new GlobalMerger(localClusters, M);
//        System.out.println("clusterMap大小为" + globalMerger.clusterMap.size() + "个");
        Set<Point> mergingObjectCandidates = globalMerger.getMergingObjectCandidates(localClusters);   //从 localClusters 中获取合并对象的候选集
        System.out.println("合并对象的候选集大小为" + mergingObjectCandidates.size() + "个");
        // 获取合并后的全局聚类结果
        Set<Cluster> globalClusters = globalMerger.mergeClusters(mergingObjectCandidates); // 根据您的 GlobalMerger 结果获取全局聚类结果
        if (globalClusters.size() == 0) {
            return;
        }
        System.out.println("Snapshot" + globalClusters.iterator().next().getTimestamp() + "合并之后的簇数量" + globalClusters.size() + "个");
//        long end = System.currentTimeMillis();
//        System.out.println("Merging execution time: " + (end-start) + " milliseconds");

        int twentyc = 0;
        for (Cluster cluster : globalClusters) {
            int clusterSize = cluster.getPoints().size(); // 获取簇的大小
            if(isTest==1) {
                if(clusterSize >= 20){
                    twentyc++;
                }
                System.out.println("ClusterID:" + cluster.getClusterID() + " Cluster Size: " + clusterSize);
                System.out.println(cluster.getPointIDs());
                System.out.println(cluster.ptIDs);
            }
            collector.collect(cluster); // 将簇发送到下游
        }
        if(isTest == 1)
            System.out.println("大于20的簇数量" + twentyc + "个");
//        // 发送合并后的全局聚类结果到下一个阶段
//        collector.collect(globalClusters);


    }
}