//
//package PatternEnumerationBit;
//
//import java.util.BitSet;
//
//public class VariableBitSet {
//    private BitSet bits;
//    private long st;
//    private long et;
//
//    public VariableBitSet(long startTime, long endTime) {
//        bits = new BitSet((int) (endTime - startTime));
//        st = startTime;
//        et = endTime;
//    }
//
//    public BitSet getBits() {
//        return bits;
//    }
//
//    public long getStartTime() {
//        return st;
//    }
//
//    public long getEndTime() {
//        return et;
//    }
//
//    public void insert(int index, boolean value) {
//        if (value) {
//            if (et <= index) {
//                for (long i = et; i <= index; i++) {
//                    bits.set((int) (i - st), false);
//                }
//                bits.set((int) (index - st), value);
//                et = index + 1;
//            } else if (st > index) {
//                throw new IllegalArgumentException("Index out of range");
//            } else {
//                bits.set((int) (index - st), value);
//            }
//        }
//    }
//
//    public void add(boolean value) {
//        if (value) {
//            bits.set((int) (et - st), value);
//            et++;
//        }
//    }
//
//    public boolean get(int index) {
//        if (index >= st && index < et) {
//            return bits.get((int) (index - st));
//        } else {
//            return false;
//        }
//    }
//
//    public int size() {
//        return (int) (et - st);
//    }
//
//    public void AND(VariableBitSet other, long nowtime) {
//        long start = Math.max(this.st, other.st);
//        long end = Math.min(this.et, other.et);
//
//        for (long i = start; i < end; i++) {
//            int thisIndex = (int) i;
//            int otherIndex = (int) (i);
//            boolean value = this.get(thisIndex) && other.get(otherIndex);
//            this.insert(thisIndex, value);
//        }
//
//        if (start > this.st && start <= this.et) {
//            for (long i = this.st; i < start; i++) {
//                bits.clear((int) (i - this.st));
//            }
//            this.st = start;
//        }
//        if (end < this.et && end >= this.st) {
//            for (long i = end; i < this.et; i++) {
//                bits.clear((int) (i - this.st));
//            }
//            this.et = end;
//        }
//    }
//}

package PatternEnumerationBit;

import java.util.ArrayList;

public class VariableBitSet {
    private ArrayList<Boolean> bits;
    private long st;
    private long et;
    //    private List<Long> VobjectList;
//  此处前闭后开，即[st, et)
    public VariableBitSet(long startTime, long endTime) {
        bits = new ArrayList<>();
        st = startTime;
        et = endTime;
//        this.VobjectList = objectList;
        for(long i = startTime; i < endTime; i++){
            bits.add(false);
        }
    }

    public ArrayList<Boolean> getBits() {
        return bits;
    }

    public long getStartTime() {
        return st;
    }

    public long getEndTime() {
        return et;
    }


    public void insert(int index, boolean value) {
        if(value){
            if(et <= index){
                for (long i = et; i <= index; i++) {
                    bits.add(false);
                }
                bits.set((int) (index-st), value);
                et = index + 1;
            }
            else if(st > index){
                for (long i = st; i > index; i--) {
                    bits.add(0, false);
                }
                bits.set(0, value);
                st = index;
            }
            else{
                bits.set((int) (index-st), value);
            }
            if(bits.size() > 400){
                st+=200;
                bits.subList(0, 200).clear();
            }
        }

    }

    public void add(boolean value) {
        if(value) {
            bits.add(value);
            et++;
            if (bits.size() > 400) {
                st += 200;
                bits.subList(0, 200).clear();
            }
        }
    }

    public boolean get(int index) {
        if (index >= st && index < et) {
            return bits.get((int) (index-st));
        }
        else return false; // 超出范围的索引返回false
    }

    public int size() {
        return (int) (et-st);
    }

    public void AND(VariableBitSet other, long nowtime) {
        long start = Math.max(this.st, other.st);
        long end = Math.min(this.et, other.et);

        for (long i = start; i < end; i++) {
            int thisIndex = (int) i;
            int otherIndex = (int) (i);
            boolean value = this.get(thisIndex) && other.get(otherIndex);
//            System.out.println(thisIndex + " " +otherIndex + " " +this.get(thisIndex) + " " + other.get(otherIndex) +" " +value);
            this.insert(thisIndex, value);
        }
//        System.out.println(this.getBits());

        if (start > this.st && start <= this.et) {
            bits.subList(0, (int) (start - this.st)).clear();
            this.st = start;
        }
        if (end < this.et && end >= this.st) {
            bits.subList((int) (end - this.st), bits.size()).clear();
            this.et = end;
        }
    }




}
