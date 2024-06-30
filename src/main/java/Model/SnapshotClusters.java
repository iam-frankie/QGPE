package Model;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.Serializable;

public class SnapshotClusters implements Serializable {
    private static final long serialVersionUID = 9162568949845610013L;
    private int timestamp;
    //    private ArrayList<SimpleCluster> clusters;
    private ObjectArrayList<SimpleCluster> clusters;

    /**
     * create an empty snapshot with a time sequence
     * @param time
     */
    public SnapshotClusters(int time){
        timestamp = time;
//	clusters = new ArrayList<>();
        clusters = new ObjectArrayList<>();
    }

    public Iterable<SimpleCluster> getClusters() {
        return clusters;
    }

    public SimpleCluster getClusterAt(int index) {
        return clusters.get(index);
    }

    public int getTimeStamp() {
        return timestamp;
    }

    public int getClusterSize() {
        return clusters.size();
    }

    public void addCluster(SimpleCluster sc) {
        clusters.add(sc);
    }

    @Override
    public String toString() {
        return "<"+ timestamp + ":"+ clusters+ ">";
    }
}
