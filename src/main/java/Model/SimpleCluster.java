package Model;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Serializable;
import java.util.Set;



public class SimpleCluster implements Serializable{
    private static final long serialVersionUID = 7061586699944678710L;
    //use a more memory friendly implementation of integer set
    private IntOpenHashSet oids;
    //    private HashSet<Integer> oids;
    private String ID; // this ID is optionally set

    public SimpleCluster() {
//	oids = new HashSet<Integer>();
        oids = new IntOpenHashSet();
    }

    /**
     * This constructor is used for testing purpose, where
     * we can create a cluster at a specific time sequence;
     */
    public SimpleCluster(Iterable<Integer> list) {
        this();
        for(int i : list) {
            this.oids.add(i);
        }
    }

    public int getSize() {
        return oids.size();
    }

    public IntSet getObjects () {
        return oids;
    }

    public void addObject(int obj) {
        oids.add(obj);
    }

    public void setID(String id) {
        ID = id;
    }

    public String getID() {
        return ID;
    }

    @Override
    public String toString() {
        return  "<"+ID+":"+ oids.toString() +">";
    }

    public void addObjects(Set<Integer> objects) {
        oids.addAll(objects);
    }

}
