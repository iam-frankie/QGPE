package Model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 管理流式轨迹数据，包括滑动窗口管理、数据清理、并发控制等
 */
public class StreamingTrajectory {
    private final int windowSize;  // 滑动窗口大小
    private final List<Trajectory> trajectoryBuffer;  // 轨迹缓存
    private final ReentrantLock bufferLock;  // 缓存操作锁

    public StreamingTrajectory(int windowSize) {
        this.windowSize = windowSize;
        this.trajectoryBuffer = new ArrayList<>();
        this.bufferLock = new ReentrantLock();
    }

    /**
     * 将新快照中的轨迹加入管理器
     * @param snapshot 新快照中的轨迹列表
     */
    public void processSnapshot(List<Trajectory> snapshot) {
        bufferLock.lock();
        try {
            trajectoryBuffer.addAll(snapshot);

            // 维护滑动窗口大小
            while (trajectoryBuffer.size() > windowSize) {
                trajectoryBuffer.remove(0);
            }
        } finally {
            bufferLock.unlock();
        }
    }

    /**
     * 获取当前滑动窗口内的轨迹
     * @return 滑动窗口内的轨迹列表
     */
    public List<Trajectory> getCurrentWindow() {
        bufferLock.lock();
        try {
            return new ArrayList<>(trajectoryBuffer);
        } finally {
            bufferLock.unlock();
        }
    }

    // 其他方法，如清理过期轨迹、并发控制等，可以根据需要进行实现
}
