package rvmm.files.shp;

/**
 *
 * @author McKillaGorilla
 */
public class SHPtoJSONTester {
    static final String dir = "./data/";
    static final String shpFileName = "Luxembourg.shp";
    static final String jsonFileName = "Luxembourg.json";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            SHPToJSONConverter shpToJSONConverter = new SHPToJSONConverter();
            SHPData data = shpToJSONConverter.loadData(dir + shpFileName);
            shpToJSONConverter.saveData(data, dir + jsonFileName);
            System.out.println(data);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
