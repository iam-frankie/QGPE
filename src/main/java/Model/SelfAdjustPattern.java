package Model;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 *  Created by Frankie on 2023.8.29
 *  实现了一个名为SelfAdjustPattern的类，用于构建和管理一种自适应模式。
 *  这个模式在时间维度上不断调整以保持有效的时间范围
 */

public class SelfAdjustPattern {
    private int M, L, K, G;
    private IntSet objects;
    private ArrayList<Integer> timestamps;
    private boolean isTemporalValid;
    private int lastConsecutiveStart;

    public SelfAdjustPattern(int m, int l, int k, int g) {
        objects = new IntOpenHashSet();
        timestamps = new ArrayList<>();
        isTemporalValid = true;
        M = m;
        L = l;
        K = k;
        G = g;
        lastConsecutiveStart = -1;
    }

    public void addObjects(Collection<Integer> objects) {
        this.objects.addAll(objects);
        if (this.objects.size() < M) {
            isTemporalValid = false;
        }
    }

    public IntSet getObjects() {
        return objects;
    }

    public ArrayList<Integer> getTimestamps() {
        return timestamps;
    }

    public boolean growTemporal(int ts) {
        if (timestamps.isEmpty()) {
            timestamps.add(ts);
            lastConsecutiveStart = 0;
            return true;
        } else {
            int nextIndex = timestamps.size();
            int prev = timestamps.get(nextIndex - 1);
            if (ts - prev > G) {
                return false;
            } else if (ts - prev != 1) {
                int prevConsecutive = nextIndex - lastConsecutiveStart;
                if (prevConsecutive >= L) {
                    timestamps.add(ts);
                    lastConsecutiveStart = nextIndex;
                    return true;
                } else {
                    ArrayList<Integer> newStamps = new ArrayList<>();
                    for (int i = 0; i < lastConsecutiveStart; i++) {
                        newStamps.add(timestamps.get(i));
                    }
                    if (newStamps.isEmpty() || ts - newStamps.get(newStamps.size() - 1) <= G) {
                        newStamps.add(ts);
                        timestamps = newStamps;
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                timestamps.add(ts);
                return true;
            }
        }
    }

    public void clearTemporals() {
        timestamps.clear();
        lastConsecutiveStart = -1;
    }

    @Override
    public String toString() {
        return String.format("<%s x %s>", timestamps, objects);
    }

    public void addTemporals(ArrayList<Integer> tstamps2) {
        for (int i : tstamps2) {
            this.growTemporal(i);
        }
    }

    public boolean checkParitialValidity() {
        return isTemporalValid;
    }

    public boolean checkFullValidity() {
        boolean temporalValid = true;
        int lastTS = getLatestTS();
        if (lastTS - lastConsecutiveStart + 1 < L) {
            temporalValid = false;
        }
        if (timestamps.size() < K) {
            temporalValid = false;
        }
        return isTemporalValid && temporalValid;
    }

    public static void main(String[] args) {
        int K = 6;
        int M = 1;
        int G = 4;
        int L = 2;
        ArrayList<Integer> objects = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        SelfAdjustPattern p = new SelfAdjustPattern(M, L, K, G);
        p.addObjects(objects);
        int[] temporals = new int[] { 0, 2, 3, 4, 7, 8, 9, 11, 13, 14, 15 };
        for (int i : temporals) {
            System.out.print(p.growTemporal(i) + "\t");
            System.out.println(p);
        }
    }

    public int getLatestTS() {
        if (timestamps.isEmpty()) {
            return -1;
        }
        return timestamps.get(timestamps.size() - 1);
    }

    public int getEarliestTS() {
        if (timestamps.isEmpty()) {
            return -1;
        } else {
            return timestamps.get(0);
        }
    }
}

