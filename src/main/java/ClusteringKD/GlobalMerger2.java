package ClusteringKD;

import Model.Point;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalMerger2 {
    private int M;
    private Map<Cluster, Set<Cluster>> adjacencyList; // 用于存储对象之间的关系
    private Set<Cluster> mergingObjects; // 存储局部聚类结果
    private int currentGlobalClusterID = 0;
    private Map<Cluster, Integer> globalClusterAssignments; // 存储全局簇 ID 的分配结果
    private Map<String, Cluster> clusterMap; // 存储局部簇的映射

    public GlobalMerger2(Set<Cluster> localClusters, int M) {
        this.adjacencyList = new HashMap<>();
        this.mergingObjects = localClusters;
        this.globalClusterAssignments = new HashMap<>();
        this.M = M;
        this.clusterMap = new HashMap<>(); // 初始化 clusterMap

        // 遍历 localClusters，将每个 Cluster 对象及其 clusterID 添加到 clusterMap 中
        for (Cluster cluster : localClusters) {
            clusterMap.put(cluster.getClusterID(), cluster);
        }
    }

    public Set<Cluster> mergeClusters(Set<Point> mergingObjectCandidates) {
        // 构建合并图，分配全局簇 ID，并返回全局簇
//        long start = System.currentTimeMillis();
        buildMergingGraph(mergingObjectCandidates);
        assignGlobalClusterIDs();
        Set<Cluster> res = mergeClustersWithSameGlobalID();
//        long endtime = System.currentTimeMillis();
//        System.out.println("合并时间"+(endtime-start)+"ms");
        return res;
    }


    public Set<Point> getMergingObjectCandidates(Set<Cluster> localClusters) {
        // 获取合并对象的候选集合
//        long start = System.currentTimeMillis();
        Set<Point> res =  localClusters.stream()
                .flatMap(cluster -> cluster.getPoints().stream())
                .filter(point -> point.getBorder()) //
                .collect(Collectors.toSet());
//        long endtime = System.currentTimeMillis();
//        System.out.println("获取合并对象时间"+(endtime-start)+"ms");
        return res;
    }


//    private Set<Cluster> mergeClustersWithSameGlobalID() {
//        // 合并具有相同全局簇 ID 的局部簇
//        Map<Integer, Set<Cluster>> clustersWithSameGlobalID = new HashMap<>();
//
//        for (Map.Entry<Cluster, Integer> entry : globalClusterAssignments.entrySet()) {
//            Cluster cluster = entry.getKey();
//            int globalClusterID = entry.getValue();
//
//            clustersWithSameGlobalID.computeIfAbsent(globalClusterID, k -> new HashSet<>()).add(cluster);
//        }
//
//        return clustersWithSameGlobalID.values().stream()
//                .map(clusters -> clusters.stream().flatMap(cluster -> cluster.getPoints().stream()).collect())
//                .collect(Collectors.toSet());
//    }

    private Set<Cluster> mergeClustersWithSameGlobalID() {
        // 合并具有相同全局簇 ID 的局部簇
        Map<Integer, Cluster> mergedClusters = new HashMap<>();

        for (Map.Entry<Cluster, Integer> entry : globalClusterAssignments.entrySet()) {
            Cluster cluster = entry.getKey();
            int globalClusterID = entry.getValue();


//            Cluster mergedCluster = mergedClusters.getOrDefault(globalClusterID, new Cluster(cluster.getTimestamp()));
//            mergedCluster.setClusterID(globalClusterID);
//            // 将当前局部簇的所有点添加到合并的Cluster中
//            for (Point point : cluster.getPoints()) {
//                if (!mergedCluster.contains(point))
//                    mergedCluster.addPoint(point);
//            }
//            mergedClusters.put(globalClusterID, mergedCluster);

            if(!mergedClusters.containsKey(globalClusterID)) {
                // 如果具有相同全局簇 ID 的局部簇已经合并过，获取已合并的Cluster，否则创建一个新的Cluster
                Cluster mergedCluster = mergedClusters.getOrDefault(globalClusterID, new Cluster(cluster.getTimestamp()));
                mergedCluster.setClusterID(globalClusterID);
                // 将当前局部簇的所有点添加到合并的Cluster中
                for (Point point : cluster.getPoints()) {
                    if (!mergedCluster.contains(point))
                        mergedCluster.addPoint(point);
                }
                mergedClusters.put(globalClusterID, mergedCluster);

                mergedCluster.ptIDs.add(cluster.ptIDs.get(0));      // 保存簇所在分区的点ID
            }else{
                Cluster mergedCluster = mergedClusters.get(globalClusterID);
                for (Point point : cluster.getPoints()) {
                    if (!mergedCluster.contains(point))
                        mergedCluster.addPoint(point);
                }
                mergedCluster.ptIDs.add(cluster.ptIDs.get(0));
                mergedClusters.put(globalClusterID, mergedCluster);

            }
            // 更新时间戳等其他属性，如果需要的话
            // mergedCluster.setTimestamp(...);

            // 存储合并后的Cluster
        }
        return new HashSet<>(mergedClusters.values());
    }


//    private void buildMergingGraph(Set<Point> mergingObjectCandidates) {
//        // 构建合并图的逻辑
//        for (Point o : mergingObjectCandidates) {
//            Set<Cluster> localClusters = mergingObjects;
//            for (Cluster cluster1 : localClusters) {
//                for (Cluster cluster2 : localClusters) {
//                    if (!cluster1.contains(cluster2) && !cluster2.contains(cluster1) && cluster1.contains(o) && cluster2.contains(o)) {
//                        addEdge(cluster1, cluster2);
//                    }
//                }
//            }
//        }
//    }

    private void buildMergingGraph(Set<Point> mergingObjectCandidates) {
        // 构建合并图的逻辑
//        long start = System.currentTimeMillis();
        // 簇的数量远远比边界点少，所以先遍历边界点，再遍历簇
        for (Point o : mergingObjectCandidates) {
            if(o.getCore()){
                Set<Cluster> localClusters = mergingObjects;
                for (Cluster cluster1 : localClusters) {

                    if (cluster1.contains(o)) {
                        for (Cluster cluster2 : localClusters) {
                            if (cluster2.contains(o) && !cluster1.contains(cluster2)) {
                                addEdge(cluster1, cluster2);
                            }
                        }
                    }
                }
            }
        }
//        long endtime = System.currentTimeMillis();
//        System.out.println("构建合并图时间"+(endtime-start)+"ms");
    }


    public void addEdge(Cluster cluster1, Cluster cluster2) {
        // 添加两个簇之间的边
        adjacencyList.computeIfAbsent(cluster1, k -> new HashSet<>()).add(cluster2);
        adjacencyList.computeIfAbsent(cluster2, k -> new HashSet<>()).add(cluster1);
    }

    private void assignGlobalClusterIDs() {
        // 分配全局簇 ID
        for (Set<Cluster> connectedComponent : findConnectedComponents()) {
            int globalClusterID = currentGlobalClusterID;
            for (Cluster localCluster : connectedComponent) {
//                System.out.println("簇大小"+localCluster.size()+"全局簇ID"+currentGlobalClusterID);
                if(localCluster.size() > M)
                    globalClusterAssignments.put(localCluster, globalClusterID);
            }
            currentGlobalClusterID++;
        }

        // 分配全局簇 ID 给 mergingObjects 中的其他局部簇
        for (Cluster localCluster : mergingObjects) {
            if (!globalClusterAssignments.containsKey(localCluster)) {
//                System.out.println("簇大小"+localCluster.getPoints().size()+"全局簇ID"+currentGlobalClusterID);
                if(localCluster.getPoints().size() > M)
                    globalClusterAssignments.put(localCluster, currentGlobalClusterID);
                currentGlobalClusterID++;
            }
        }
    }


    private List<Set<Cluster>> findConnectedComponents() {
        // 查找连通分量
        List<Set<Cluster>> connectedComponents = new ArrayList<>();

        for (Cluster cluster : adjacencyList.keySet()) {
            if (!cluster.isVisited()) {
                Set<Cluster> connectedComponent = new HashSet<>();
                depthFirstSearch(cluster, connectedComponent);
                connectedComponents.add(connectedComponent);
            }
        }
        System.out.println("连通图组件数" + connectedComponents.size());
        return connectedComponents;
    }

    private void depthFirstSearch(Cluster cluster, Set<Cluster> connectedComponent) {
        // 使用深度优先搜索查找连通分量
        cluster.setVisited(true);
        connectedComponent.add(cluster);

        for (Cluster neighbor : adjacencyList.get(cluster)) {
            if (!neighbor.isVisited()) {
                depthFirstSearch(neighbor, connectedComponent);
            }
        }
    }

}
