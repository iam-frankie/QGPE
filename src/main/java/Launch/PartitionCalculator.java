package Launch;
import org.apache.flink.api.common.functions.MapFunction;
import Model.SnapShot;

public class PartitionCalculator implements MapFunction<SnapShot, Integer> {
    private int basePartitionSize;

    public PartitionCalculator(int basePartitionSize) {
        this.basePartitionSize = basePartitionSize;
    }

    @Override
    public Integer map(SnapShot snapShot) throws Exception {
        int partitionNum = (int) Math.ceil((double) snapShot.getPoints().size() / basePartitionSize);
        // 确保分区数是2的倍数
        if (partitionNum % 2 != 0) {
            partitionNum++;
        }
        return partitionNum;
    }
}
