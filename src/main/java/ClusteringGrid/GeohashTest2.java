package ClusteringGrid;
//import com.github.davidmoten.geo.*;
import ch.hsr.geohash.*;

public class GeohashTest2 {

    public static void main(String[] args) {
//        String geohashString = GeoHash.geoHashStringWithCharacterPrecision(37.7, -122.4, 5);
//        System.out.println(geohashString);
//


        GeoHash geoHash = GeoHash.withBitPrecision(37.7, -122.4, 7);
        String binaryString = geoHash.toBinaryString();
        System.out.println(binaryString.length());
        System.out.println(binaryString);

        GeoHash northernNeighbor2 = geoHash.getNorthernNeighbour();
        String binaryString2 = northernNeighbor2.toBinaryString();
        System.out.println(binaryString2);


        System.out.println("----------------------------------------------------------------------------");
        // Convert the binary string to a decimal number
        long decimalGeoHash = Long.parseLong("1100", 2); // radix设置为2表示是二进制
        System.out.println(decimalGeoHash);
        // Create a GeoHash object from the decimal number
        GeoHash geoHash2 = GeoHash.fromLongValue(decimalGeoHash, 32);

        // Get the northern neighbor
        GeoHash northernNeighbor = geoHash2.getNorthernNeighbour();

        // Convert the GeoHash of the northern neighbor back to a binary string
        String binaryNorthernNeighbor = Long.toBinaryString(northernNeighbor.longValue());

        System.out.println(northernNeighbor);
        System.out.println("Northern neighbor in binary: " + binaryNorthernNeighbor);


    }
}