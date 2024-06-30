package ClusteringKD;

import Model.Point;
import Tools.JsonUtil;
import it.unimi.dsi.fastutil.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static Tools.DistanceOracle.compEuclidianDistance;

public class LocalDBSCAN {
    private final int MinPts;
    private final double Epsilon;
    private final long time;
    private String ptID;

    private final List<Point> dataset;
    private Set<Cluster> clusters = null; // 用于存储聚类簇的列表
    Map<Point, Set<Point>> neighborhoods = null;


    public LocalDBSCAN(Partition dataset, int minPts, double epsilon) {
        this.MinPts = minPts;
        this.Epsilon = epsilon;
        this.dataset = dataset.getPoints();
        this.clusters = new HashSet<>();
        this.time = dataset.getTimestamp();
        this.ptID = dataset.getPtID();
    }

    public Map<Point, Set<Point>> slidingWindowNeighborhoodQuery(double epsilon) {
        Point spj = new Point(-1, 0, 0, 0, 0);
        HashMap<Point, Set<Point>> neighbors = new HashMap<>();

        // 随机选择一个维度
//        int j = new Random().nextInt(2);
        int j = 1;

        // 对数据点集合进行排序,默认是升序
//        dataset.sort(Comparator.comparingDouble(o -> o.getCoordinates()[j]));
        dataset.sort(Comparator.comparingDouble(Point::getPivotDistance));

        // 缓存已经计算过的点对邻居关系
        Map<Point, Set<Point>> cache = new HashMap<>();


        for (int l = 0; l < dataset.size(); l++) {
            Point ol = dataset.get(l);
            Set<Point> olNeighbor = new HashSet<>();

            // 用缓存加速查询
            if (cache.containsKey(ol)) {
                olNeighbor.addAll(cache.get(ol));
            }

            for (int u = l+1; u < dataset.size(); u++) {
                Point ou = dataset.get(u);

                if((compEuclidianDistance(spj,ou) - compEuclidianDistance(spj,ol)) > epsilon){
                    break;
                }

                double distance = compEuclidianDistance(ol, ou);
//                if(ol.getOid()==687336 && ou.getOid()==843054) {
//                    System.out.println("ol-" + ol.getOid() + "ou-" + ou.getOid());
//                    System.out.println(compEuclidianDistance(ou, ol));
//                }
                if (distance <= epsilon) {
                    olNeighbor.add(ou);
                    cache.computeIfAbsent(ol, k -> new HashSet<>()).add(ou);
                    cache.computeIfAbsent(ou, k -> new HashSet<>()).add(ol);
                }
            }
            neighbors.computeIfAbsent(ol, k-> new HashSet<>()).addAll(olNeighbor);
//            ol.addNeighbors(olNeighbor, true);
//            if (ol.getOid() == 375101)
//                System.out.println("375101的邻居数为"+olNeighbor.size());
        }
        return neighbors;
    }

    // 局部DBSCAN聚类
    public void Clustering(String ptID) throws Exception {
//        System.out.println("开始局部DBSCAN聚类，共"+dataset.size()+"个数据点");
        List<Cluster> resultClusters = new ArrayList<>();
        this.neighborhoods = slidingWindowNeighborhoodQuery(Epsilon);
//        saveNeighborhoodsToHDFS();
//        System.out.println(this.neighborhoods.size());
        List<Integer> printOidList = Arrays.asList(687336, 843054, 494548, 696972, 82071, 828859, 375101, 260345, 301902, 214303, 545638);

        int count = 0;
        for (Point point : dataset) {
//            if (printOidList.contains(point.getOid())) {
//                System.out.println("Point with Oid " + point.getOid() + " has " + neighborhoods.get(point).size() + " neighbors.");
//            }
//            if(point.getOid()==687336)
//                for(Point p: neighborhoods.get(point)){
//                    System.out.println("邻居的oid为"+p.getOid());
//                    if(p.getOid()==843054)
//                        System.out.println(compEuclidianDistance(p, point));
//                }
            if (point.getPointStatus() != 0)
                continue;
            Cluster cluster = expandCluster(point, neighborhoods.get(point));

            if (cluster != null && cluster.getPoints().size() > 0) {
                cluster.setClusterIDKD(ptID+"_"+count++);
                cluster.ptIDs.add(this.ptID);
//                System.out.println("新增局部簇大小"+cluster.size()+"，簇的ID为"+cluster.getClusterID());

                resultClusters.add(cluster);
            }
        }
        clusters.addAll(resultClusters);
    }

    private Cluster expandCluster(Point point, Set<Point> neighbors) {

//        Set<Point> neighbors = new HashSet<>(neighborhood);

        if (neighbors.size() < MinPts) {
            point.setPointStatus(-1); // Mark as noise or visited
            return null;
        } else {
            Cluster cluster = new Cluster(this.time);
            point.setPointStatus(1); ; // Mark as visited
            cluster.addPoint(point); // Add point to the cluster

            point.setCore(true);    // 标记为核心点,因为该点的邻居数大于等于MinPts
            Queue<Point> seedsQueue = new LinkedList<>(neighbors);

            while (!seedsQueue.isEmpty()) {
                Point current = seedsQueue.poll();
                if(current.getPointStatus()==0) {
                    Set<Point> currentNeighborhood = neighborhoods.get(current);
                    if (currentNeighborhood.size() >= MinPts) {
                        current.setCore(true);    // 标记为核心点,因为该点的邻居数大于等于MinPts
//                        for (Point neighbor : currentNeighborhood) {
//                            if (!neighbor.isVisited()) {
//                                //                            neighbors.add(neighbor);
//                                seedsQueue.add(neighbor);
//                            }
//                        }
                        seedsQueue.addAll(currentNeighborhood);
                    }
                }
                if (current.getPointStatus()!=1) {
                    cluster.addPoint(current);
                    current.setPointStatus(1);      // 标记为已被簇包含
                }
            }
            return cluster;
        }
    }

    public void Clustering2(String ptID) throws Exception {
        List<Cluster> resultClusters = new ArrayList<>();
        this.neighborhoods = slidingWindowNeighborhoodQuery(Epsilon);
//        saveNeighborhoodsToHDFS();
        int count = 0;
        for (int i = 0; i < dataset.size(); i++) {
            Point point = dataset.get(i);
            if (point.isVisited())
                continue;
            point.setVisited(true);
            if(neighborhoods.get(point).size() < MinPts)
                point.setPointStatus(-1);
            else{
//                point.setPointStatus(1);
                point.setCore(true);
                Cluster cluster = new Cluster(this.time);
                cluster.addPoint(point);
                List<Point> seeds = new ArrayList<>(neighborhoods.get(point));
                int index = 0;
                while(index < seeds.size()){
                    Point neighbor = seeds.get(index++);
                    if(!neighbor.isVisited()){
                        neighbor.setVisited(true);
                        if(neighborhoods.get(neighbor).size() >= MinPts){
//                            point.setPointStatus(1);
                            neighbor.setCore(true);
                            seeds.addAll(neighborhoods.get(neighbor));
                        }else{
                            neighbor.setBorder(true);
                        }
                    }else{
                        cluster.addPoint(neighbor);
                        if(neighbor.getPointStatus()==-1)
                            neighbor.setBorder(true);
                    }
                }
                cluster.setClusterIDKD(ptID+"_"+count++);
                cluster.ptIDs.add(this.ptID);
                clusters.add(cluster);
            }
        }
    }


    public void saveNeighborhoodsToHDFS() throws Exception {
        String hdfsUri = "hdfs://amax:9000";
        String hdfsPath = "hdfs://amax:9000/dataset/MyNeighbour.json";

        // Convert neighborhoods data to JSON
        Map<Integer, List<Integer>> idsMap = new HashMap<>();
        for (Point p : neighborhoods.keySet()) {
            List<Integer> ids = neighborhoods.get(p).stream()
                    .map(Point::getOid)
                    .collect(Collectors.toList());
            if(ids.size()!=0)
                idsMap.put(p.getOid(), ids);
        }
        String json = JsonUtil.toJSON(idsMap);

        // Create a Hadoop configuration
        Configuration conf = new Configuration();

        // Set the HDFS URI (e.g., "hdfs://localhost:9000")
        conf.set("fs.defaultFS", hdfsUri);

        // Create a FileSystem object
        FileSystem fs = FileSystem.get(conf);

        // Define the HDFS output file path
        Path outputPath = new Path(hdfsPath);

        // Check if the file exists, if not, create it
        if (!fs.exists(outputPath)) {
            fs.create(outputPath).close(); // Create an empty file
        }

        // Append the data to the HDFS file
        try (FSDataOutputStream fsos = fs.append(outputPath);
             BufferedOutputStream bos = new BufferedOutputStream(fsos)) {
            byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
            bos.write(jsonBytes, 0, jsonBytes.length);
        } finally {
            fs.close();
        }
    }

//    // 局部DBSCAN聚类
//    public void Clustering2() {
//        List<Cluster> resultClusters = new ArrayList<>();
//        this.neighborhoods = slidingWindowNeighborhoodQuery(Epsilon);
//
//
//        for (int i = 0; i < dataset.size(); i++) {
//            Point point = dataset.get(i);
//            if (!point.isVisited()) {
//                Cluster cluster = expandCluster(point, neighborhoods.get(point));
//                if (cluster.getPoints().size()>0) {
////                    System.out.println("新增局部簇大小"+cluster.size());
//                    resultClusters.add(cluster);
//                }
//            }
//        }
//
//        clusters.addAll(resultClusters);
//    }
//
//    private Cluster expandCluster2(Point point, List<Point> neighborhood) {
//        Cluster cluster = new Cluster(this.time);
//        Set<Point> neighbors = new HashSet<>(neighborhood);
//
//        if (neighbors.size() < MinPts) {
//            point.setVisited(true); // Mark as noise or visited
//        } else {
//            point.setVisited(true); ; // Mark as visited
//            cluster.addPoint(point); // Add point to the cluster
//
//            point.setCore(true);    // 标记为核心点,因为该点的邻居数大于等于MinPts
//            Queue<Point> queue = new LinkedList<>(neighbors);
//
//            while (!queue.isEmpty()) {
//                Point current = queue.poll();
//                if(!current.isVisited()) {
//                    List<Point> currentNeighborhood = neighborhoods.get(current);
//                    current.setVisited(true);
//                    if (currentNeighborhood.size() >= MinPts) {
//                        current.setCore(true);    // 标记为核心点,因为该点的邻居数大于等于MinPts
//                        for (Point neighbor : currentNeighborhood) {
//                            if (!neighbor.isVisited()) {
//                                //                            neighbors.add(neighbor);
//                                queue.add(neighbor);
//                            }
//                        }
//                    }
//                }else{
//                    if (!isPointInAnyCluster(current, clusters)) {
//                        cluster.addPoint(current);
//                    }
//                }
//            }
//        }
//
//        return cluster;
//    }

//    private boolean isPointInAnyCluster(Point point, Set<Cluster> clusters) {
//        for (Cluster cluster : clusters) {
//            if (cluster.contains(point)) {
//                return true;
//            }
//        }
//        return false;
//    }

    public Set<Cluster> getClusters() {
        return clusters;
    }


}
