package Launch;
import org.apache.flink.api.common.functions.MapFunction;
import Model.SnapShot;
/*
首先，我们需要创建一个函数来计算分区数。这个函数将根据数据量动态调整分区数。
我们可以设定一个基础分区数，然后根据数据量的大小进行调整。
例如，我们可以设定每1000个数据点为一个基础分区，然后根据数据量的大小进行调整。同时，我们需要确保分区数是2的倍数。
 */

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