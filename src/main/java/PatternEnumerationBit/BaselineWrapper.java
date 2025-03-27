package PatternEnumerationBit;

import Tools.loadHDFS;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import org.apache.flink.util.Collector;
import org.checkerframework.checker.units.qual.K;
import scala.collection.Iterable;

import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 *
 * PatternEnumerationBit.Pto：输入类型，这是处理函数接收的数据类型。
 * List<Pattern>：输出类型，这是处理函数输出的数据类型。
 * Long：键类型，这是用于分组数据的键的类型。
 * TimeWindow：窗口类型，这是用于定义窗口的类型。在这里，TimeWindow 表示基于时间的窗口。
 */

public class BaselineWrapper extends ProcessWindowFunction<Pto, List<Pattern>, Long, TimeWindow> {
//    private transient ValueState<Set<Long>> patternState;
//    private transient SinkFunction<String> sink;
    private int PEmode = 0;
    PatternEnumeration patternMinning;
    private int K;

    public BaselineWrapper(int K, int L, int G, int M, int SlidingWinndowSize, int PEmode) {
        this.patternMinning = new PatternEnumeration(K, L, G, M, SlidingWinndowSize);
        this.PEmode = PEmode;
        this.K = K;
    }


    @Override
    public void process(Long aLong, ProcessWindowFunction<Pto, List<Pattern>, Long, TimeWindow>.Context context, java.lang.Iterable<Pto> elements, Collector<List<Pattern>> out) throws Exception {
        long windowSize = (context.window().getEnd() - context.window().getStart())/1000;
//        System.out.println("当前窗口长度为"+windowSize);

//        if(windowSize >= K) {//        Set<Long> keys = value.keySet();
        long start = 0;
        long end = 0;

        List<Pattern> someResult = null;

        List<Pto> sortedElements = new ArrayList<>();
        elements.forEach(sortedElements::add);
        if(sortedElements.size()!= windowSize)
            return;
        // 按照时间排序
        sortedElements.sort(Comparator.comparing(Pto::getTime));
        System.out.println("分区大小"+sortedElements.size());
        System.out.println("最早时间" + sortedElements.get(0).getTime()+"最晚时间"+sortedElements.get(sortedElements.size()-1).getTime());

        if (this.PEmode == 1)
            someResult = patternMinning.baselineAlgorithm(sortedElements, start, end);
        else
            someResult = patternMinning.FBA(sortedElements, start, end);

        if (someResult.size() > 0) {
            System.out.println(someResult.size());
            for (Pattern pattern : someResult) {
//                System.out.println(pattern.objectsID);
//                patternState.update(pattern.getObjectsID());
//                Map<String, Set<Long>> patternMap = new HashMap<>();
//                patternMap.put(pattern.getStartTime() + "-" + pattern.getEndTime(), patternState.value());
////                loadHDFS.writeRes(patternMap, "3");

                String result = pattern.getStartTime() + "-" + pattern.getEndTime() + ": " + pattern.getObjectsID().toString();
                System.out.println(result);

            }

            System.out.println("************************************Patterns had updated in patternState once**********************************");
            out.collect(someResult);
        }
//        }
    }
}