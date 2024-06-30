package Launch;

import Model.SnapShot;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;


public class MyTimestampExtractor implements AssignerWithPunctuatedWatermarks<SnapShot> {

//
//    public MyTimestampExtractor() {
//        super(Time.seconds(1));  // 假设最大乱序时间为1秒
//    }

//    @Override
//    public long extractTimestamp(SnapShot snapShot) {
//
//    }
/*
* 在Flink中，时间戳通常以毫秒为单位的长整型（long）表示。
* 这是自1970年1月1日00:00:00 GMT以来的毫秒数。
* 如果你的时间戳不是这种格式，你需要将其转换为这种格式。
*  */

    public Watermark checkAndGetNextWatermark(SnapShot lastElement, long extractedTimestamp) {
        // 可以在这里实现水印生成逻辑，如果不需要生成水印可以返回 null
        // 例如：每隔一定时间生成一个水印
        long watermarkTimestamp = extractedTimestamp - 1; // 比事件时间提前一毫秒
        return new Watermark(watermarkTimestamp);
    }

    @Override
    public long extractTimestamp(SnapShot element, long recordTimestamp) {
        // 从 SnapShot 对象中提取事件时间戳字段
        long timestamp = element.getTS(); // 这里假设 timestamp 字段用于表示事件时间
        System.out.println("TimeStamps: " + timestamp);
        return timestamp*1000;
    }
}



//public class MyTimestampExtractor extends BoundedOutOfOrdernessTimestampExtractor<SnapShot> {
//
//    public MyTimestampExtractor() {
//        super(Time.seconds(1));  // 假设最大乱序时间为1秒
//    }
//
//    @Override
//    public long extractTimestamp(SnapShot snapShot) {
//        // 从 SnapShot 对象中提取事件时间戳字段
//        long timestamp = snapShot.getTS(); // 这里假设 timestamp 字段用于表示事件时间
//        return timestamp;
//    }
//}