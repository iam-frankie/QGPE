//package ClusteringKD;
//
//import Model.Point;
//
//import com.google.common.collect.Sets;
//import com.google.common.collect.Maps;
//
//import java.util.Set;
//import java.util.Map;
//
//import com.google.common.base.Equivalence;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class GlobalMergerUF {
//    private int M;
//    private int currentGlobalClusterID = 0;
//    private Set<Cluster> mergingObjects; // 存储局部聚类结果
//    private Map<Cluster, Integer> globalClusterAssignments; // 存储全局簇 ID 的分配结果
//
//    public GlobalMergerUF(Set<Cluster> localClusters, int M) {
//        this.mergingObjects = localClusters;
//        this.globalClusterAssignments = new HashMap<>();
//        this.M = M;
//    }
//
//    public Set<Cluster> mergeClusters(Set<Point> mergingObjectCandidates, UnionFind unionFind) {
//        // 构建合并图，分配全局簇 ID，并返回全局簇
//        buildMergingGraph(mergingObjectCandidates, unionFind);
//        assignGlobalClusterIDs(unionFind);
//        return mergeClustersWithSameGlobalID();
//    }
//
//    public Set<Point> getMergingObjectCandidates(Set<Cluster> localClusters) {
//        // 获取合并对象的候选集合
//        return localClusters.stream()
//                .flatMap(cluster -> cluster.getPoints().stream())
//                .filter(point -> point.getBorder() && point.getCore())
//                .collect(Collectors.toSet());
//    }
//
//    private Set<Cluster> mergeClustersWithSameGlobalID() {
//        // 合并具有相同全局簇 ID 的局部簇
//        Map<Integer, Cluster> mergedClusters = new HashMap();
//
//        for (Map.Entry<Cluster, Integer> entry : globalClusterAssignments.entrySet()) {
//            Cluster cluster = entry.getKey();
//            int globalClusterID = entry.getValue();
//
//            // 如果具有相同全局簇 ID 的局部簇已经合并过，获取已合并的Cluster，否则创建一个新的Cluster
//            Cluster mergedCluster = mergedClusters.getOrDefault(globalClusterID, new Cluster(cluster.getTimestamp()));
//            mergedCluster.setClusterID(globalClusterID);
//
//            // 将当前局部簇的所有点添加到合并的Cluster中
//            for (Point point : cluster.getPoints()) {
//                mergedCluster.addPoint(point);
//            }
//
//            // 存储合并后的Cluster
//            mergedClusters.put(globalClusterID, mergedCluster);
//        }
//
//        return new HashSet<>(mergedClusters.values());
//    }
//
//    private void buildMergingGraph(Set<Point> mergingObjectCandidates, UnionFind unionFind) {
//        // 构建合并图的逻辑
//        for (Point o : mergingObjectCandidates) {
//            Set<Cluster> localClusters = mergingObjects;
//            for (Cluster cluster1 : localClusters) {
//                if (cluster1.contains(o)) {
//                    for (Cluster cluster2 : localClusters) {
//                        if (cluster2.contains(o) && !cluster1.equals(cluster2)) {
//                            // 执行union操作以合并两个点所在的集合
//                            unionFind.union(cluster1.getPoints(), cluster2.getPoints());
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private void assignGlobalClusterIDs(UnionFind unionFind) {
//        // 分配全局簇 ID
//        Map<Set<Point>, Integer> assignedIDs = new HashMap<>();
//
//        for (Cluster localCluster : mergingObjects) {
//            // 使用并查集的find操作获取局部簇的全局簇 ID
//            Set<Point> newCluster = unionFind.find(localCluster);
//
//            if (!assignedIDs.containsKey(newCluster)) {
//                // 如果根簇的全局簇 ID 尚未分配，分配一个新的全局簇 ID
//                assignedIDs.put(newCluster, assignedIDs.size());
//            }
//
//            int globalClusterID = assignedIDs.get(rootCluster);
//
//            // 仅为满足条件的局部簇分配全局簇 ID
//            if (localCluster.getPoints().size() > M) {
//                globalClusterAssignments.put(localCluster, globalClusterID);
//            }
//        }
//    }
//
//    private void assignGlobalClusterIDs() {
//        // 分配全局簇 ID
//        for (Set<Cluster> connectedComponent : findConnectedComponents()) {
//            int globalClusterID = currentGlobalClusterID;
//            for (Cluster localCluster : connectedComponent) {
////                System.out.println("簇大小"+localCluster.size()+"全局簇ID"+currentGlobalClusterID);
//                if(localCluster.size() > M)
//                    globalClusterAssignments.put(localCluster, globalClusterID);
//            }
//            currentGlobalClusterID++;
//        }
//
//        // 分配全局簇 ID 给 mergingObjects 中的其他局部簇
//        for (Cluster localCluster : mergingObjects) {
//            if (!globalClusterAssignments.containsKey(localCluster)) {
////                System.out.println("簇大小"+localCluster.getPoints().size()+"全局簇ID"+currentGlobalClusterID);
//                if(localCluster.getPoints().size() > M)
//                    globalClusterAssignments.put(localCluster, currentGlobalClusterID);
//                currentGlobalClusterID++;
//            }
//        }
//    }
//}
