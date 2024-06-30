//package Tools;
//import org.apache.flink.streaming.api.functions.sink.SinkFunction;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.net.Socket;
//import com.google.gson.Gson;
//
//// ...
//import Model.SnapshotClusters;
//// ...
//
//public class PythonSink implements SinkFunction<SnapshotClusters> {
//    private transient Socket socket;
//    private transient PrintWriter writer;
//
//    private String convertToJson(SnapshotClusters snapshotClusters) {
//        Gson gson = new Gson();
//        return gson.toJson(snapshotClusters);
//    }
//
//    @Override
//    public void open() throws Exception {
//        socket = new Socket("localhost", 9999);  // Change to your Python script's host and port
//        OutputStream os = socket.getOutputStream();
//        writer = new PrintWriter(new OutputStreamWriter(os));
//    }
//
//    @Override
//    public void invoke(SnapshotClusters value, Context context) {
//        String json = convertToJson(value);
//        sendToPython(json);
//    }
//
//    private void sendToPython(String message) {
//        writer.println(message);
//        writer.flush();
//    }
//
//    @Override
//    public void close() {
//        if (writer != null) {
//            writer.close();
//        }
//        if (socket != null) {
//            try {
//                socket.close();
//            } catch (IOException e) {
//                // Handle the exception if needed
//            }
//        }
//    }
//}
//
//
//
