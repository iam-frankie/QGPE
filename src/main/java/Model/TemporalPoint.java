package Model;

/**
 * 1、记录了时间点的点形式
 * @author Frankie
 */

public class TemporalPoint extends Point{
    private static final long serialVersionUID = 4837777569308584367L;
    private final int time;
    private final int hashCode;
    public TemporalPoint(int id, double lat, double lont, int time) {
        super(id, lat, lont, 0, 0);
        this.time = time;
        final int prime = 31;
        int result = super.hashCode();
        hashCode = prime * result + time; // temporal point 需要结合时刻信息产生新的hashcode
    }
    public int getTime() {
        return this.time;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * 判断两个temporal point点是否相同，还需要判断时间上的紧凑型
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj instanceof TemporalPoint) {
            if(((TemporalPoint) obj).getTime() == time) {
                if(Math.abs(((TemporalPoint) obj).getX() - this.getX()) < 0.0000001) {
                    if(Math.abs(((TemporalPoint) obj).getY() - this.getY()) < 0.0000001) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("<%8.6f,%8.6f,%d>", this.getX(),
                this.getY(), this.getTime());
    }
}
