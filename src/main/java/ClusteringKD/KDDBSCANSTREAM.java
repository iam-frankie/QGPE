package ClusteringKD;

import ClusteringGRindex.GridObject;
import Model.Point;
import Model.SnapShot;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KDDBSCANSTREAM {

    public DataStream<Cluster> KDDBSCAN_Launcher(DataStream<SnapShot> snapshotStream,
                                                 int partitionNum,
                                                 int M,
                                                 double eps,
                                                 int minPts,
                                                 double convertionFactor,
                                                 long windowSizeInMs,
                                                 int parallelism,
                                                 int isTest,
                                                 int isBeijing) {

        // todo 考虑后期动态的分析当前快照的分区数
/*
        // 创建分区计算器，设定基础分区大小为1000
        PartitionCalculator partitionCalculator = new PartitionCalculator(1000);

        // 使用分区计算器计算分区数
                DataStream<Integer> partitionNumStream = snapshotStream.map(partitionCalculator);

        // 获取分区数
                int partitionNum = partitionNumStream.executeAndCollect().iterator().next();
*/

        if(partitionNum%2!=0)
            partitionNum+=1;
        System.out.println("分区数" + partitionNum);
        System.out.println("并行数" + parallelism);
        long snapshotStartTime = System.currentTimeMillis();

        // 第一阶段 生成全局划分
        long stage1StartTime = System.currentTimeMillis();
        DataStream<Partition> globalPartition = snapshotStream
                .flatMap(new KdTreePartitioningToCells(partitionNum, eps, convertionFactor, isBeijing))
                .setParallelism(parallelism);
        long stage1EndTime = System.currentTimeMillis();
        long stage1ExecutionTime = stage1EndTime - stage1StartTime;
        System.out.println("第一阶段执行时间:" + stage1ExecutionTime + " ms");

        // 第二阶段 生成局部聚类
        long stage2StartTime = System.currentTimeMillis();
        DataStream<Set<Cluster>> localClustersStream = globalPartition
                .flatMap(new LocalDBSCANtoCells(eps, minPts))
                .setParallelism(parallelism)
                .filter(clusters->clusters.size()>0)
                .setParallelism(parallelism);
        long stage2EndTime = System.currentTimeMillis();
        long stage2ExecutionTime = stage2EndTime - stage2StartTime;
        System.out.println("第二阶段执行时间:" + stage2ExecutionTime + " ms");

        // 第三阶段(1) 合并局部聚类-DFS深搜图实现
        // 这一阶段耗时长，主要是因为分区比较耗时
        long stage3StartTime = System.currentTimeMillis();
        DataStream<Set<Cluster>> GlobalClustersStream = localClustersStream
                .keyBy(clusters -> clusters.iterator().next().getTimestamp())
                .timeWindow(Time.milliseconds(windowSizeInMs))
//                .windowAll(EventTimeSessionWindows.withGap(Time.milliseconds(windowSizeInMs)))
                .reduce((ReduceFunction<Set<Cluster>>) (value1, value2) -> {
//                    Set<Cluster> clusters = new HashSet<>();
                    value2.addAll(value1);
//                    System.out.println(value2.size());
                    return value2;
                })
                .setParallelism(parallelism)
                ;      // 零散度高的数据并行度适当低，减小数据
        long stage3MiddleTime = System.currentTimeMillis();

        DataStream<Cluster> clusterstream = GlobalClustersStream
                .flatMap(new GlobalMergerFunction(M, isTest))
                .setParallelism(parallelism);
        long stage3MiddleTime2 = System.currentTimeMillis();


        long stage3EndTime = System.currentTimeMillis();
        long stage3ExecutionTime = stage3EndTime - stage3StartTime;
        System.out.println("第三阶段执行时间:" + stage3ExecutionTime + " ms");

//        System.out.println("合并时间:" + (stage3EndTime-stage3MiddleTime2) + " ms");

        long snapshotEndTime = System.currentTimeMillis();
        long snapshotExecutionTime = snapshotEndTime - snapshotStartTime;
        System.out.println("snapshot总执行时间:" + snapshotExecutionTime + " ms");

        return clusterstream;
    }
}
