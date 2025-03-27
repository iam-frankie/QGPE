package PatternEnumerationBit;


import java.io.Serializable;
import java.util.*;

public class PatternEnumeration implements Serializable {
    private final int K;  // 模式中的持续时间
    private final int L;  // 模式中的段连续时间
    private final int G;  // 模式中的段间间隔
    private final int M;  // 模式中的人数
    private final int eta;  // 模式中的最长长度
//    Map<Long, Pto> ptoHashMap;
    private long t;

    public PatternEnumeration (int K, int L, int G, int M, int eta) {
        this.K = K;
        this.L = L;
        this.G = G;
        this.M = M;
        this.eta = eta;
    }

    // Baseline 算法的主要实现
    public List<Pattern> baselineAlgorithm(List<Pto> elements, long start, long end) {
        List<Pattern> patterns = new ArrayList<>();
        this.t = (int) elements.get(0).getTime();

        // 初始化所有可能的轨迹点组合
//        Set<Long> allobject = ptoHashMap.get(t).getObjectsID();
        int maxobject = elements.get(0).getObjectsID().size();
        List<Set<Long>> candidates = enumerateSubsets(elements.get(0).getObjectsID(), maxobject, maxobject, true);
        System.out.println("候选模式大小:" + candidates.size() + "   最大人数:" + maxobject);

//        for(Set<Long> h: candidates) {
        Set<Integer> overlook = new HashSet<>();
        for(int i = 0; i < candidates.size(); i++) {
            if(overlook.contains(i))
                continue;
            Set<Long> h = candidates.get(i);

            Pattern pattern = new Pattern(h,true);
            pattern.addTimeSeries((int) this.t);

            for(Pto Pio: elements.subList(1, elements.size())){

                if((Pio.getObjectsID().containsAll(pattern.getObjectsID()) && (Pio.getTime()-pattern.getEndTime()==1) )||
                        (Pio.getObjectsID().containsAll(pattern.getObjectsID()) && pattern.getTl() >= L && (Pio.getTime()-pattern.getEndTime() <= G)
                        )){
                    // 两种情况更新模式
                    // 1. 模式中的对象在当前时间点都存在，且当前时间点与模式中最后一个时间点相差1
                    // 2. 模式中的对象在当前时间点都存在，且当前时间点与模式中最后一个时间点相差小于G
//                    pattern.setEndTime(Pio.getTime());
                    pattern.addTimeSeries((int)Pio.getTime());
                    if(pattern.getaccDuration() >= K && pattern.getTl() >= L) {
                        patterns.add(pattern);
                        break;
                    }
                }else
                    overlook.add(i);
            }
        }
        return patterns;
    }


    public List<Pattern> FBA(List<Pto> elements, long mint, long maxt) {
        long startTime = System.currentTimeMillis();

        this.t = (int) elements.get(0).getTime();

        Map<Long, BitSet> B = new HashMap<>();
        Set<Long> C = new HashSet<>();
        List<Pattern> result = new ArrayList<>();

        List<Long> timeSeries = new ArrayList<>();

        for(Long OiID : elements.get(0).getObjectsID()) {
            BitSet Boi = new BitSet(eta);
            for(Pto pjo: elements){
                timeSeries.add(pjo.getTime());

                if(pjo.getObjectsID().contains(OiID)) {
                    Boi.set((int) (pjo.getTime() -t));
                }
            }

            if (isValidBitSet(Boi)) {
                C.add(OiID);
            }
            B.put(OiID, Boi);

            if (System.currentTimeMillis() - startTime > 200) {
                return result;
            }
        }

        List<Set<Long>> S = enumerateSubsets(elements.iterator().next().getObjectsID(), M-1, M-1, true);
        int S_level = M-1;
        while(!S.isEmpty() && S_level <= C.size()){
            List<Set<Long>> Sa = new ArrayList<>();
            Set<UniqueSet> candidatePatterns = new HashSet<>();
            for(Set<Long> h: S) {
                for(Long OiID: C) {
                    UniqueSet h1 = new UniqueSet(h);
                    h1.add(OiID);
                    if(h1.size()>h.size())
                        candidatePatterns.add(h1);
                }
            }
            S_level++;
            for(Set<Long> candidatePattern: candidatePatterns){
                Iterator<Long> citerator = candidatePattern.iterator();
                BitSet BO = B.get(citerator.next());
                while(citerator.hasNext()){
                    BO.and(B.get(citerator.next()));
                }
                if(isValidBitSet(BO)){
                    Pattern pttr = new Pattern(candidatePattern, true);
                    pttr.addTimeSeries(timeSeries);
                    result.add(pttr);

                    Sa.add(candidatePattern);
                }

                if (System.currentTimeMillis() - startTime > 300) {
                    return result;
                }
            }
            S = Sa;
        }
        return result;
    }
//    public List<Pattern> FBA(List<Pto> elements, long mint, long maxt) {
//        this.t = (int) elements.get(0).getTime();
//
////        System.out.println(elementsSize+"-"+maxTime+"-"+t);
//        Map<Long, BitSet> B = new HashMap<>();
//        Set<Long> C = new HashSet<>();
//        List<Pattern> result = new ArrayList<>();
//
//        List<Long> timeSeries = new ArrayList<>();
//
//        // 2. 构建对象比特字符串
//        for(Long OiID : elements.get(0).getObjectsID()) {
//            BitSet Boi = new BitSet(eta);
//            for(Pto pjo: elements){
//                timeSeries.add(pjo.getTime());
//
//                if(pjo.getObjectsID().contains(OiID)) {
//                    Boi.set((int) (pjo.getTime() -t)); // 从0开始计数
//                }
//            }
//
//            // Step 2: Check if B[oi] satisfies (K, L, G) constraints
//            if (isValidBitSet(Boi)) {
//                C.add(OiID); // Add to the candidate list
//            }
//            B.put(OiID, Boi);
//        }
//
////        List<Set<Long>> S = enumerateSubsets(C, M-1, M-1, true);
//        List<Set<Long>> S = enumerateSubsets(elements.iterator().next().getObjectsID(), M-1, M-1, true);
////        System.out.println("当前模式数量" + S.size() + " " + "待选物体ID" +C.size());
//        int S_level = M-1;
//        while(!S.isEmpty() && S_level <= C.size()){
//            List<Set<Long>> Sa = new ArrayList<>();
//            Set<UniqueSet> candidatePatterns = new HashSet<>();
//            // 将S和C中的对象进行组合
//            for(Set<Long> h: S) {
//                for(Long OiID: C) {
//                    UniqueSet h1 = new UniqueSet(h);
//                    h1.add(OiID);
//                    if(h1.size()>h.size())
//                        candidatePatterns.add(h1);
//                }
//            }
//            S_level++;
//            for(Set<Long> candidatePattern: candidatePatterns){
//                // BO = B[O1] And B[O2] And ... And B[Om]
//                Iterator<Long> citerator = candidatePattern.iterator();
//                BitSet BO = B.get(citerator.next());
//                while(citerator.hasNext()){
//                    BO.and(B.get(citerator.next()));
//                }
//                if(isValidBitSet(BO)){
//                    Pattern pttr = new Pattern(candidatePattern, true);
//                    pttr.addTimeSeries(timeSeries);
//                    result.add(pttr);
//
//                    Sa.add(candidatePattern);
//                }
//            }
//            S = Sa;
//        }
//        return result;
//    }




//    boolean satisfies(BitSet bitset) {
//
//        // 1. 检查最小持续时间K
//        int duration = computeK(bitset);
//        if(duration < K) {
//            return false;
//        }
//
//        // 2. 检查最小连续段长度L
//        int longestConsec = computeL(bitset);
//        if(longestConsec < L) {
//            return false;
//        }
//
//        // 3. 检查最大间隙长度G
//        int longestGap = computeG(bitset);
//        if(longestGap > G) {
//            return false;
//        }
//
//        // 4. 全部符合要求
//        return true;
//
//    }


    /**
     * @param objects
     * @param minSize
     * @return
     */
//    public List<Set<GridObject>> enumerateSubsets(Set<GridObject> objects, int minSize, int maxSize) {
//        List<Set<GridObject>> subsets = new ArrayList<>();
//        int n = objects.size();
//
//        // 获取对象列表
//        List<GridObject> objectList = new ArrayList<>(objects);
//
//        for (int i = 0; i < (1 << n); i++) {
//            Set<GridObject> subset = new HashSet<>();
//            for (int j = 0; j < n; j++) {
//                if ((i & (1 << j)) != 0) {
//                    subset.add(objectList.get(j));
//                }
//            }
//            if(subset.size() >= minSize && subset.size() <= maxSize)
//                subsets.add(subset);
//        }
//        return subsets;
//    }


    public List<Set<Long>> enumerateSubsets(Set<Long> objects, int minSize, int maxSize, Boolean isID) {
        List<Set<Long>> subsets = new ArrayList<>();
        int n = objects.size();

        // 获取对象列表
        List<Long> objectList = new ArrayList<>(objects);

        for (int i = 0; i < (1 << n); i++) {
            Set<Long> subset = new HashSet<>();
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
