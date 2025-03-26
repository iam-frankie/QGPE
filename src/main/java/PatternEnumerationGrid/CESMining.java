package PatternEnumerationGrid;

import ClusteringKD.Cluster;
import PatternEnumerationBit.Pattern;
import PatternEnumerationBit.Pto;
import PatternEnumerationBit.UniqueSet;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import java.util.*;


public class CESMining implements FlatMapFunction<SubMap, Set<Set<Integer>>> {
    private int M;
    private int K;
    private int L;
    private int G;
    private long t;
    private int eta;

    public CESMining(int eta, int M, int K, int L, int G) {
        this.M = M;
        this.K = K;
        this.L = L;
        this.G = G;
        this.eta = eta;
    }


    @Override
    public void flatMap(SubMap ces, Collector<Set<Set<Integer>>> out) throws Exception {
        long startTime = System.currentTimeMillis(); // 获取开始时间
        Set<Set<Integer>> patterns = new HashSet<>();
        this.t = ces.getTimestamp()-eta+1;

        // 1. 枚举子集
        int maxobjectsize = ces.getSubMap().size();
        List<Set<Integer>> S = enumerateSubsets(ces.getSubMap().keySet(), M-1, M-1, true);
        // 2. 建立位字符串
        Map<Integer, BitSet> B = new HashMap<>();
        Set<Integer> C = new HashSet<>();

        for(int OiID : ces.getSubMap().keySet()) {
            BitSet Boi = new BitSet(eta);
            for(Long timeStamp: ces.getSubMap().get(OiID)){
                Boi.set((int) (timeStamp -t+1)); // 从0开始计数
            }
            if (isValidBitSet(Boi)) {
                C.add(OiID); // Add to the candidate list
            }
            B.put(OiID, Boi);
        }

        // 3. 枚举子集
        int S_level = M-1;
        while(!S.isEmpty() && S_level <= C.size()){
            List<Set<Integer>> Sa = new ArrayList<>();
            Set<Set<Integer>> candidatePatterns = new HashSet<>();
            // 将S和C中的对象进行组合
            for(Set<Integer> h: S) {
                for(Integer OiID: C) {
                    Set<Integer> h1 = new HashSet<>(h);
                    h1.add(OiID);
                    if(h1.size()>h.size())
                        candidatePatterns.add(h1);
                }
            }
            S_level++;
            for(Set<Integer> candidatePattern: candidatePatterns){
                // BO = B[O1] And B[O2] And ... And B[Om]
                Iterator<Integer> citerator = candidatePattern.iterator();
                BitSet BO = B.get(citerator.next());
                while(citerator.hasNext()){
                    BO.and(B.get(citerator.next()));
                }
                if(isValidBitSet(BO)){
                    patterns.add(candidatePattern);
                    Sa.add(candidatePattern);
                }
            }
            S = Sa;



        }

        if(!patterns.isEmpty()) {
            System.out.println(patterns);
        }
        out.collect(patterns);
    }

    public List<Set<Integer>> enumerateSubsets(Set<Integer> objects, int minSize, int maxSize, Boolean isID) {
        List<Set<Integer>> subsets = new ArrayList<>();
        int n = objects.size();

        // 获取对象列表
        List<Integer> objectList = new ArrayList<>(objects);

        for (int i = 0; i < (1 << n); i++) {
            Set<Integer> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    subset.add(objectList.get(j));
                }
            }
            if(subset.size() >= minSize && subset.size() <= maxSize)
                subsets.add(subset);
        }
        return subsets;
    }


    // 判断比特向量是否有效
    boolean isValidBitSet(BitSet bitset) {

        // 1. 检查对象数目
//        if(bitset.cardinality() < M) {
//            return false;
//        }

        // 2. 检查持续时间
        int duration = computeK(bitset);
        if(duration < K) {
            return false;
        }

        // 3. 检查连续性
        int consec = computeL(bitset);
        if(consec < L) {
            return false;
        }

        // 4. 检查间隙阈值
        int gap = computeG(bitset);
        if(gap > G) {
            return false;
        }

        // 5. 全部校验通过,有效
        return true;

    }
    public static int computeK(BitSet bitset) {
        int ans = 0;
        for(int i = 0; i < bitset.length(); i++) {
            if(bitset.get(i)) {
                ans++;
            }
        }
        return ans;
    }



    // 计算最短连续段
    public static int computeL(BitSet bitset) {
        int shortest = Integer.MAX_VALUE; // 用于存储最短连续1序列的长度
        int current = 0; // 当前连续1序列的长度

        for (int i = 0; i < bitset.length(); i++) {
            if (bitset.get(i)) { // 如果当前位是1
                current++;
                if (i == bitset.length() - 1 || !bitset.get(i + 1)) {
                    shortest = Math.min(shortest, current);
                    current = 0; // 重置当前连续1序列的长度
                }
            }
        }

        return shortest == Integer.MAX_VALUE ? 0 : shortest;
    }


    // 计算最长间隙
    public static int computeG(BitSet bitset) {
        int longestGap = 0; // 用于存储最长的连续0序列长度
        int currentGap = 0; // 当前连续0序列的长度

        for (int i = 0; i < bitset.length(); i++) {
            if (!bitset.get(i)) { // 如果当前位是0
                currentGap++;
            } else {
                longestGap = Math.max(longestGap, currentGap);
                currentGap = 0; // 重置当前连续0序列的长度
            }
        }

        return longestGap+1;
    }
}
