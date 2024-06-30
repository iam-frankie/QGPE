package ClusteringGrid;
import com.github.davidmoten.geo.*;
// 1、获取邻居更加方便
// 2、可以直接获取某个方向的邻居


import java.util.List;

public class GeohashTest {

    public static void main(String[] args) {
        String geohashString = GeoHash.encodeHash(37.7, -122.4, 5);
        LatLong result = GeoHash.decodeHash(geohashString);
//        GeoHash.
        List<String> result2 = GeoHash.neighbours(geohashString);
        GeoHash.adjacentHash(geohashString, Direction.BOTTOM);
        System.out.println(geohashString);
        System.out.println(result2);
    }
}
