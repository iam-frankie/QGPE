package ClusteringBase;

import Model.SnapShot;
import Model.SnapshotClusters;
import Model.SimpleCluster;

import org.apache.flink.api.common.functions.MapFunction;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import scala.Tuple2;
import ClusteringBase.DBSCANClustering;
import java.util.ArrayList;

public class DBSCANWrapper implements
        MapFunction<Tuple2<Integer, SnapShot>, SnapshotClusters> {
    private static final long serialVersionUID = 3562163124094087749L;
    private double eps;
    private int M, minPts;
    private int earth;

    public DBSCANWrapper(double ieps, int iminPts, int m, int earth) {
        eps = ieps;
        minPts = iminPts;
        M = m;
        this.earth = earth;
    }

    @Override
    public SnapshotClusters map(Tuple2<Integer, SnapShot> v1) throws Exception {
        // todo 开始计时
        long time_start = System.currentTimeMillis();

        DBSCANClustering dbscanTask = new DBSCANClustering(eps, minPts, v1._2, earth);
        ArrayList<SimpleCluster> clusters = dbscanTask.cluster();
        SnapshotClusters result = new SnapshotClusters(v1._1);
        for (SimpleCluster cluster : clusters) {
//            System.out.println("簇ID:" + cluster.getID() + "  " + "簇大小:" + cluster.getObjects().size()+"簇的物体集"+cluster.getObjects().toString());

            if (cluster.getObjects().size() >= M) {
                SimpleCluster simplecluster = new SimpleCluster();
                simplecluster.addObjects(cluster.getObjects());
                simplecluster.setID(cluster.getID());
                result.addCluster(simplecluster);
//                System.out.println(cluster.getObjects().size());
            }
        }
        long time_end = System.currentTimeMillis();
        int count = 0;
        for (SimpleCluster cluster : result.getClusters()) {
            if (cluster.getObjects().size() >= 20) {
                count++;
            }
            System.out.println("簇ID:" + cluster.getID() + "  " + "簇大小:" + cluster.getObjects().size()+"簇的物体集"+cluster.getObjects().toString());
        }
        System.out.println("大于20的簇的数量:" + count);

        // todo 结束计时
        // remove when actual deploy
        System.out.println("人数:" +v1._2.getObjects().size() + "  " + "快照聚类耗时:" + (time_end - time_start)
                + " ms" + "\t" + "簇的数量:" + result.getClusterSize());
        return result;
    }

//    public Tuple2<Integer, SnapShot> revert(SnapShot snapShot) throws Exception {
//        return new Tuple2<>(snapShot.getTS(), snapShot);
//    }

//    @Override
//    public TypeInformation<SnapshotClusters> getProducedType() {
//        return TypeInformation.of(new TypeHint<SnapshotClusters>() {});
//    }
}
