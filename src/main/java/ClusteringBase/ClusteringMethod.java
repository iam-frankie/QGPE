package ClusteringBase;

import Model.SnapShot;
import org.apache.flink.api.java.DataSet;
import Model.SnapshotClusters;
import org.apache.flink.streaming.api.datastream.DataStream;

public interface ClusteringMethod {
    DataStream<SnapshotClusters> doClustering(DataStream<SnapShot> input);

    DataSet<SnapshotClusters> doClustering(DataSet<SnapShot> input);
}
