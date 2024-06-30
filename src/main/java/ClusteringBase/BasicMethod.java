package ClusteringBase;

import Model.SnapShot;
import Model.SnapshotClusters;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.DataSet;
import scala.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;

/**
 * A basic clustering method. Each snapshot is clustered separately.
 *
 * @author a0048267
 *
 */
public class BasicMethod implements ClusteringMethod {
    private static final long serialVersionUID = 1273022000870413977L;

    private final DBSCANWrapper dbscanwrapper;
//    private int pars;

    public BasicMethod(double eps, int minpts, int M, int earth) {
//        this.pars = pars;
        dbscanwrapper = new DBSCANWrapper(eps, minpts, M, earth);
    }

    @Override
    public DataStream<SnapshotClusters> doClustering(DataStream<SnapShot> input) {

        // MapFunction的两个参数分别是输入和输出，这里输入是SnapShot，输出是Tuple2<Integer, SnapShot>
        // DBSCAN
        DataStream<SnapshotClusters> CLUSTERS = input
                .map(new MapFunction<SnapShot, Tuple2<Integer, SnapShot>>() {
                    @Override
                    public Tuple2<Integer, SnapShot> map(SnapShot snapShot) throws Exception {
                        return new Tuple2<>(snapShot.getTS(), snapShot);
                    }
                })
                .map(dbscanwrapper)
                .filter(snapClusters -> snapClusters != null && snapClusters.getClusterSize() > 0);
//                .returns(TypeInformation.of(SnapshotClusters.class)); // Specify the return type


        return CLUSTERS;
    }

    @Override
    public DataSet<SnapshotClusters> doClustering(DataSet<SnapShot> input) {
        return null;
    }
}
