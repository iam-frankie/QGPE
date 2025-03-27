package PatternEnumerationBit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HResult {
    List<Pattern> patterns;
    Map<Long, VariableBitSet> H;

    public HResult(List<Pattern> patterns, Map<Long, VariableBitSet> H) {
        this.patterns = patterns;
        this.H = H;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public Map<Long, VariableBitSet> getH() {
        return H;
    }

}
