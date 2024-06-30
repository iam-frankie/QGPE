package ClusteringGrid;

// GridPartitioning目前的逻辑是基于KD-tree的分区思路，每次将子空间中的中间物体的值进行进行二分，直到达到预设的分区数量为止；我想改造一下逻辑，改为基于quad-tree的分区思路，每次将子空间依据经纬度进行四分，直到达到预设的分区数量，帮我改写一下代码实现需求
// GridPartitioning的作用是进行全局网格的划分，并为每个网格赋予index的id，现在赋予id的方式其实是乱序的，因为每一个快照到来，list的分区都是混乱的，现在我想根据分区的空间经纬度信息将其固定下来id，也就是说每个固定地理空间的id分区，在不同的snapshot中都是相同的，帮我看看如何改造这个程序

import ClusteringKD.Partition;
import ClusteringKD.SubSpace;
import Model.Point;

import java.util.*;
import ch.hsr.geohash.GeoHash;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
//import com.github.davidmoten.geo.GeoHash;

import static Tools.DistanceOracle.compEuclidianDistance;

//  ch.hsr.geohash.GeoHash 库生成的GeoHash编码是基于Z-order曲线（也称为Morton Order）的。
//        Z-order曲线是一种空间填充曲线，它将多维空间的数据映射到一维空间，同时保持了空间的局部性。
//        在GeoHash中，Z-order曲线的应用主要是将经纬度两个维度的信息交织在一起，形成一个一维的字符串编码。

//  除了ch.hsr.geohash.GeoHash之外，还有以下一些实现了geohash的开源包：
//        com.github.davidmoten:geo：这是一个提供了GeoHash和Hilbert曲线实现的库，同时还提供了一些地理位置相关的工具，如距离计算、经纬度转换等。

public class GridPartitioning {
    private final double epsilon;
    private static double conversionFactor = 1;
    private Map<String, Set<Integer>> partitonSet;
    private int time;

    public static Map<String, Integer> subspaceIdMap = new HashMap<>();
    private static int nextId = 0;

    public GridPartitioning(double rawEpsilon, double rawConversionFactor, int time) {
        this.epsilon = rawEpsilon;
        this.conversionFactor = rawConversionFactor;
        this.partitonSet = new HashMap<>();
        this.time = time;
    }

    public List<Partition> divideIntoSubspaces3(List<Point> dataset, List<Point> pivotSet, int geoHashPrecision, int time) {
//        geoHashPrecision = 5;   // 设置GeoHash的精度，实际的二进制编码长度是精度的5倍
        double latlonMargin = this.epsilon/111000;
        // Create a new HashMap to store the points grouped by their GeoHash strings
        Map<String, List<Point>> geoHashMap = new HashMap<>();

        // Iterate over each point in the dataset
        for (Point point : dataset) {
            point.setPivotDistance(compEuclidianDistance(point, new Point(-1, 0, 0, 0, 0)));

            // Calculate the GeoHash string for the current point
            String geoHashString = GeoHash.withBitPrecision(point.getRawLat(), point.getRawLon(), geoHashPrecision).toBinaryString();

//            System.out.println(geoHashString); 使用位操作可以得到GeoHash的二进制编码，无法获得周围的邻居网格
//            String geohashString  = String.valueOf(GeoHash.withBitPrecision(point.getRawLat(), point.getRawLon(), 30));

            // Calculate the GeoHash strings for the neighbors of the current point
            String northNeighborGeoHashString = GeoHash.withBitPrecision(point.getRawLat() + latlonMargin, point.getRawLon(), geoHashPrecision).toBinaryString();
            String southNeighborGeoHashString = GeoHash.withBitPrecision(point.getRawLat() - latlonMargin, point.getRawLon(), geoHashPrecision).toBinaryString();
            String eastNeighborGeoHashString = GeoHash.withBitPrecision(point.getRawLat(), point.getRawLon() + latlonMargin, geoHashPrecision).toBinaryString();
            String westNeighborGeoHashString = GeoHash.withBitPrecision(point.getRawLat(), point.getRawLon() - latlonMargin, geoHashPrecision).toBinaryString();



            // If the GeoHash string of the current point is different from any of its neighbors, mark it as a boundary point
            if (!geoHashString.equals(northNeighborGeoHashString)) {
                point.setBorder(true);
                geoHashMap.computeIfAbsent(northNeighborGeoHashString, k -> new ArrayList<>()).add(point);
//                System.out.println("北边界点"+point.getOid());
            }
            if (!geoHashString.equals(southNeighborGeoHashString)) {
                point.setBorder(true);
                geoHashMap.computeIfAbsent(southNeighborGeoHashString, k -> new ArrayList<>()).add(point);
//                System.out.println("南边界点"+point.getOid());
            }
            if (!geoHashString.equals(eastNeighborGeoHashString)) {
                point.setBorder(true);
                geoHashMap.computeIfAbsent(eastNeighborGeoHashString, k -> new ArrayList<>()).add(point);
//                System.out.println("东边界点"+point.getOid());
            }
            if (!geoHashString.equals(westNeighborGeoHashString)) {
                point.setBorder(true);
                geoHashMap.computeIfAbsent(westNeighborGeoHashString, k -> new ArrayList<>()).add(point);
//                System.out.println("西边界点"+point.getOid());
            }

            // If the GeoHash string already exists in the HashMap, add the point to the corresponding list
            // Otherwise, create a new entry in the HashMap with the GeoHash string as the key and a new list containing the point as the value
            geoHashMap.computeIfAbsent(geoHashString, k -> new ArrayList<>()).add(point);
        }

        // Create a new list to store the partitions
        List<Partition> partitions = new ArrayList<>();

        // Convert each value in the HashMap (i.e., each list of points) into a new Partition object and add it to the list of partitions
        for(Map.Entry<String, List<Point>> entry : geoHashMap.entrySet()) {
            partitions.add(new Partition(entry.getValue(), time, entry.getKey()));
//            System.out.println("分区ID：" + entry.getKey() + "大小" + entry.getValue().size());
        }
        System.out.println("分区数量"+partitions.size());
        // Return the list of partitions
        return partitions;
    }

    public static List<GeoHash> getAdjacentGeoHashStrings(GeoHash geoHash) {
        List<GeoHash> adjacentGeoHashList = new ArrayList<>();

//        GeoHash geoHash = GeoHash.fromGeohashString(geoHashString);

        // 获取周围的八个网格的GeoHash对象
        GeoHash[] adjacentGeoHashes = new GeoHash[] {
                geoHash.getNorthernNeighbour(),
                geoHash.getSouthernNeighbour(),
                geoHash.getEasternNeighbour(),
                geoHash.getWesternNeighbour(),
//                geoHash.getNorthernNeighbour().getEasternNeighbour(), // Northeast
//                geoHash.getNorthernNeighbour().getWesternNeighbour(), // Northwest
//                geoHash.getSouthernNeighbour().getEasternNeighbour(), // Southeast
//                geoHash.getSouthernNeighbour().getWesternNeighbour()  // Southwest
        };

        // 将GeoHash对象转换为GeoHash字符串
        for (GeoHash adjacentGeoHash : adjacentGeoHashes) {
            adjacentGeoHashList.add(adjacentGeoHash);
        }

        return adjacentGeoHashList;
    }

    public List<Partition> divideIntoSubspacesQuadTree(List<Point> dataset, List<Point> pivotSet, int geoHashPrecision, int time) {
        double latlonMargin = this.epsilon/111000;
        Map<String, Partition> geoHashMap = new HashMap<>();
//        System.out.println("总的点数量"+dataset.size());
        // todo 1、遍历所有的点，将每个点加入到对应的分区中
        for (Point point : dataset) {
            point.setPivotDistance(compEuclidianDistance(point, new Point(-1, 0, 0, 0, 0)));
            GeoHash geoHash = null;
            try {
                geoHash = GeoHash.withBitPrecision(point.getRawLat(), point.getRawLon(), geoHashPrecision);
//                String geoHashString = geoHash.toBinaryString();
                // Rest of your code...
            } catch (Exception e) {
                // Log the exception if necessary
                System.err.println("Error processing point: " + e.getMessage());
                continue;
            }
            String geoHashString = geoHash.toBinaryString();
            if(geoHashMap.containsKey(geoHashString))
                geoHashMap.get(geoHashString).addPoint(point);
            else{
                Partition partition = new Partition(new ArrayList<>(), time, geoHashString);
                partition.setGeoHash(geoHash);
                partition.addPoint(point);
                geoHashMap.put(geoHashString, partition);
            }
//            geoHashMap.computeIfAbsent(geoHashString, k -> new Partition(new ArrayList<>(), time, k)).addPoint(point);
        }
        System.out.println("quad细分之前的分区数量"+geoHashMap.size());
        List<Partition> partitions = new ArrayList<>();
        // 创建一个Comparator，比较Partition的大小, 从大到小排序（默认是递增排序，已经逆转）
        Comparator<Partition> comparator = Comparator.comparingInt(Partition::size).reversed();
        PriorityQueue<Partition> queue = new PriorityQueue<>(comparator);
        int minSize;

        // todo 2、将分区加入到优先队列中, 并记录每个分区的大小，得到中位数，方便后续的细分
        List<Integer> sizes = new ArrayList<>();
        int count = 0;
        for(Partition partition : geoHashMap.values()){
            count+=partition.getPoints().size();
            queue.add(partition);
            sizes.add(partition.size());
        }
        System.out.println("分区后总的点数量"+count);
        Collections.sort(sizes);
        int middle = sizes.size()/2;
        if (sizes.size() % 2 == 1) {
            minSize = sizes.get(middle);
        } else {
            minSize = (sizes.get(middle-1) + sizes.get(middle)) / 2;
        }

        // todo 3、细分分区，直到每个分区的大小都小于等于中位数*2
        while (true) {
            Partition maxPartition = queue.peek();
            assert maxPartition != null;
            if (maxPartition.size() <= 2 * minSize) break;

            List<Partition> dividedPartitions = dividePartitionIntoFour(maxPartition, time);
//            for(Partition pt: dividedPartitions){
//                minSize = Math.min(minSize, pt.size());
//            }
            if(dividedPartitions!=null){
                queue.remove(maxPartition);
                queue.addAll(dividedPartitions);
            }else break;
        }


        // todo 4、获得所有边界点,这里的逻辑是依据分区来的，获得的border map每个分区的点包含了来自周围四个方向的点
        Map<String, List<Point>> borderPointsMap = new HashMap<>();

        while(!queue.isEmpty()){
            Partition partition = queue.poll();
            List<Point> points = new ArrayList<>(partition.getPoints());
            int igeoHashPrecision = partition.getGeoHash().toBinaryString().length();
            partition.clear();
            for(Point point: points){
                String geoHashString = GeoHash.withBitPrecision(point.getRawLat(), point.getRawLon(), igeoHashPrecision).toBinaryString();

                // Calculate the GeoHash strings for the neighbors of the current point
                String northNeighborGeoHashString = GeoHash.withBitPrecision(point.getRawLat() + latlonMargin, point.getRawLon(), igeoHashPrecision).toBinaryString();
                String southNeighborGeoHashString = GeoHash.withBitPrecision(point.getRawLat() - latlonMargin, point.getRawLon(), igeoHashPrecision).toBinaryString();
                String eastNeighborGeoHashString = GeoHash.withBitPrecision(point.getRawLat(), point.getRawLon() + latlonMargin, igeoHashPrecision).toBinaryString();
                String westNeighborGeoHashString = GeoHash.withBitPrecision(point.getRawLat(), point.getRawLon() - latlonMargin, igeoHashPrecision).toBinaryString();

                if (!geoHashString.equals(northNeighborGeoHashString)) {
                    point.setBorder(true);
                    borderPointsMap.computeIfAbsent(northNeighborGeoHashString, k -> new ArrayList<>()).add(point);
//                System.out.println("北边界点"+point.getOid());
                }
                if (!geoHashString.equals(southNeighborGeoHashString)) {
                    point.setBorder(true);
                    borderPointsMap.computeIfAbsent(southNeighborGeoHashString, k -> new ArrayList<>()).add(point);
//                System.out.println("南边界点"+point.getOid());
                }
                if (!geoHashString.equals(eastNeighborGeoHashString)) {
                    point.setBorder(true);
                    borderPointsMap.computeIfAbsent(eastNeighborGeoHashString, k -> new ArrayList<>()).add(point);
//                System.out.println("东边界点"+point.getOid());
                }
                if (!geoHashString.equals(westNeighborGeoHashString)) {
                    point.setBorder(true);
                    borderPointsMap.computeIfAbsent(westNeighborGeoHashString, k -> new ArrayList<>()).add(point);
//                System.out.println("西边界点"+point.getOid());
                }
//                String[] directions = {northNeighborGeoHashString, southNeighborGeoHashString, eastNeighborGeoHashString, westNeighborGeoHashString};
//                for (String direction : directions) {
//                    if (!geoHashString.equals(direction)) {
//                        point.setBorder(true);
//                        break;
//                    }
//                }

                partition.addPoint(point);
            }
//            System.out.println(partition.getPtID());
            partitions.add(partition);
        }
//        System.out.println(borderPointsMap.keySet());
        Set<Integer> geoHashPrecisionType = new HashSet<>();
        for(Partition pt: partitions){
            geoHashPrecisionType.add(pt.getGeoHash().toBinaryString().length());
        }

        // todo 5、将边界点加入到分区中
        // 1、长度匹配的添加
        for(Partition pt: partitions){
            String ptID = pt.getPtID();
            if(borderPointsMap.containsKey(ptID)){
                pt.addAll(borderPointsMap.get(ptID));
                borderPointsMap.remove(ptID);
            }else{
                List<String> keysToRemove = new ArrayList<>();
                for(String key: borderPointsMap.keySet()){
                    // 2、小网格往大网格添加,前缀包含则大网格全包含小网格点
                    if(key.length() > ptID.length() && key.startsWith(ptID)){
                        pt.addAll(borderPointsMap.get(key));
                        keysToRemove.add(key);
                    }
                    // 3、大网格往小网格添加,需要进行精确筛选
                    if(key.length() < ptID.length() && ptID.startsWith(key)){
                        pt.addAll(borderPointsMap.get(key));

//                        List<Point> temp = new ArrayList<>();
//                        int microPrecision = ptID.length();
//
//                        for(Point point: borderPointsMap.get(key)){
//                            String geoHashString = GeoHash.withBitPrecision(point.getRawLat(), point.getRawLon(), microPrecision).toBinaryString();
//                            if(geoHashString.equals(ptID)){
//                                temp.add(point);
//                            }
//                        }
//                        pt.addAll(temp);
//                        keysToRemove.add(key);
                        //  会产生一些冗余，但是不影响结果
                    }

                }
                for(String key: keysToRemove){
                    borderPointsMap.remove(key);
                }
            }
        }
        // 3、大网格往小网格添加
//        System.out.println(borderPointsMap.keySet());
//        for(String key: borderPointsMap.keySet()){
//            System.out.println(key.length());
//        }

        System.out.println("quad细分之后的分区数量"+partitions.size());
        return partitions;
    }

    private List<Partition> dividePartitionIntoFour(Partition partition, int time) {
        Map<String, Partition> geoHashMap = new HashMap<>();

        GeoHash geoHash = partition.getGeoHash();
        String geoHashString = geoHash.toBinaryString();
        int geoHashPrecision = geoHashString.length();
        if(geoHashPrecision > 28)
            return null;
//        System.out.println("分区前的大小"+partition.size());
        for (Point point : partition.getPoints()) {
            GeoHash pgeoHash = GeoHash.withBitPrecision(point.getRawLat(), point.getRawLon(), geoHashPrecision+2);
            String pgeoHashString = pgeoHash.toBinaryString();      // 只用来生成不同分区，不用来作为分区依据
            if(geoHashMap.containsKey(pgeoHashString))
                geoHashMap.get(pgeoHashString).addPoint(point);
            else{
                Partition new_partition = new Partition(new ArrayList<>(), time, pgeoHashString);
                new_partition.setGeoHash(pgeoHash);
                new_partition.addPoint(point);
                geoHashMap.put(pgeoHashString, new_partition);
            }
//            geoHashMap.computeIfAbsent(geoHashString, k -> new Partition(new ArrayList<>(), time, k)).addPoint(point);
        }
//        for(Partition pt: geoHashMap.values()){
//            System.out.println("分区"+pt.getPtID()+"的大小：" +pt.size());
//        }
        return new ArrayList<>(geoHashMap.values());
    }

}
