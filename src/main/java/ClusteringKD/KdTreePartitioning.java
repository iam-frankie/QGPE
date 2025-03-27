package ClusteringKD;

import Model.Point;

import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static Tools.DistanceOracle.compEuclidianDistance;

import Tools.JsonUtil;
import Tools.recordPtNum;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;

public class KdTreePartitioning {
    private final double epsilon;
    private static double conversionFactor = 1;
    private Map<String, Set<Integer>> partitonSet;
    private int time;

    public KdTreePartitioning(double rawEpsilon, double rawConversionFactor, int time) {
        this.epsilon = rawEpsilon;
        this.conversionFactor = rawConversionFactor;
        this.partitonSet = new HashMap<>();
        this.time = time;
    }

    public List<Partition> divideIntoSubspaces(List<Point> dataset, List<Point> pivotSet, int m, int time, int isBeijing) throws Exception {
//        System.out.println(dataset.size());
        List<SubSpace> subspaces = new ArrayList<>();
        double[] minBounds = { -180.0*conversionFactor,  -90.0*conversionFactor}; // 整个地球的最小边界坐标
        double[] maxBounds = { 180.0*conversionFactor,  90.0*conversionFactor }; // 整个地球的最大边界坐标

        if(isBeijing==1){
            minBounds = new double[]{9600000, 4400000}; // 北京的近似最小边界坐标
            maxBounds = new double[]{10400000, 4640000}; //  北京的近似最大边界坐标
        }

        long startTime = System.currentTimeMillis();
        // 创建初始子空间，即整个地球的边界
        SubSpace initialSubspace = new SubSpace(minBounds, maxBounds, time);

        // 创建一个队列，用于存储子空间
        Queue<SubSpace> queue = new LinkedList<>();
        queue.add(initialSubspace);
        int div_num = 0;
        int demension_count = 1;
        while (!queue.isEmpty()) {

            SubSpace currentSubspace = queue.poll();

            // Select a random dimension
//            int dimension = selectRandomDimension(pivotSet);
            int dimension = (demension_count++)%2;
//            System.out.println("选择的维度为"+(dimension+1)+"维");
//            System.out.println("当前的子空间为"+currentSubspace.printSubSpace());
//            System.out.println("当前的数据集大小为"+dataset.size()+"个");
            // Find the median sm of currentSubspace on the selected dimension
            List<Point> subDataset = filterDataset(dataset, currentSubspace, 0);
//            System.out.println(subDataset.size());
            double median;
//            if(div_num >= (m/2-1))
//                median = findMedian2(currentSubspace, subDataset, dimension, epsilon);
//            else
                median = findMedian(currentSubspace, subDataset, dimension);
            div_num += 1;
            // Divide currentSubspace into disjoint S1 and S2 by the median
            SubSpace[] dividedSubspaces = divideSubspace(currentSubspace, dimension, median, time);

            // Push dividedSubspaces into the queue
            for (SubSpace subspace : dividedSubspaces) {
                // Filter the dataset for the current subspace

                // Continue processing with the filtered dataset
//                if (!subDataset.isEmpty()) {
                queue.add(subspace);
//                }
            }

            // When the number of subspaces in the queue reaches or exceeds m, compute and return subspaces
            if (queue.size() >= m) {
                while (subspaces.size() < m) {
                    subspaces.add(queue.poll());
                }
                break;
            }

        }
        long endTime = System.currentTimeMillis();
//        System.out.println("分区时间为"+(endTime-startTime)+"ms");
        // If the desired number of subspaces is not reached, return what's available
//        return subspaces;
//        System.out.println("分区数为"+subspaces.size());
        List<Partition> partitions = partitionData(dataset, subspaces, this.epsilon, time);
        int count = 0;
        Set<Integer> numSet = new HashSet<>();
        int nmax = Integer.MIN_VALUE;
        int nmin = Integer.MAX_VALUE;
        for(Partition pt: partitions) {

//            System.out.println(pt.getPoints().size());
            numSet.add(pt.size());
            nmax = Math.max(nmax, pt.size());
            nmin = Math.min(nmin, pt.size());
//            System.out.println(pt.getPtID());
            if(pt.getPtID() == null || pt.getPtID().equals(""))
                pt.setPtID(String.valueOf(count++));
        }
        System.out.println("Snapshot"+ time + "-" + nmax + "-" + nmin);
//        partitonSet.put("Snapshot"+ time + "-" + nmax + "-" + nmin, numSet);
//        writeRes(partitonSet);
        return partitions;
    }



//    private static int selectRandomDimension(List<Point> pivotSet) {
//        // Replace with your logic to select a random dimension from the pivot set
//        // Example:
//        Random random = new Random();
//        int dimensionIndex = random.nextInt(2);
//        return dimensionIndex;
//    }

    // 基于点的中点
    private static double findMedian(SubSpace subspace, List<Point> dataset, int dimension) {
        // Replace with your logic to find the median along the selected dimension within the subspace
        // Example:
        List<Double> dimensionValues = new ArrayList<>();
        for (Point point : dataset) {
            double coordinate = point.getCoordinates()[dimension];
//            System.out.println(coordinate);
            if (coordinate >= subspace.minBounds[dimension] && coordinate <= subspace.maxBounds[dimension]) {
                dimensionValues.add(coordinate);
//                System.out.println(coordinate);
            }
        }
//        System.out.println(dimensionValues);
        Collections.sort(dimensionValues);
        double medianValue = dimensionValues.get((dimensionValues.size() - 1) / 2);
        return medianValue;
    }

    // 基于epsilon邻居的中点
    private static double findMedian2(SubSpace subspace, List<Point> dataset, int dimension, double epsilon) {
        // Replace with your logic to find the median along the selected dimension within the subspace
        // Example:
        int minBound = (int) subspace.minBounds[dimension];
        int maxBound = (int) subspace.maxBounds[dimension];
        int midBound = (minBound + maxBound) / 2;
        double length = subspace.maxBounds[dimension] - subspace.minBounds[dimension];
        List<Double> dimensionValues = new ArrayList<>();

        for (Point point : dataset) {
            double coordinate = point.getCoordinates()[dimension];

            if (coordinate >= subspace.minBounds[dimension] && coordinate <= subspace.maxBounds[dimension]) {
                dimensionValues.add(coordinate);
            }
        }

        Collections.sort(dimensionValues);
        double medianValue = dimensionValues.get((dimensionValues.size() - 1) / 2);
        double splitValue = medianValue;
//        int left = 0;
//        int right = 0;
//        while(){
//
//        }
//        if(medianValue < (double) (maxBound+minBound)/2){ // 中间点向左
//
//        }else{      // 中间点向右搜索
//
//        }

        int loc = dimensionValues.size()/2;
        int locremain = dimensionValues.size()/2;
//        System.out.println("left:"+loc+ " "+"right:"+locremain);

        if(medianValue < midBound){
//            System.out.println("向右搜索平衡点");
            while(true) {
                int left = 0, right = 0;
                while(dimensionValues.get(loc+left) < splitValue + epsilon) {
                    left++;

                }
                while (dimensionValues.get(loc+left+right) < splitValue + 2 * epsilon) {
                    right++;
                }
//                System.out.println("左边界"+left+"右边界"+right);
                splitValue += epsilon;
                loc += left;
                locremain-=left;
//                System.out.println(loc+right + " " + locremain+left);
                if(loc+right >= locremain+left){
                    System.out.println("left:"+(loc+right)+"  right:"+(locremain+left));
                    break;
                }

            }
        }else if(medianValue > midBound){
//            System.out.println("向左搜索平衡点");
            while(true) {
                int left = 0, right = 0;
                while (dimensionValues.get(loc-right) > splitValue - epsilon) {
                    right++;
                }
                while (dimensionValues.get(loc-left-right) > splitValue - 2 * epsilon) {
                    left++;
                }
//                System.out.println("左边界"+left+"右边界"+right);
                splitValue -= epsilon;
                loc -= right;
                locremain += right;
                if(loc+right <= locremain+left){
                    System.out.println("left:"+(loc+right)+"  right:"+(locremain+left));
                    break;
                }

            }
        }
        return splitValue;
    }

    private static double findSplitPoint(SubSpace subspace, List<Point> dataset, int dim, double epsilon) {

        double min = subspace.minBounds[dim];
        double max = subspace.maxBounds[dim];

        int windowSize = 2 * (int)epsilon;

        int left = 0;
        int[] histogram = new int[windowSize];

        int bestLeft = 0;
        int bestSplit = 0;
        int bestGap = Integer.MAX_VALUE;

        while (left + windowSize <= max) {

            // 更新滑动窗口直方图
            updateHistogram(histogram, dataset, dim, left, left+windowSize);

            // 寻找最佳分割
            int split = findBestSplit(histogram);

            // 更新最佳结果
            int gap = computeGap(histogram, split);
            if (gap < bestGap) {
                bestGap = gap;
                bestLeft = left;
                bestSplit = split;
            }

            left++;

        }

        // 计算窗口中点坐标作为最佳分割点
        return bestLeft + bestSplit + 0.5;

    }

    static void updateHistogram(int[] histogram, List<Point> dataset, int dim, int left, int right) {

        // 先清零当前直方图
        Arrays.fill(histogram, 0);

        // 重新统计滑动窗口内的分布
        for (Point p : dataset) {
            double coord = p.getCoordinates()[dim];
            if (coord >= left && coord <= right) {
                int idx = (int)(coord - left);
                histogram[idx]++;
            }
        }

    }
    private static int findBestSplit(int[] histogram) {

        int bestSplit = 0;
        int bestGap = Integer.MAX_VALUE;

        for (int i = 0; i < histogram.length - 1; i++) {

            // 计算当前分割左右两侧点数
            int leftCount = computeCount(histogram, 0, i);
            int rightCount = computeCount(histogram, i + 1, histogram.length - 1);

            // 计算差值的绝对值
            int gap = Math.abs(leftCount - rightCount);

            // 如果是最小的差值,记录分割点
            if (gap < bestGap) {
                bestGap = gap;
                bestSplit = i;
            }
        }

        return bestSplit;

    }

    private static int computeCount(int[] histogram, int start, int end) {
        // 累加分割一侧的点数
        int count = 0;
        for (int i = start; i <= end; i++) {
            count += histogram[i];
        }
        return count;
    }

    static int computeGap(int[] histogram, int split) {

        int leftCount = 0;
        for (int i = 0; i <= split; i++) {
            leftCount += histogram[i];
        }

        int rightCount = 0;
        for (int i = split + 1; i < histogram.length; i++) {
            rightCount += histogram[i];
        }

        return Math.abs(leftCount - rightCount);

    }

    private static SubSpace[] divideSubspace(SubSpace subspace, int dimension, double median, int time) {
        // Replace with your logic to divide the subspace into two disjoint subspaces
        // based on the selected dimension and median
        // Example:


        double[] minBounds1 = Arrays.copyOf(subspace.minBounds, subspace.minBounds.length);
        double[] maxBounds1 = Arrays.copyOf(subspace.maxBounds, subspace.maxBounds.length);

        maxBounds1[dimension] = median;

        double[] minBounds2 = Arrays.copyOf(subspace.minBounds, subspace.minBounds.length);
        double[] maxBounds2 = Arrays.copyOf(subspace.maxBounds, subspace.maxBounds.length);

        minBounds2[dimension] = median;

        SubSpace leftSubspace = new SubSpace(minBounds1, maxBounds1, time);
        SubSpace rightSubspace = new SubSpace(minBounds2, maxBounds2, time);

        SubSpace[] dividedSubspaces = { leftSubspace, rightSubspace };

        return dividedSubspaces;
    }


//    private static List<Point> generateRandomDataPoints(int count) {
//        List<Point> dataPoints = new ArrayList<>();
//        Random random = new Random();
//        for (int i = 0; i < count; i++) {
//            double lat = -90 + random.nextDouble() * 180; // 随机生成纬度 [-90, 90]
//            double lon = -180 + random.nextDouble() * 360; // 随机生成经度 [-180, 180]
//
//            // 限制经纬度在合法范围内
//            lat = Math.min(lat, 90.0);
//            lat = Math.max(lat, -90.0);
//            lon = Math.min(lon, 180.0);
//            lon = Math.max(lon, -180.0);
//
//            Point point = new Point(lat, lon);
//            dataPoints.add(point);
//        }
//        return dataPoints;
//    }


    public static List<Partition> partitionData(List<Point> data, List<SubSpace> subspaces, double margin, int time) {
        long startTime = System.currentTimeMillis();
        List<Partition> partitions = new ArrayList<>();
//        System.out.println("边界宽度设置为"+margin);
        int index = 0;
        for (SubSpace subspace : subspaces) {
            // 使用 filterDataset 方法筛选子空间内的数据点
            Partition partition = new Partition(filterDataset(data, subspace, margin), time, "");
            partitions.add(partition);
//            System.out.println("子空间的大小:"+subspace.printSubSpace());
////            System.out.println("新增分区大小"+partition.size());
//            System.out.println(Arrays.toString(partition.getPoints().get(0).getCoordinates()));
        }
        // 生成每个子空间的合并簇候选集，后续使用
//        generateMergeCandidates(data, subspaces, margin);
        long endTime = System.currentTimeMillis();
//        System.out.println("分区数据时间"+(endTime-startTime)+"ms");
        return partitions;
    }


    // 此处的算法对原文的算法做了简化
    private static List<Point> filterDataset(List<Point> dataset, SubSpace subspace, double margin) {
        List<Point> filteredDataset = new ArrayList<>();

        for (Point point : dataset) {
            if (isInSubSpace(point, subspace, margin)) {
//                System.out.println("添加点+1");
                point.setPivotDistance(compEuclidianDistance(point, new Point(-1, 0, 0, 0, 0)));
//                if(isOnBoundary(point, subspace, margin))
                    point.setBorder(true);
                filteredDataset.add(point);
            }
        }

        return filteredDataset;
    }

//    private static List<Point> filterDataset(List<Point> dataset, SubSpace subspace) {
//        // Filter the dataset based on the bounds of the subspace
//        List<Point> filteredDataset = new ArrayList<>();
//        for (Point point : dataset) {
//            double[] coordinates = point.getCoordinates2();
//            boolean isInSubspace = true;
//            for (int i = 0; i < coordinates.length; i++) {
//                if (coordinates[i] < subspace.minBounds[i] || coordinates[i] > subspace.maxBounds[i]) {
//                    isInSubspace = false;
//                    break;
//                }
//            }
//            if (isInSubspace) {
//                filteredDataset.add(point);
//            }
//        }
//        return filteredDataset;
//    }


    /**
     * 判断点是否在子空间内,同时标记点的边界状况
     * @param point
     * @param subspace
     * @param margin
     * @return
     */

    // 本来需要判断点是否越界，但是由于我们的数据集是经过筛选的，所以不需要判断
    private static boolean isInSubSpace(Point point, SubSpace subspace, double margin) {
        double[] coordinates = point.getCoordinates();

        boolean isOnBoundary = true;
        for (int i = 0; i < coordinates.length; i++) {
//            System.out.println("当前的点维为"+coordinates[i]);
//            System.out.println("大小范围" + subspace.minBounds[i] + " " + subspace.maxBounds[i] + " ");
            if (coordinates[i] < subspace.minBounds[i] - margin || coordinates[i] > subspace.maxBounds[i] + margin) {
                return false;
            }
//            if (Math.abs(coordinates[i]-subspace.minBounds[i])>margin || Math.abs(coordinates[i]-subspace.maxBounds[i])>margin) {
//                isOnBoundary = false;
//            }
        }
//        if(isOnBoundary)
//            point.setBorder(true);
        return true;
    }

    private static boolean isOnBoundary(Point point, SubSpace subspace, double margin) {
        double[] coordinates = point.getCoordinates();

        // 遍历每个坐标维度
        for (int dimension = 0; dimension < coordinates.length; dimension++) {
            double coordinate = coordinates[dimension];
            double minBound = subspace.minBounds[dimension];
            double maxBound = subspace.maxBounds[dimension];

            // 检查坐标是否接近边界，考虑容忍度范围（margin）
            if (minBound - coordinate > margin || coordinate - maxBound > margin) {
                return false;
            }
        }

        // 如果在每个维度上都不接近边界，则认为不在边界上
        return true;
    }


//    // 生成每个子空间的合并簇候选集
//    public static void generateMergeCandidates(List<Point> dataset, List<SubSpace> subspaces, double margin) {
////        Map<SubSpace, List<Point>> mergeCandidates = new HashMap<>();
//
//        // 遍历每个子空间
//        for (SubSpace subspace : subspaces) {
//            // 使用 filterDataset 方法筛选子空间内的数据点
//            List<Point> partition = filterDataset(dataset, subspace, margin);
//
//            // 遍历筛选后的子空间数据点
//            for (Point point : partition) {
//                // 如果数据点是边界点，将其添加到合并簇候选集
//                if (isOnBoundary(point, subspace, margin)) {
//                    // 如果子空间的候选集列表已存在，则直接添加，否则创建新的列表
//                    point.setBorder(true);
////                    mergeCandidates.computeIfAbsent(subspace, k -> new ArrayList<>()).add(point);
//                }
//            }
//        }
//    }

//    public static void main(String[] args) {
//        // 创建数据点集合 dataset
//        List<Point> dataset = generateRandomDataPoints(20); // 生成包含20个随机数据点的数据集
//
////        // 创建 pivotSet，用于选择分区维度
////        List<Point> pivotSet = generateRandomPivotSet(10); // 生成包含10个随机数据点的 pivotSet
////        System.out.println(dataset);
//        int m = 8; // 分区数
//
//        // 调用分区方法来获取子空间列表
//        List<SubSpace> subspaces = KdTreePartitioning.divideIntoSubspaces(dataset, null, m);
//
//        // 输出分区的子空间信息
//        for (int i = 0; i < subspaces.size(); i++) {
//            SubSpace subspace = subspaces.get(i);
//            System.out.println("Subspace " + i + ":");
//            System.out.println("Min Bounds: " + Arrays.toString(subspace.minBounds));
//            System.out.println("Max Bounds: " + Arrays.toString(subspace.maxBounds));
//            System.out.println();
//        }
//
//        double eps = Double.parseDouble(AppProperties.getProperty("eps"));
//        double conversionFactor = Double.parseDouble(AppProperties.getProperty("conversion_factor"));
//        double margin = eps / conversionFactor;
//        // 调用 partitionData 方法来获取分区
//        List<List<Point>> partitions = KdTreePartitioning.partitionData(dataset, subspaces, margin);
//
//        // 输出分区的数据点信息
//        for (int i = 0; i < partitions.size(); i++) {
//            List<Point> partition = partitions.get(i);
//            System.out.println("Partition " + i + ":");
//            System.out.println(partition);
//            System.out.println();
//        }
//    }

//    public static void writeRes(Map<String, Set<Integer>> neighborIdsMap) throws Exception {
//        String hdfsUri = "hdfs://amax:9000";
//        String hdfsPath = "hdfs://amax:9000//comovement/result/pt.json";
//
//        // Convert neighborhood data to JSON
//        String json = JsonUtil.toJSON(neighborIdsMap);
//
//        // Create a Hadoop configuration
//        Configuration conf = new Configuration();
//
//        // Set the HDFS URI (e.g., "hdfs://localhost:9000")
//        conf.set("fs.defaultFS", hdfsUri);
//
//        // Create a FileSystem object
//        FileSystem fs = FileSystem.get(conf);
//
//        // Define the HDFS output file path
//        Path outputPath = new Path(hdfsPath);
//
//        // Check if the file exists, if not, create it
//        if (!fs.exists(outputPath)) {
//            fs.create(outputPath).close(); // Create an empty file
//        }
//
//        // Append the data to the HDFS file
//        try (FSDataOutputStream fsos = fs.append(outputPath);
//             BufferedOutputStream bos = new BufferedOutputStream(fsos)) {
//            byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
//            bos.write(jsonBytes, 0, jsonBytes.length);
//        } finally {
//            fs.close();
//        }
//    }
}
