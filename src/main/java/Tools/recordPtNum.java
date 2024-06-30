package Tools;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class recordPtNum {
    public static void writeRes(Map<String, Set<Integer>> neighborIdsMap) throws Exception {
        String hdfsUri = "hdfs://amax:9000";
        String hdfsPath = "hdfs://amax:9000//comovement/result/pt.json";

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
}
