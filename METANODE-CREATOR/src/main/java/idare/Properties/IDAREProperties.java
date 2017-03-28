package idare.Properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class containing all static Fields for the IDARE Columns. Please use only these fields when altering IDARE columns.
 * @author Thomas PFau
 *
 */
public class IDAREProperties {

	
	public static final String IDARE_NAMESPACE = "idare";
	private HashMap<String,String> typeMap= new HashMap<String,String>();
	
	public enum ColumnHeaders
	{
		IDARE_NODE_TYPE("IDARENodeType",String.class,""),
		IDARE_NODE_NAME("IDARENodeName",String.class,""),
		IDARE_DUPLICATED_NODE("IDAREDuplicatedNode",Boolean.class,false),
		IDARE_ORIGINAL_NODE("IDAREOriginalNode",Long.class,null),
		IDARE_LINK_TARGET_SUBSYSTEM("IDARETargetSubsystem",Long.class,null),
		IDARE_LINK_TARGET("IDARELinkTargets",Long.class,null),
		IDARE_NODE_UID("IDARELinkTargetID",Long.class,null),
		IDARE_SUBNETWORK_TYPE("IDARESubNetworkNodeType",String.class,""),		
		IDARE_NETWORK_ID("IDARENetworkID",Long.class,null),
		IDARE_GENE_EDGE_ID("IDAREGeneEdge",Long.class,null),
		IDARE_EDGE_PROPERTY("IDAREEdgeType",String.class,"");
		
		
		private final String text;
		private final Class type;
		private final Object defaultValue;
		private ColumnHeaders(final String text, Class type, Object defaultValue)
		{
			this.text = text;
			this.defaultValue = defaultValue;
			this.type = type;
		}
		
		@Override
		public String toString()
		{
			return text;
		}
		public Class getType()
		{
			return type;
		}
		public Object getdefaultValue()
		{
			return defaultValue;
		}
	}

	public static ColumnHeaders[] NetworkHeaders = {ColumnHeaders.IDARE_NETWORK_ID};
		
	public static ColumnHeaders[] EdgeHeaders = {ColumnHeaders.IDARE_GENE_EDGE_ID,ColumnHeaders.IDARE_EDGE_PROPERTY};
	
	//All Headers refering to nodes.
	public static ColumnHeaders[] NodeHeaders = {ColumnHeaders.IDARE_NODE_TYPE,
												 ColumnHeaders.IDARE_NODE_NAME,
												 ColumnHeaders.IDARE_DUPLICATED_NODE,
												 ColumnHeaders.IDARE_LINK_TARGET,
												 ColumnHeaders.IDARE_LINK_TARGET_SUBSYSTEM,
												 ColumnHeaders.IDARE_NODE_UID,
												 ColumnHeaders.IDARE_ORIGINAL_NODE,
												 ColumnHeaders.IDARE_SUBNETWORK_TYPE
												 };
	
	/**
	 * IDARE Column Headers
	 */
	public static final String IDARE_EDGE_PROPERTY = "IDAREEdgeType";
	public static final String IDARE_NODE_TYPE = "IDARENodeType";
	public static final String IDARE_SUBNETWORK_TYPE = "IDARESubNetworkNodeType";
	public static final String IDARE_NODE_NAME = "IDARENodeName";
	public static final String IDARE_NETWORK_ID = "IDARENetworkID";
	public static final String IDARE_GENE_EDGE_ID = "IDAREGeneEdge";
	public static final String IDARE_DUPLICATED_NODE = "IDAREDuplicatedNode";
	public static final String IDARE_ORIGINAL_NODE = "IDAREOriginalNode";
	/**
	 * This field should only be set if the corresponding node is a Link Node (i.e. the IDARENodeType is IDARE_LINK)
	 */
	public static final String IDARE_LINK_TARGET = "IDARELinkTargets";
	/**	 
	 * The ID for this node if it is the target of a link. 
	 * At network creation this will be set to the SUID of the corresponding node.
	 * It should never ever be overwritten
	 * Link Nodes should (in general) not have this property set. 
	 */	
	public static final String IDARE_NODE_UID = "IDARELinkTargetID";
	/**
	 * This is an awkward way to restore the correct linking behaviour since there is otherwise no way to know which SubsystemView a link targeted.
	 */
	public static final String IDARE_LINK_TARGET_SUBSYSTEM = "IDARETargetSubsystem";
	
	public static final String SBML_EDGE_TYPE = "interaction type";
	
	/**
	 * IDARE SubSystems Save File
	 */
	public static final String SUBSYSTEMS_SAVE_FILE = "SubsysStore";
	public static final String SUBSYSTEMS_SAVE_ID = "SubsysStorage";
	
	/**
	 * The Field ID of IDARE information in stored sessions.
	 */
	public static final String IDARE_IMAGE_SESSION_ID = "IDAREImageFiles";
	public static final String IDARE_DISPLAYMANAGERINFO_SESSION_ID = "IDAREDisplayManager";
	
	public static class NodeType
	{
		public static final String IDARE_GENE = "gene";
		public static final String IDARE_SPECIES = "species";
		public static final String IDARE_REACTION = "reaction";
		public static final String IDARE_imagenode = "imagenode";
		public static final String IDARE_PROTEIN = "protein";
		public static final String IDARE_LINK = "link";
		public static final String IDARE_PROTEINCOMPLEX = "proteincomplex";		
	}
	
	public static class EdgeType
	{
		public static final String REACTION_REVERSIBLE = "reversible";
		public static final String PRODUCT_EDGE = "reaction-product";
		public static final String REACTANT_EDGE = "reaction-reactant";
	}
	
	/**
	 * Some specifications for SBML data fields
	 */
	public static final String SBML_GENE_STRING = "gene";
	public static final String SBML_TYPE_STRING = "sbml type";
	public static final String SBML_ID_STRING = "sbml id";
	public static final String SBML_SPECIES_STRING = "species";
	public static final String SBML_REACTION_STRING = "reaction";
	public static final String SBML_NAME_STRING = "name";
	public static final Map<String, String> SBMLToIDARE;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(SBML_REACTION_STRING, NodeType.IDARE_REACTION);
        aMap.put(SBML_SPECIES_STRING, NodeType.IDARE_SPECIES);
        aMap.put(SBML_GENE_STRING, NodeType.IDARE_GENE);
        SBMLToIDARE = Collections.unmodifiableMap(aMap);
    }
    
    public String getType(String name)
    {
    	return typeMap.get(name);
    }
    public void setType(String IDAREId, String name)
    {
    	typeMap.put(name,IDAREId);	    	
    }
}


