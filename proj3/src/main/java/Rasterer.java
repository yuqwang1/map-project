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
        double lrlon = params.get("lrlon");
        double ullon = params.get("ullon");
        double lrlat = params.get("lrlat");
        double ullat = params.get("ullat");
        double width = params.get("w");
        double lonDPP = ((lrlon - ullon)/width);
        int depth = measureDepth(lonDPP);
        int[] x_range = measureXRange(ullon, lrlon, depth);
        int[] y_range = measureYRange(ullat, lrlat, depth);

        if (ullon > lrlon || lrlat > ullat || lrlon <= ROOT_ULLON|| ullon >= ROOT_LRLON || lrlat >= ROOT_ULLAT||ullat <= ROOT_LRLAT){
            query_success = false;
        }

        double x_step = ROOT_WIDTH / Math.pow(2, depth);
        double y_step = ROOT_HEIGHT/ Math.pow(2, depth);
        String[][] grid_render = getRenderDoc(depth, x_range, y_range);
        results.put("raster_ul_lon", ROOT_ULLON + x_range[0] * x_step);
        results.put("raster_lr_lon", ROOT_ULLON + (1.0 + x_range[1]) * x_step);
        results.put("raster_ul_lat", ROOT_ULLAT - y_range[0] * y_step);
        results.put("raster_lr_lat", ROOT_ULLAT - (1.0 + y_range[1]) * y_step);
        results.put("render_grid", grid_render);
        results.put("depth", depth);
        results.put("query_success", query_success);

        return results;


//        System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
//                + "your browser.");
//        return results;


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
         double current_x = ROOT_ULLON + movement;
         int x1,x2;
         for (x1 = 0; user_ullon > current_x; x1++){
             current_x += movement;
         }

         for ( x2 = x1; user_lrlon < current_x; x2++ ) {
             current_x += movement;
             if (current_x > ROOT_LRLON) {
                 break;
             }
         }

         result[0] = x1;
         result[1] = x2;
         return result;


    }

    private int[] measureYRange(double user_ullat, double user_lrlat, double depth) {
        int[] result = new int[2];
        double movement = ROOT_HEIGHT/Math.pow(2, depth);
        double current_y = ROOT_ULLAT - movement;
        int y1,y2;
        for (y1 = 0; user_ullat < current_y; y1++){
            current_y -= movement;
        }

        for (y2 = y1; user_lrlat > current_y; y2++) {
            current_y -= movement;
            if (current_y < ROOT_LRLAT) {
                break;
            }
        }

        result[0] = y1;
        result[1] = y2;
        return result;
    }

    private String[][] getRenderDoc(int depth, int[] x_range, int[] y_range ){
        String[][] result = new String[y_range[1] - y_range[0] + 1][x_range[1] - x_range[0]+ 1];
        for (int j = 0; j + y_range[0] < y_range[1]; j++) {
            for (int i = 0; i + x_range[0] < x_range[1]; i++) {
                String filename = "d" + Integer.toString(depth) + "_x" + Integer.toString(i + x_range[0]) + "_y" + Integer.toString(j + y_range[0])+ ".png";
                result[j][i] = filename;
            }
        }
        return result;
    }
}
