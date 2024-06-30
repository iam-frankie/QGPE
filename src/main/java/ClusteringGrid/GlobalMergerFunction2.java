package ClusteringGrid;


import ClusteringKD.Cluster;
import ClusteringKD.GlobalMerger;
import ClusteringKD.Partition;
import Model.Point;
import ch.hsr.geohash.GeoHash;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.streaming.runtime.tasks.mailbox.MailboxDefaultAction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;

public class GlobalMergerFunction2 extends RichFlatMapFunction<Set<Cluster>,Cluster> {

    private int M;
    private int isTest;
    private int geohashPrecision;
    private int slidingWindowSize;

    public GlobalMergerFunction2(int M, int isTest, int geohashPrecision, int slidingWindowSize) {
        this.M = M;
        this.isTest = isTest;
        this.geohashPrecision = geohashPrecision;
        this.slidingWindowSize = slidingWindowSize;
    }

    public int calculateCellWidth(int geohashLength) {
        geohashLength = geohashLength/2;
        // The maximum longitude in degrees
        double maxLong = 180.0;
        // The cell width in degrees is the maximum longitude divided by the number of cells
        // The number of cells is 2 to the power of the number of bits
        double cellWidth = maxLong / Math.pow(2, geohashLength);
//        System.out.println(cellWidth);
        return (int) (cellWidth*111000);
    }

    public List<String> getAdjacentGeoHashStrings(String geoHashString, Cluster cluster) {
        List<String> adjacentGeoHashStrings = new ArrayList<>();
        GeoHash geoHash = GeoHash.fromGeohashString(geoHashString);

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
            adjacentGeoHashStrings.add(adjacentGeoHash.toBinaryString());
        }

        return adjacentGeoHashStrings;
    }


    public List<String> getAdjacentGeoHashStrings2(String geoHashString, Cluster cluster) {
        Set<String> adjacentGeoHashStrings = new HashSet<>();

        // 1、获取簇的平均速度，然后计算簇的最长跳距离，并且计算簇的方向
        int clusterSize = cluster.getPoints().size(); // 获取簇的大小
        int allspeed = 0;
        Map<String, Integer> directMap = new HashMap<>();

        for(Point point: cluster.getPoints()){
            allspeed += point.getSpeed();
            int times = directMap.getOrDefault(point.getDirection(), 0);
            directMap.put(point.getDirection(), times+1);
        }
        int avgSpeed = allspeed/clusterSize;
        int hoplength = avgSpeed*this.slidingWindowSize;
        String clusterDirection = Collections.max(directMap.entrySet(), Map.Entry.comparingByValue()).getKey();

        GeoHash geoHash = GeoHash.fromGeohashString(geoHashString);
        int gridLength = calculateCellWidth(geoHashString.length());
        System.out.println(hoplength + " " + gridLength);
        int hop = (int) Math.ceil((float)hoplength / gridLength);
        System.out.println("最长跳距离"+hop+"  方向是"+clusterDirection);

        // 2、获得所有需要复制的网格
        Queue<GeoHash> geoqueue = new LinkedList<>();
        geoqueue.add(geoHash);
        for (int i = 0; i < hop + 1; i++) {
            while (!geoqueue.isEmpty()) {
                GeoHash temp = geoqueue.poll();
                adjacentGeoHashStrings.add(temp.toBinaryString());
                if(i < hop)
                    switch (clusterDirection) {
                        case "ES":
                            GeoHash eneighbour = temp.getEasternNeighbour();
                            GeoHash esneighbour = eneighbour.getSouthernNeighbour();
                            GeoHash sneighbour = temp.getSouthernNeighbour();
                            geoqueue.add(eneighbour);
                            geoqueue.add(sneighbour);
                            geoqueue.add(esneighbour);
                            break;
                        case "WS":
                            GeoHash wneighbour = temp.getWesternNeighbour();
                            GeoHash wsneighbour = wneighbour.getSouthernNeighbour();
                            sneighbour = temp.getSouthernNeighbour();
                            geoqueue.add(wneighbour);
                            geoqueue.add(sneighbour);
                            geoqueue.add(wsneighbour);
                            break;
                        case "EN":
                            eneighbour = temp.getEasternNeighbour();
                            GeoHash enneighbour = eneighbour.getNorthernNeighbour();
                            GeoHash nneighbour = temp.getNorthernNeighbour();
                            geoqueue.add(eneighbour);
                            geoqueue.add(enneighbour);
                            geoqueue.add(nneighbour);
                            break;
                        case "WN":
                            wneighbour = temp.getWesternNeighbour();
                            GeoHash wnneighbour = wneighbour.getNorthernNeighbour();
                            nneighbour = temp.getNorthernNeighbour();
                            geoqueue.add(wneighbour);
                            geoqueue.add(wnneighbour);
                            geoqueue.add(nneighbour);
                            break;
                    }

            }

        }
//        System.out.println(adjacentGeoHashStrings);
        for (String adjacentGeoHashString:adjacentGeoHashStrings)
            System.out.println(adjacentGeoHashString);
        return new ArrayList<>(adjacentGeoHashStrings);
    }


    @Override
    public void flatMap(Set<Cluster> localClusters, Collector<Cluster> collector) throws Exception {
//        long start = System.currentTimeMillis();

        // 创建 GlobalMerger 实例
        System.out.println("合并前的簇数量" + localClusters.size());
        GlobalMerger globalMerger = new GlobalMerger(localClusters, M);

//        for (Cluster cluster : localClusters) {
//            System.out.println("ClusterID:" + cluster.getClusterID() + " Cluster Size: " + cluster.getPoints().size());
//            System.out.println(cluster.getPointIDs());
//        }


        Set<Point> mergingObjectCandidates = globalMerger.getMergingObjectCandidates(localClusters);   //从 localClusters 中获取合并对象的候选集
        System.out.println("合并对象的候选集大小为" + mergingObjectCandidates.size() + "个");
        // 获取合并后的全局聚类结果
        Set<Cluster> globalClusters = globalMerger.mergeClusters(mergingObjectCandidates); // 根据您的 GlobalMerger 结果获取全局聚类结果
        System.out.println("Snapshot" + globalClusters.iterator().next().getTimestamp() + "合并之后的簇数量" + globalClusters.size() + "个");
//        long end = System.currentTimeMillis();
//        System.out.println("Merging execution time: " + (end-start) + " milliseconds");

        int twentyc = 0;


        for (Cluster cluster : globalClusters) {



            int clusterSize = cluster.getPoints().size(); // 获取簇的大小

            if(isTest==1) {
                System.out.println("ClusterID:" + cluster.getClusterID() + " Cluster Size: " + clusterSize);
                if (clusterSize > 20) {
                    twentyc++;
                }
//                System.out.println(cluster.ptIDs);
                System.out.println(cluster.getPointIDs());
                System.out.println(cluster.ptIDs);
            }

            // 1、获取核心点的数量
            Set<String> corePtIDs = new HashSet<>();        // 簇的核心网格id，也就是簇的源生网格id
            for(String ptID: cluster.ptIDs){
                if(ptID.length() > geohashPrecision)
                    ptID = ptID.substring(0, geohashPrecision);
                corePtIDs.add(ptID);
            }

            // 2、获取需要复制的所有网格id
            Set<String> allPtIDs = new HashSet<>();        // 所有的网格只存在一次
            for(String ptID: corePtIDs){
                if(ptID.length() > geohashPrecision)
                    ptID = ptID.substring(0, geohashPrecision);
                allPtIDs.add(ptID);
                allPtIDs.addAll(getAdjacentGeoHashStrings(ptID, cluster));
            }

            // 3、根据簇的状态，发送不同的簇到下游
            for(String id: allPtIDs){
                cluster.ptIDs.clear();
                cluster.ptIDs.add(id);
                if(corePtIDs.contains(id)){              // 如果该网格是簇的源生网格，则激活用于后面的分区
                    cluster.setActivate(true);
                    collector.collect(cluster);         // 将簇发送到下游
                    cluster.setActivate(false);

//                    System.out.println("核心网格+1");
                }else {                                  // 如果该网格是簇的衍生网格，则不激活用于后面的分区
                    cluster.setActivate(false);
                    collector.collect(cluster); // 将簇发送到下游
                }
            }
        }
        if(isTest==1)
            System.out.println("大于20的簇数量" + twentyc);
//        // 发送合并后的全局聚类结果到下一个阶段
//        collector.collect(globalClusters);


    }
}