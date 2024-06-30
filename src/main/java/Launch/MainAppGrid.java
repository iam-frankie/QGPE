package Launch;

import ClusteringKD.*;
import Conf.AppProperties;
import Input.LocalSnapshotSource;
import Model.SnapShot;
import ClusteringGrid.GridDBSCANSTREAM;
import PatternEnumerationGraph.EdgeGenerator;

import PatternEnumerationGraph.PatternMiningGraph;
import PatternEnumerationGraph.StarMiner;
import PatternEnumerationGrid.CESMining;
import PatternEnumerationGrid.ClusterEvolvingStream;
import PatternEnumerationGrid.GridExtracting;
import PatternEnumerationGrid.SubMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import scala.Tuple2;

import java.util.*;


public class MainAppGrid {
    public static void main(String[] args) throws Exception {
        // todo 1、得到环境参数和输入文件路径
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
//        env.setRestartStrategy(RestartStrategies.fixedDelayRestart( 3,// 尝试重启的次数
//                org.apache.flink.api.common.time.Time.of(10, TimeUnit.SECONDS)));

        String snapshotRootPath = AppProperties.getProperty("hdfs_input");
        String hdfs_uri = AppProperties.getProperty("hdfs_uri");
        int isConstrainTime = Integer.parseInt(AppProperties.getProperty("isConstrainTime"));
        int StartNo = Integer.parseInt(AppProperties.getProperty("StartNo"));
        int StopNo = Integer.parseInt(AppProperties.getProperty("StopNo"));

        int isTest = Integer.parseInt(AppProperties.getProperty("test"));
        int testTime = AppProperties.getProperty("test_time") == null ? 0 : Integer.parseInt(AppProperties.getProperty("test_time"));
        int parallelism = 1;
        int partitionNum = 2;
        int geoHashPrecision = 4;

        // todo 2、得到聚类参数并生成聚类结果
        // 设置 DBSCAN 参数
        double gridSize = Double.parseDouble(AppProperties.getProperty("gridSize"));
        double ConversionFactor = Double.parseDouble(AppProperties.getProperty("conversion_factor"));
        double epsilon = Double.parseDouble(AppProperties.getProperty("eps"));
        int minPts = Integer.parseInt(AppProperties.getProperty("minPts"));
        int M = Integer.parseInt(AppProperties.getProperty("M"));
        int K = Integer.parseInt(AppProperties.getProperty("K"));
        int L = Integer.parseInt(AppProperties.getProperty("L"));
        int G = Integer.parseInt(AppProperties.getProperty("G"));
        int SlidingWindowSize = ((K/L)-1)*(G-1)+K+L-1;
        int earth = Integer.parseInt(AppProperties.getProperty("earth"));
        parallelism = Integer.parseInt(AppProperties.getProperty("parallelism"));       // 应该是2的倍数，但最好是2的次幂,至少是2，最好小于32
        partitionNum = Integer.parseInt(AppProperties.getProperty("partitionNum"));
        geoHashPrecision = Integer.parseInt(AppProperties.getProperty("geoHashPrecision"));
        long TwindowSize = Long.parseLong(AppProperties.getProperty("TwindowSize"));
        long TransGap = Long.parseLong(AppProperties.getProperty("TransGap"));
        long MAX_RUNTIME = Long.parseLong(AppProperties.getProperty("MAX_RUNTIME"));
        int miningTimes = Integer.parseInt(AppProperties.getProperty("miningTimes"));
        DataStream<SnapShot> snapshotStream = env.addSource(new LocalSnapshotSource(hdfs_uri, snapshotRootPath, isTest, testTime, TransGap, isConstrainTime, StartNo, StopNo))
                .assignTimestampsAndWatermarks(new MyTimestampExtractor());

        Long startTime = System.currentTimeMillis(); // 记录开始时间
        // todo 2-1 基于KD-tree集中式的聚类方法
//        DataStream<List<Partition>> globalPartitions = snapshotStream
//                .map(new KdTreePartitioningFunction(partitionNum, epsilon, ConversionFactor));
////        globalPartitions.print();
//        DataStream<Set<Cluster>> localClustersStream = globalPartitions
//                .map(new LocalDBSCANFunction(epsilon, minPts)).setParallelism(20);
////        localClustersStream.print();
//        DataStream<Set<Cluster>> GlobalClustersStream = localClustersStream
//                .flatMap(new GlobalMergerFunction(M));
        // todo 2-2 基于KD-tree的流式聚类方法
        GridDBSCANSTREAM gDBSCAN = new GridDBSCANSTREAM();
        DataStream<Cluster> clusterStream = gDBSCAN.gridDBSCAN_Launcher(snapshotStream, geoHashPrecision, M, epsilon, minPts, ConversionFactor, TwindowSize, parallelism, isTest, SlidingWindowSize);

//        allClusters.writeAsText("hdfs://amax:9000/path/to/result.txt").setParallelism(1);

        env.execute("Grid-DBSCAN");
        Long endTime = System.currentTimeMillis(); // 记录结束时间
        long totalTime = endTime - startTime; // 计算总耗时

        System.out.println("运行总耗时 " + totalTime + "ms");
    }

}



// hdfs dfs -put /data/ds/dataset/beijing-part /dataset/beijing-part
// hdfs dfs -put /data/ds/dataset/Geolife/newPivotStream /dataset/Geolife/newPivotStream