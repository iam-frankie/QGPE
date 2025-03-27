package ClusteringKD;

import Model.Point;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalMerger {
    private int M;
    private Map<String, Set<String>> adjacencyList; // 用于存储对象之间的关系
    private Set<Cluster> mergingObjects; // 存储局部聚类结果
    private String currentGlobalClusterID;
    private Map<String, String> globalClusterAssignments; // 存储全局簇 ID 的分配结果
    public Map<String, Cluster> clusterMap; // 存储局部簇的映射

    public GlobalMerger(Set<Cluster> localClusters, int M) {
        this.adjacencyList = new HashMap<>();
        this.mergingObjects = localClusters;
        this.globalClusterAssignments = new HashMap<>();
        this.M = M;
        this.clusterMap = new HashMap<>(); // 初始化 clusterMap
//        System.out.println(localClusters);
        // 遍历 localClusters，将每个 Cluster 对象及其 clusterID 添加到 clusterMap 中
        for (Cluster cluster : localClusters) {
//            System.out.println(cluster.getClusterID());
//            System.out.println(cluster.getClusterID()+" "+cluster.getPoints().size());
            clusterMap.put(cluster.getClusterID(), cluster);
        }
    }

    public Set<Cluster> mergeClusters(Set<Point> mergingObjectCandidates) {
        // 构建合并图，分配全局簇 ID，并返回全局簇
//        long start = System.currentTimeMillis();
        buildMergingGraph(mergingObjectCandidates);
        // 输出adjacencyList
//        System.out.println("adjacencyList的大小"+adjacencyList.size());
//        for (Map.Entry<String, Set<String>> entry : adjacencyList.entrySet()) {
//            System.out.println(entry.getKey());
//            System.out.println(entry.getValue());
//        }

        assignGlobalClusterIDs();
        Set<Cluster> res = mergeClustersWithSameGlobalID();
//        long endtime = System.currentTimeMillis();
//        System.out.println("合并时间"+(endtime-start)+"ms");
        return res;
    }


    public Set<Point> getMergingObjectCandidates(Set<Cluster> localClusters) {
        // 获取合并对象的候选集合
        // localClusters共有多少个个簇
//        System.out.println("局部簇数量"+localClusters.size());
//        long start = System.currentTimeMillis();
        Set<Point> res =  localClusters.stream()
                .flatMap(cluster -> cluster.getPoints().stream())
                .filter(point -> point.getBorder()) //
                .collect(Collectors.toSet());
//        long endtime = System.currentTimeMillis();
//        System.out.println("获取合并对象时间"+(endtime-start)+"ms");
//        for(Point point: res){
//            System.out.println("候选对象"+point.getOid());
//        }
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
        Map<String, Cluster> mergedClusters = new HashMap<>();

        for (Map.Entry<String, String> entry : globalClusterAssignments.entrySet()) {
            Cluster cluster = clusterMap.get(entry.getKey());
            String globalClusterID = entry.getValue();
            cluster.setClusterIDKD(globalClusterID);

//            Cluster mergedCluster = mergedClusters.getOrDefault(globalClusterID, new Cluster(cluster.getTimestamp()));
//            mergedCluster.setClusterID(globalClusterID);
//            // 将当前局部簇的所有点添加到合并的Cluster中
//            for (Point point : cluster.getPoints()) {
//                if (!mergedCluster.contains(point))
//                    mergedCluster.addPoint(point);
//            }
//            mergedClusters.put(globalClusterID, mergedCluster);

            if(!mergedClusters.containsKey(globalClusterID)) {
//                // 如果具有相同全局簇 ID 的局部簇已经合并过，获取已合并的Cluster，否则创建一个新的Cluster
//                Cluster mergedCluster = mergedClusters.getOrDefault(globalClusterID, new Cluster(cluster.getTimestamp()));
//                mergedCluster.setClusterIDKD(globalClusterID);
//                // 将当前局部簇的所有点添加到合并的Cluster中
//                for (Point point : cluster.getPoints()) {
//                    if (!mergedCluster.contains(point))
//                        mergedCluster.addPoint(point);
//                }

                mergedClusters.put(globalClusterID, cluster);

//                mergedCluster.ptIDs.add(cluster.ptIDs.get(0));      // 保存簇所在分区的点ID
            }else{
                Cluster mergedCluster = mergedClusters.get(globalClusterID);
                for (Point point: cluster.getPoints()) {
                    if (!mergedCluster.contains(point))
                        mergedCluster.addPoint(point);
                }
//                mergedCluster.ptIDs.add(cluster.ptIDs.get(0));
                mergedClusters.put(globalClusterID, mergedCluster);

            }
            // 更新时间戳等其他属性，如果需要的话
            // mergedCluster.setTimestamp(...);

            // 存储合并后的Cluster
        }
        return mergedClusters.values().stream()
                    .filter(cluster -> cluster.getPoints().size() >= M)
                    .collect(Collectors.toSet());
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
//        System.out.println("候选集合大小"+mergingObjectCandidates.size());
        for (Point o : mergingObjectCandidates) {
//            if(o.getCore()){
//                System.out.println("核心对象有"+o.getOid());
                for (Map.Entry<String, Cluster> cluster1E : clusterMap.entrySet()) {
                    Cluster cluster1 = cluster1E.getValue();
                    String cluster1ID = cluster1E.getKey();
                    if (cluster1.contains(o)) {
                        for (Map.Entry<String, Cluster> cluster2E : clusterMap.entrySet()) {
                            Cluster cluster2 = cluster2E.getValue();
                            String cluster2ID = cluster2E.getKey();
                            if(Objects.equals(cluster1ID, cluster2ID))
                                continue;
//                            if(cluster1.contains(cluster2)){
//                                System.out.println("簇"+cluster1ID+"簇"+cluster2ID+"有公共对象"+o.getOid()+"且相等");
//
//                            }
                            if (cluster2.contains(o)) {
//                                System.out.println("簇"+cluster1ID+"簇"+cluster2ID+"有公共对象"+o.getOid());
                                addEdge(cluster1ID, cluster2ID);
//                                System.out.println(cluster1ID+"和"+cluster2ID+"有公共对象"+o.getOid());
                            }
                        }
                    }
                }
//            }
        }
//        long endtime = System.currentTimeMillis();
//        System.out.println("构建合并图时间"+(endtime-start)+"ms");
    }


    public void addEdge(String cluster1ID, String cluster2ID) {
        // 添加两个簇之间的边
        adjacencyList.computeIfAbsent(cluster1ID, k -> new HashSet<>()).add(cluster2ID);
//        adjacencyList.computeIfAbsent(cluster2ID, k -> new HashSet<>()).add(cluster1ID);
    }

    private void assignGlobalClusterIDs() {
        // 分配全局簇 ID
        for (Set<String> connectedComponent : findConnectedComponents()) {
            String globalClusterID = connectedComponent.iterator().next();
            for (String localClusterID : connectedComponent) {
//                System.out.println("簇大小"+localCluster.size()+"全局簇ID"+currentGlobalClusterID);
//                if(clusterMap.get(localClusterID).size() > M)
                globalClusterAssignments.put(localClusterID, globalClusterID);
            }
//            currentGlobalClusterID++;
        }

        // 分配全局簇 ID 给 mergingObjects 中的其他局部簇
        for (Map.Entry<String, Cluster> localClusterE: clusterMap.entrySet()) {

            if (!globalClusterAssignments.containsKey(localClusterE.getKey())) {
//                System.out.println("簇大小"+localCluster.getPoints().size()+"全局簇ID"+currentGlobalClusterID);
//                if(localClusterE.getValue().getPoints().size() > M)
                globalClusterAssignments.put(localClusterE.getKey(), localClusterE.getValue().getClusterID());
//                currentGlobalClusterID++;
            }
        }
        // 输出globalClusterAssignments
//        System.out.println(globalClusterAssignments);
//        System.out.println(clusterMap.keySet());
    }


    private List<Set<String>> findConnectedComponents() {
        // 查找连通分量
        List<Set<String>> connectedComponents = new ArrayList<>();

        for (String clusterID : adjacencyList.keySet()) {
            if (!clusterMap.get(clusterID).isVisited()) {
                Set<String> connectedComponent = new HashSet<>();
                connectedComponent = depthFirstSearch(clusterID, connectedComponent);
                connectedComponents.add(connectedComponent);
            }
        }
        System.out.println("连通图组件数" + connectedComponents.size());
        return connectedComponents;
    }

    private Set<String> depthFirstSearch(String clusterID, Set<String> connectedComponent) {
        // 使用深度优先搜索查找连通分量
        clusterMap.get(clusterID).setVisited(true);
        connectedComponent.add(clusterID);

        for (String neighborID : adjacencyList.get(clusterID)) {
            if (!clusterMap.get(neighborID).isVisited()) {
                connectedComponent = depthFirstSearch(neighborID, connectedComponent);
            }
        }
        return connectedComponent;
    }

}
