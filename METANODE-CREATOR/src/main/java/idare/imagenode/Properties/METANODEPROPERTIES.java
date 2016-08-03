package idare.imagenode.Properties;

import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;

/**
 * Class that stores the Static properties used by METANODES.
 * @author Thomas Pfau
 *
 */
public class METANODEPROPERTIES {
	
	public static String DATASET_FILES = "METANODE_DATASET_FILES";
	public static String DATASET_PROPERTIES = "METANODE_DATASET_PROPERTIES";
	public static String METANODE_TEMP_FOLDER = "IDARE_METANODE_TEMP";
	
	
	/**
	 * IDs for style management during save and load
	 */
	public static String STYLE_MAPPINGS_SAVE_FILE = "IDARE_STYLE_MAPPINGS_SAVEFILE";
	public static String STYLE_MAPPINGS_SAVE_NAME = "IDARE_STYLE_MAPPINGS";
	
	/**
	 * IDS for File Lists to store and save Datasets in cytoscape
	 */
	public static String DATASET_PROPERTIES_FILE_NAME = "METANODE_DATASET_PROPERTIES";
	public static String DATASETCOLLECTION_FILE_NAME = "METANODE_DATASETS";
	
	public static String LAYOUT_FILES = "NODE_LAYOUTS";
	public static String LAYOUT_FILE_NAME = "IDARE_NODE_LAYOUTS";
	
	public static int IMAGEWIDTH = 400;
	public static int IMAGEHEIGHT = 240;
	public static int LABELHEIGHT = 50;
	public static enum LayoutStyle {EDGE, CENTER}; 
	
	/**
	 * Visual Properties of Metanodes
	 */	
	public static double IDARE_NODE_DISPLAY_WIDTH = 80.; 
	public static double IDARE_NODE_DISPLAY_HEIGHT = 290./400 * 80;
	public static NodeShape IDARE_NODE_DISPLAY_SHAPE = NodeShapeVisualProperty.RECTANGLE;
	
	public static int LEGEND_DESCRIPTION_OPTIMAL_WIDTH = 300;
}
