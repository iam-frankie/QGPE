package Tools;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

    public static String toJSON(Object obj) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }

}