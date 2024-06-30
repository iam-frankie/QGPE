package PatternEnumerationGrid;

import ClusteringKD.Cluster;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import scala.Tuple2;

import java.util.*;

public class GridExtracting extends KeyedProcessFunction<String, Cluster, SubMap> {
    private MapState<Integer, TreeSet<Long>> PartitionMap;
    private MapState<Tuple2<Integer, Integer>, TreeSet<Long>> PartitionEdgeMap;
    private int eta;
    private int M;
    private int K;
    private int L;
    private int G;

    public GridExtracting(int eta, int M, int K, int L, int G) {
        this.eta = eta;
        this.M = M;
        this.K = K;
        this.L = L;
        this.G = G;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        MapStateDescriptor<Integer, TreeSet<Long>> descriptor = new MapStateDescriptor<>(
                "ClusterMapState",
                TypeInformation.of(new TypeHint<Integer>() {}),
                TypeInformation.of(new TypeHint<TreeSet<Long>>() {})
        );
        PartitionMap = getRuntimeContext().getMapState(descriptor);

        MapStateDescriptor<Tuple2<Integer, Integer>, TreeSet<Long>> descriptor2 = new MapStateDescriptor<>(
                "ClusterMapState",
                TypeInformation.of(new TypeHint<Tuple2<Integer, Integer>>() {}),
                TypeInformation.of(new TypeHint<TreeSet<Long>>() {})
        );
        PartitionEdgeMap = getRuntimeContext().getMapState(descriptor2);
    }

    @Override
    public void processElement(Cluster cluster, Context ctx, Collector<SubMap> out) throws Exception {
        Map<Integer, TreeSet<Long>> retMap = null;
        Map<Integer, TreeSet<Long>> subMap = new HashMap<>();
        Map<Tuple2<Integer, Integer>, TreeSet<Long>> subEdgeMap = new HashMap<>();
        List<Integer> clusterIDs = new ArrayList<>(cluster.getPointIDs());
        clusterIDs.sort(Collections.reverseOrder());

//        for (Integer pointID : cluster.getPointIDs()) {
//            PriorityQueue<Long> timestamps = PartitionMap.get(pointID);
//            if (timestamps == null) {
//                timestamps = new PriorityQueue<>();
//            }
//            timestamps.add(cluster.getTimestamp());
//            while (!timestamps.isEmpty() && timestamps.peek() < cluster.getTimestamp() - eta) {        // 这一步逻辑顺利，如果长时间没有更新的点，会被删除
//                timestamps.poll();
//            }
//            PartitionMap.put(pointID, timestamps);
//            if(timestamps.size() >= K)
//                subMap.put(pointID, new PriorityQueue<>(timestamps));
//        }
//
//        if(subMap.size() >= M && cluster.isActivate()) {        // 只有簇不为空且激活时才能产生挖掘子任务
//            SubMap subMapWithTimestamp = new SubMap(subMap, cluster.getTimestamp());
//            out.collect(subMapWithTimestamp);
//        }

        for(int i = 0; i < clusterIDs.size()-M; i++) {
            int pointID1 = clusterIDs.get(i);
            TreeSet<Long> timestamps = null;
            for(int j = i + 1; j < clusterIDs.size(); j++) {
                int pointID2 = clusterIDs.get(j);
                Tuple2<Integer, Integer> key = new Tuple2<>(pointID1, pointID2);
                timestamps = PartitionEdgeMap.get(key);
                if (timestamps == null) {
                    timestamps = new TreeSet<>();
                }
                timestamps.add(cluster.getTimestamp());
//                while (!timestamps.isEmpty() && timestamps.peek() < cluster.getTimestamp() - eta) {
//                    timestamps.poll();
//                }
                timestamps.removeIf(time -> time < cluster.getTimestamp() - eta);

                PartitionEdgeMap.put(key, timestamps);
                if(timestamps.size() >= K)
                    subMap.put(pointID2, new TreeSet<>(timestamps));
            }
            subMap.put(pointID1, new TreeSet<>(timestamps));
            if(retMap==null)
                retMap = new HashMap<>(subMap);
            else{
                if(retMap.size() < subMap.size())
                    retMap = new HashMap<>(subMap);
            }
            subMap.clear();
        }

        if(retMap!=null && retMap.size() >= M && cluster.isActivate()) {        // 只有簇不为空且激活时才能产生挖掘子任务
            SubMap subMapWithTimestamp = new SubMap(retMap, cluster.getTimestamp());
//            System.out.println(cluster.getTimestamp()+"时产生子任务"+cluster.getClusterID());
//            System.out.println(retMap);
            out.collect(subMapWithTimestamp);
        }

    }
}