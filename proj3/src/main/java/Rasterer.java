import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private Boolean query_success;
    private static final double ROOT_LRLON = MapServer.ROOT_LRLON;
    private static final double ROOT_ULLON = MapServer.ROOT_ULLON;
    private static final double ROOT_ULLAT = MapServer.ROOT_ULLAT;
    private static final double ROOT_LRLAT = MapServer.ROOT_LRLAT;
    private static final double ROOT_WIDTH = ROOT_LRLON - ROOT_ULLON;
    private static final double ROOT_HEIGHT = ROOT_ULLAT - ROOT_LRLAT;
    private static final double ROOT_LONDPP = (ROOT_WIDTH)/MapServer.TILE_SIZE;

    public Rasterer() {
        // YOUR CODE HERE
        query_success = true;

    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();
//        System.out.println(params);
        double lonDPP = (params.get("lrlon") - params.get("ullon")/params.get("w"));
        int depth = measureDepth(lonDPP);


        System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
                + "your browser.");
        return results;
    }

    private int measureDepth(double user_DPP) {
        int depth = 0;
        double current_DPP = ROOT_LONDPP;
        while (current_DPP > user_DPP && depth < 7) {
            current_DPP /= 2;
            depth++;
        }
        return depth;
    }

    private int[] measureXRange(double user_ullon, double user_lrlon, double depth) {
         int[] result = new int[2];
         double movement = ROOT_WIDTH/Math.pow(2, depth);
         double current_x = ROOT_ULLON;
         int x1,x2;
         for (x1 = 0; user_ullon > current_x; x1++){
             current_x += movement;
         }

         for ( x2 = x1; user_lrlon < current_x; x2++ ) {
             current_x += movement;
         }

         result[0] = x1;
         result[1] = x2;
         return result;


    }

    private int[] measureYRange(double user_ullat, double user_lrlat, double depth) {
        int[] result = new int[2];
        double movement = ROOT_HEIGHT/Math.pow(2, depth);
        double current_y = ROOT_ULLAT;
        int y1,y2;
        for (y1 = 0; user_ullat < current_y; y1++){
            current_y -= movement;
        }

        for ( y2 = y1; user_lrlat > current_y; y2++ ) {
            current_y -= movement;
        }

        result[0] = y1;
        result[1] = y2;
        return result;
    }




}
