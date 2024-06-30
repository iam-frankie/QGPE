package Tools;
import PatternEnumerationBit.Pattern;
import PatternEnumerationBit.PatternEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class MyLogger implements Serializable {
    // ... 其他成员变量和方法

    private static final Logger logger = LoggerFactory.getLogger(PatternEnumeration.class);

    public void logPatterns(List<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            logger.info("Pattern Found: {}", pattern.toString());
        }
    }

    // 其他方法
}