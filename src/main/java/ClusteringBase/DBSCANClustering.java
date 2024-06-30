package ClusteringBase;

import java.util.*;

import Model.Point;
import Model.SimpleCluster;
import Model.SnapShot;
import Conf.AppProperties;
import Tools.DistanceOracle;

import Tools.JsonUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class DBSCANClustering {
    private int ID_COUNTER = 0;

    private enum PointStatus {
        NOISE,
        PART_OF_CLUSTER
    }

    private SnapShot snapshot;
    private ArrayList<SimpleCluster> clusters;
    private double eps;
    private double minPts;

    private int earth = AppProperties.getProperty("earth").equals("1") ? 1 : 0;

    public DBSCANClustering(double eps, int minPts, SnapShot sp) throws Exception {
        this.eps = eps;
        this.minPts = minPts;
        this.snapshot = sp;
        clusters = cluster();
    }

    public DBSCANClustering(double eps, int minPts, SnapShot sp, int earth) throws Exception {
        this.eps = eps;
        this.minPts = minPts;
        this.snapshot = sp;
        clusters = cluster();
        this.earth = earth;
    }

    public ArrayList<SimpleCluster> getCluster() {
        return clusters;
    }

    public ArrayList<SimpleCluster> cluster() throws Exception {
        ArrayList<SimpleCluster> clusters = new ArrayList<>();
        Map<Integer, PointStatus> visited = new HashMap<>();
        Map<Integer, List<Integer>> neighborIdsMap = new HashMap<>(); // 存储neighbors信息
        List<Integer> printOidList = Arrays.asList(687336, 843054, 494548, 696972, 82071, 828859, 375101, 260345, 301902, 214303, 545638);


        for (Integer point : snapshot.getObjects()) {
            if (visited.get(point) != null) {
                continue;
            }
            ArrayList<Integer> neighbors = getNeighbors(point);
//            if(printOidList.contains(point))
//                System.out.println(point+"有"+neighbors.size()+"个邻居");
//            if(point == 687336)
//                System.out.println(neighbors.toString());
            if(neighbors.size()!=0)
                neighborIdsMap.put(point, neighbors); // 存储neighbors信息

            if (neighbors.size() >= minPts) {
                SimpleCluster cluster = new SimpleCluster();
                String id = snapshot.getTS() + "-" + String.valueOf(ID_COUNTER++);
                cluster.setID(id);
                clusters.add(expandCluster(cluster, point, neighbors, visited));
            } else {
                visited.put(point, PointStatus.NOISE);
            }
        }

        // 调用方法保存neighbors信息到文件
//        saveNeighborhoodsToHDFS(neighborIdsMap);

        return clusters;
    }


    private SimpleCluster expandCluster(SimpleCluster cluster,
                                        int point,
                                        ArrayList<Integer> neighbors,
                                        Map<Integer, PointStatus>  visited) {
        cluster.addObject(point);
        visited.put(point, PointStatus.PART_OF_CLUSTER);

        ArrayList<Integer> seeds = new ArrayList<>(neighbors);
        int index = 0;
        while (index < seeds.size()) {
            int current = seeds.get(index);
            PointStatus pStatus = visited.get(current);
            if (pStatus == null) {
                ArrayList<Integer> currentNeighbors = getNeighbors(current);
                if (currentNeighbors.size() >= minPts) {
                    seeds = merge(seeds, currentNeighbors); // 核心点被作为种子点，将其邻居点加入种子点集合
                }
            }

            if (pStatus != PointStatus.PART_OF_CLUSTER) {
                visited.put(current, PointStatus.PART_OF_CLUSTER);
                cluster.addObject(current);
            }
            index++;
        }
        return cluster;
    }



    public void saveNeighborhoodsToHDFS(Map<Integer, List<Integer>> neighborIdsMap) throws Exception {
        String hdfsUri = "hdfs://amax:9000";
        String hdfsPath = "hdfs://amax:9000/dataset/BaseNeighbour.json";

        // Convert neighborhood data to JSON
        String json = JsonUtil.toJSON(neighborIdsMap);

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

    // 获取一个点的所有邻居点
    private ArrayList<Integer> getNeighbors(int point) {
        ArrayList<Integer> neighbors = new ArrayList<>();
        for (int neighbor : snapshot.getObjects()) {
//            if(point==687336 && neighbor==843054) {
//                System.out.println(dist(point, neighbor) + "-" + "-" + eps);
//                System.out.println(dist(neighbor, point) <= eps);
//            }
            if (point != neighbor && dist(neighbor, point) <= eps) {
//                System.out.println("neighbor: " + dist(neighbor, point));

                neighbors.add(neighbor);
//                if(point==687336 && neighbor==843054) {
//                    System.out.println(dist(point, neighbor));
//                    System.out.println(neighbors);
//                }
            }
        }
//        if(point == 687336) {
//            System.out.println("687336有" + neighbors.size() + "个邻居");
//            System.out.println(eps);
//            System.out.println(neighbors);
//        }
        return neighbors;

    }

    // 计算点和邻居点之间的距离
    private double dist(int neighbor, int point) {
        Point p1 = snapshot.getPoint(neighbor);
        Point p2 = snapshot.getPoint(point);

        if (earth == 1) {
            return DistanceOracle.compEuclidianDistance(p1, p2);
        } else {
            return DistanceOracle.compEuclidianDistance(p1, p2);
        }
    }

    // 合并两个ArrayList
    private ArrayList<Integer> merge(ArrayList<Integer> one, ArrayList<Integer> two) {
        HashSet<Integer> oneSet = new HashSet<>(one);
        for (int item : two) {
            if (!oneSet.contains(item)) {
                one.add(item);
            }
        }
        return one;
    }
}
