package Tools;

import java.io.File;
import java.io.FileWriter;

public class FileUtil {

    public static void writeFile(String fileName, String content) throws Exception {

        File file = new File(fileName);

        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();

    }

}