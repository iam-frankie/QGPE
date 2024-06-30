//package Input;
//import Model.SnapShot;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//
//import java.sql.Array;
//
//public class SnapshotProcessing {
//    public static void main(String[] args) throws Exception {
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//
//        String[] snapshotFilePath = new String[1];
//        DataStream<SnapShot> snapshotStream = env.addSource(new LocalSnapshotSource(snapshotFilePath));
//
//        // Define your data processing logic here using the snapshotStream
//        // For example, you can perform clustering operations on the snapshotStream
//
//        // Execute the Flink job
//        env.execute("Snapshot Processing");
//    }
//}
