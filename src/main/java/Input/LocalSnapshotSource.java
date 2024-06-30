package Input;
import Model.SnapShot;
import Model.Point;
import Conf.AppProperties;
import org.apache.flink.core.fs.FileStatus;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;

/*
    * This class is used to read the snapshots from the directory and send them to the Flink stream
    * 由于该场景下快照数据是串行的，因此使用了SourceFunction
    * 考虑到其他场景如果有并行数据源的情况，可以使用ParallelSourceFunction，RichParallelSourceFunction
    * 并行时发送的数据是乱序的，因此需要在数据中加入时间戳，然后使用assignTimestampsAndWatermarks
 */

public class LocalSnapshotSource implements SourceFunction<SnapShot>, Serializable {
    private Path[] snapshotFilePaths;
    private volatile boolean isRunning = true;
    private boolean isTest;
    private String rootDir;
    private int testTime;
    private String hdfs_uri;
    private FileSystem hdfs = null;
    private long tranGap;
    private int isConstrainTime = 2;
    private int StartNo = 0;
    private int StopNo  = 0;

    public LocalSnapshotSource(String hdfs_uri, String rootDir, int isTest, int testTime, long transGap, int isConstrainTime, int startNo, int stopNo) throws IOException, URISyntaxException {
        this.hdfs_uri = hdfs_uri;
        this.rootDir = rootDir;
        this.isTest = (isTest == 1);
        this.testTime = testTime;
        this.tranGap = transGap;
        this.isConstrainTime = isConstrainTime;
        this.StartNo = startNo;
        this.StopNo = stopNo;
    }

    @Override
    public void run(SourceContext<SnapShot> ctx) throws Exception {
        int currentIndex = 0;
        System.out.println("Start reading snapshots from the directory.");

        this.hdfs = FileSystem.get(new URI(hdfs_uri));
        if (hdfs != null) {
            FileStatus[] snapshotFiles = hdfs.listStatus(new Path(rootDir));
//            System.out.println(snapshotFiles.length);
            if (snapshotFiles != null) {
                snapshotFilePaths = new Path[snapshotFiles.length];

                for (int i = 0; i < snapshotFiles.length; i++) {
                    snapshotFilePaths[i] = snapshotFiles[i].getPath();
//                    System.out.println(snapshotFiles[i].getPath());
                }
            } else {
                System.out.println("No snapshot files found in the directory.");
            }
        } else {
            System.out.println("Directory not found or not a valid directory.");
        }

//        System.out.println(System.currentTimeMillis());
        if(isTest) {
            String currentFilePath = this.rootDir + "/time_" + this.testTime + ".plt";
            SnapShot snapshot = readSnapshotFromFile(currentFilePath); // Implement this function

            System.out.println(currentFilePath+"包含"+snapshot.getObjects().size()+"个轨迹点");

            if (snapshot != null) {
                ctx.collect(snapshot); // Send snapshot to the Flink stream
            }
            Thread.sleep(this.tranGap); // Adjust the delay between reading snapshots

        }else{
            //        if(true)
            int div = 0;
            int istart = 0;
            int istop = snapshotFilePaths.length;
            if(isConstrainTime == 1) {
                istart = StartNo;
                istop = StopNo;
            }
            for(int i = istart; i < istop; i++){
                i+=div;
                String currentFilePath = this.rootDir + "/time_" + i +".plt";

                Path filePath = new Path(currentFilePath);
                if (!hdfs.exists(filePath)) {
                    div++;
                    System.out.println("File " + currentFilePath + " does not exist.");
                    continue;
                }

                SnapShot snapshot = readSnapshotFromFile(currentFilePath); // Implement this function
                System.out.println(currentFilePath+"包含"+snapshot.getObjects().size()+"个轨迹点");

//                System.out.println("/time_" + Integer.toString(i)+".plt);
                if (snapshot != null) {
                    ctx.collect(snapshot); // Send snapshot to the Flink stream
                }
                Thread.sleep(this.tranGap); // Adjust the delay between reading snapshots
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

    // Read the snapshot from the file and return it
    private SnapShot readSnapshotFromFile(String snapShotFilePath) {
        try {
            String[] pathArr = snapShotFilePath.split("/");
            int timeID = Integer.parseInt(pathArr[pathArr.length - 1].split("_")[1].split("\\.")[0]);
            SnapShot snapShotRet = new SnapShot(timeID);
//            FileSystem hdfs  = FileSystem.get(new URI(snapShotFilePath));
            Path snapshotPatHDFS = new Path(snapShotFilePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(hdfs.open(snapshotPatHDFS)));
            try {
                String line;
                String[] par;
                while ((line = reader.readLine()) != null) {
                    par = line.split("\t");
//                    System.out.println(par);
//                    System.out.println("par[0]:"+par[0]+" par[1]:"+par[1]+" par[2]:"+par[2]);
                    int object_id = Integer.parseInt(par[0]);
                    if(object_id % 2 ==0){
                        continue;
                    }
                    Point newpoint = new Point(
                            object_id,
                            Double.parseDouble(par[1])*Math.cos(Math.toRadians(Double.parseDouble(par[2])))*111000,
                            Double.parseDouble(par[2])*111000,
                            Double.parseDouble(par[1]),
                            Double.parseDouble(par[2])
//                            ,
//                            Double.parseDouble(par[1]),
//                            Double.parseDouble(par[2])
                    );
//                    System.out.println(par[3]);
                    newpoint.setDirection(par[3]);
                    newpoint.setSpeed(Double.parseDouble(par[4]));

                    snapShotRet.addObject(object_id, newpoint);

                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (snapShotRet.getObjects() != null)
                return snapShotRet;
            else
                return null;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }




}
