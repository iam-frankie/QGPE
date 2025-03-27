package PatternEnumerationBit;

import java.math.BigInteger;
import java.util.*;

public class Test {
    public static BigInteger binomial(int n, int k) {
        BigInteger result = BigInteger.ONE;

        for (int i = 0; i < k; i++) {
            result = result.multiply(BigInteger.valueOf(n - i))
                    .divide(BigInteger.valueOf(i + 1));
        }

        return result;
    }

    public static void main(String[] args) {
        VariablePatternEnumeration2 vpe = new VariablePatternEnumeration2(0, 0, 0, 0, 0);
        PatternEnumeration pe = new PatternEnumeration(0, 0, 0, 0, 0);

        // todo 1、子集枚举生成测试
        Set<Long> objects = new HashSet<>(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        int minSize = 9;
        int maxSize = 21;


//        List<Set<Long>> subsets = vpe.enumerateSubsets(objects, minSize, maxSize);
//        List<Set<Long>> subsets = pe.enumerateSubsets(objects, minSize, maxSize, true);
//        for (Set<Long> subset : subsets) {
//            System.out.println(subset);
//        }


        int n = 21;
//        int minSize = 9;
//        int maxSize = 18;
        BigInteger total = BigInteger.ZERO;

        for (int k = minSize; k <= maxSize; k++) {
            total = total.add(binomial(n, k));
        }

        System.out.println("Total number of subsets: " + total);







        // todo 2、KLG计算测试
        VariableBitSet variableBitSet = new VariableBitSet(2, 10);
        variableBitSet.insert(1, false);
        variableBitSet.insert(2, true);
        variableBitSet.insert(3, true);
        variableBitSet.insert(4, true);

        variableBitSet.insert(7, true);
        variableBitSet.insert(8, true);
        variableBitSet.insert(9, true);


        VariableBitSet variableBitSet2 = new VariableBitSet(2, 10);
        variableBitSet2.insert(1, true);
        variableBitSet2.insert(2, true);
        variableBitSet2.insert(3, true);
        variableBitSet2.insert(4, true);

//        variableBitSet2.insert(7, true);
        variableBitSet2.insert(8, true);
        variableBitSet2.insert(9, true);

        int Gresult = VariablePatternEnumeration2.computeG(variableBitSet, 2, 10);
        int Kresult = VariablePatternEnumeration2.computeK(variableBitSet, 2, 10);
        int Lresult = VariablePatternEnumeration2.computeL(variableBitSet, 2, 10);
//        System.out.println("G: " + Gresult + " K: " + Kresult + " L: " + Lresult);
//        System.out.println(variableBitSet.getStartTime() + "-" + variableBitSet.getEndTime());
/*        System.out.println(variableBitSet.getBits());
//
        System.out.println(variableBitSet2.getBits());

        variableBitSet2.AND(variableBitSet, 10);
        System.out.println(variableBitSet2.getBits());*/



        // todo 3、子集枚举生成测试

        BitSet bitSet = new BitSet();
        bitSet.set(2);
        bitSet.set(3);
        bitSet.set(4);
        bitSet.set(7);
        bitSet.set(8);
        bitSet.set(9);

        int resultK = PatternEnumeration.computeK(bitSet);


        int resultL = PatternEnumeration.computeL(bitSet);


        int resultG = PatternEnumeration.computeG(bitSet);

//        System.out.println("G: " + resultG + " K: " + resultK + " L: " + resultL);
    }
}