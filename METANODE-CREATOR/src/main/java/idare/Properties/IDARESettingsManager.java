package idare.Properties;

import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.session.events.SessionLoadedEvent;

/**
 * Class to keep track of IDARE IDs and complain (throw exceptions) if duplicate IDs are found. hopefully there wont be any instances with > max(long) nodes in one network...
 * 
 * @author thomas
 *
 */
public class IDARESettingsManager{
	
	private HashSet<Long> nodeIDs;
	private Long maxNodeID = new Long(0);
	private HashSet<Long> networkIDs;
	private Long maxNetworkID = new Long(0);
	
	private IDAREProperties properties;
	private HashMap<String,String> SubNetworkTypes;
	
	/**
	 * Initialize the IDare Manager
	 */
	public IDARESettingsManager()
	{
		properties = new IDAREProperties();
		nodeIDs = new HashSet<Long>();
		maxNodeID = new Long(0);
		maxNetworkID = new Long(0);
		networkIDs = new HashSet<Long>();
		SubNetworkTypes = new HashMap<String, String>();
	}
	/**
	 * Get the next ID which is the largest current ID + 1
	 * This Id should be used otherwise it will be blocked for this manager.
	 * @return a valid IDARE identifier
	 */
	
	public long getNextNodeID()
	{
		maxNodeID = maxNodeID+1;
		nodeIDs.add(maxNodeID);
		return maxNodeID;
	}
	
	
	/**
	 * Get the next network ID which is the largest current ID + 1
	 * This Id should be used otherwise it will be blocked for this manager.
	 * @return a valid IDARE identifier
	 */
	
	public long getNextNetworkID()
	{
		maxNetworkID = maxNetworkID+1;
		networkIDs.add(maxNetworkID);
		return maxNetworkID;
	}
	
	/**
	 * Reset this Manager. This invalidates all IDs formerly provided by this manager.
	 * All IDARE objects/nodes should therefore recieve a new ID once this function is called.
	 */
	private void reset()
	{
		nodeIDs = new HashSet<Long>();
		maxNodeID = new Long(0);
		networkIDs = new HashSet<Long>();
		maxNetworkID = new Long(0);
	}
	
	/**
	 * Add an ID to this manager. The manager checks whether this ID is already used.
	 * @param id - The id that needs to be checked
	 * @throws IllegalArgumentException - If the provided ID is already present in this Manager this exception is thrown but the Manager is still valid 
	 */
	private void addID(Long id) throws IllegalArgumentException
	{
		if(nodeIDs.contains(id)) {
			throw new IllegalArgumentException("ID already used");
		}
		else
		{
			if(id.compareTo(maxNodeID) > 0)
			{
				maxNodeID = id;
			}
			nodeIDs.add(id);
		}
	}

	/**
	 * Add a network ID to this manager. The manager checks whether this ID is already used.
	 * @param id - The id that needs to be checked
	 * @throws IllegalArgumentException - If the provided ID is already present in this Manager this exception is thrown but the Manager is still valid 
	 */
	private void addNetworkID(Long id) throws IllegalArgumentException
	{
		if(networkIDs.contains(id)) {
			throw new IllegalArgumentException("ID already used");
		}
		else
		{
			if(id.compareTo(maxNetworkID) > 0)
			{
				maxNetworkID = id;
			}
			networkIDs.add(id);
		}
	}
	
	
	/**
	 * Add all IDs of the collection to this manager. The manager checks whether any ID is already used.
	 * @param ids - The ids that need to be checked
	 * @throws IllegalArgumentException - If the provided ID is already present in this Manager this exception is thrown
	 * IF an exception is thrown, the Manager is still valid and none of the provided ids has been added. 
	 */
	public void addIDs(Collection<Long> ids) throws IllegalArgumentException
	{
		for(Long id : ids)
		{
			if(nodeIDs.contains(id))
			{
				throw new IllegalArgumentException("ID already used");
			}
		}

		for(Long id : ids)
		{
			if(id > maxNodeID)
			{
				maxNodeID = id;
			}
			nodeIDs.add(id);
		}	
		
	}
	
	/**
	 * Get the Type associated with a specific Name.
	 * @param name
	 * @return the type associated with the given name.
	 */
	public String getType(String name)
	{
		return properties.getType(name);
	}	
	
	/**
	 * Set the IDAREType associated with a specific Target Type.
	 * @param IDAREType
	 * @param targetType
	 */
	public void setType(String IDAREType, String targetType)
	{
		if(targetType != null)
		{
			properties.setType(IDAREType, targetType);
		}
			
	}
	
	/**
	 * Reset the SubNetworkTypes. This should be called when new subnetworktypes are being set for a subnetwork generation process.
	 */
	public void resetSubNetworkTypes()
	{
		SubNetworkTypes.clear();
	}
	
	/**
	 * Set the IDAREType associated with a specific Target Type for subnetwork generation.
	 * @param IDAREType
	 * @param targetType
	 */
	public void setSubNetworkType(String IDAREType, String targetType)
	{
		if(targetType != null)
		{
			SubNetworkTypes.put(targetType,IDAREType);
		}
			
	}
	
	/**
	 * Get the IDAREType associated with a specific Target Type for the subnetwork generation.
	 * @param targetType
	 */
	public String getSubNetworkType(String targetType)
	{
			return SubNetworkTypes.get(targetType);
	}
	
	

	public void handleSessionLoadedEvent(SessionLoadedEvent arg0) {
		reset();
		Set<CyNetwork> networks = arg0.getLoadedSession().getNetworks();
		HashMap<Long,CyNode> nodematch = new HashMap<Long, CyNode>();
		HashMap<Long,CyNetwork> networkmatch = new HashMap<Long, CyNetwork>();
		for(CyNetwork network : networks)
		{
			
			if(isSetupNetwork(network))
			{
				CyTable NodeTable = network.getDefaultNodeTable();
				CyTable NetworkTable = network.getDefaultNetworkTable();
				for(CyRow row : NodeTable.getAllRows())
				{
					Long IDAREID = row.get(IDAREProperties.IDARE_NODE_UID, Long.class);
					CyNode current = network.getNode(row.get(CyNode.SUID,Long.class));
					//this happens, if the node is not in this network. As we get the default NOde Table it contains ALL Nodes...) 
					if(current == null)
					{
						continue;
					}
					if(!nodematch.containsKey(IDAREID))
					{
						addID(IDAREID);
						nodematch.put(IDAREID, current);
					}
					else
					{
						//if this is not pointing to the same node, we have to reset the whole IDARE Setup, i.e. we will clear the whole network from Cytoscape Columns.
						PrintFDebugger.Debugging(this, "Determining whether " + nodematch.get(IDAREID) + " has the same SUID as " + current);
						if(!nodematch.get(IDAREID).getSUID().equals(current.getSUID()))
						{
							PrintFDebugger.Debugging(this, "Found a duplicate node id: " + IDAREID + " ... removing all IDARE Columns");
							resetNetworks(networks);
							reset();
							return;
						}
					}
				}
				Long NetworkID = NetworkTable.getRow(network.getSUID()).get(IDAREProperties.IDARE_NETWORK_ID, Long.class);
				if(!networkmatch.containsKey(NetworkID))
				{
					networkmatch.put(NetworkID, network);
					addNetworkID(NetworkID);
				}
				else
				{
					if(!networkmatch.get(NetworkID).getSUID().equals(network.getSUID()))
					{
						PrintFDebugger.Debugging(this, "Found a duplicate network id: " + NetworkID + " ... removing all IDARE Columns");
						resetNetworks(networks);
						reset();						
						return;
					}
				}
			}
		}
	}
	
	public static void resetNetworks(Collection<CyNetwork> networks)
	{
		for(CyNetwork network : networks)
		{
			
			if(isSetupNetwork(network))
			{
				CyTable NodeTable = network.getDefaultNodeTable();
				CyTable EdgeTable = network.getDefaultEdgeTable();
				CyTable NetworkTable = network.getDefaultNetworkTable();
				if(NodeTable.getColumn(IDAREProperties.IDARE_NODE_UID) != null)
				{
					NodeTable.deleteColumn(IDAREProperties.IDARE_NODE_UID);
				}
				if(NodeTable.getColumn(IDAREProperties.IDARE_NODE_TYPE) != null)
				{
					NodeTable.deleteColumn(IDAREProperties.IDARE_NODE_TYPE);
				}
				if(NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME) != null)
				{
					NodeTable.deleteColumn(IDAREProperties.IDARE_NODE_NAME);
				}
				if(NodeTable.getColumn(IDAREProperties.LINK_TARGET) != null)
				{
					NodeTable.deleteColumn(IDAREProperties.LINK_TARGET);
				}
				if(NodeTable.getColumn(IDAREProperties.LINK_TARGET_SUBSYSTEM) != null)
				{
					NodeTable.deleteColumn(IDAREProperties.LINK_TARGET_SUBSYSTEM);
				}
				if(NodeTable.getColumn(IDAREProperties.IDARE_SUBNETWORK_TYPE) != null)
				{
					NodeTable.deleteColumn(IDAREProperties.IDARE_SUBNETWORK_TYPE);
				}
				if(EdgeTable.getColumn(IDAREProperties.IDARE_EDGE_PROPERTY) != null)
				{
					EdgeTable.deleteColumn(IDAREProperties.IDARE_EDGE_PROPERTY);
				}
				if(NetworkTable.getColumn(IDAREProperties.IDARE_NETWORK_ID) != null)
				{
					NetworkTable.deleteColumn(IDAREProperties.IDARE_NETWORK_ID);
				}								
				
			}

		}
	}
	
	
	/**
	 * Set up the provided network using the Provided IDCol for the IDARE Names and the NodeTypesCol for the IDARE Node Types
	 * @param network - the network to set up
	 * @param IDAREIdmgr - A IDARE ID Manager to set up the IDs of all  
	 * @param NodeTypeCol
	 * @param IDCol
	 */
	public static void SetupNetwork(CyNetwork network, IDARESettingsManager IDAREIdmgr, String NodeTypeCol, String IDCol )
	{
		CyTable NodeTable = network.getDefaultNodeTable();
		CyTable EdgeTable = network.getDefaultEdgeTable();
		CyTable NetworkTable = network.getDefaultNetworkTable();
		
		
		initNetwork(network);
		if(!NetworkTable.getRow(network.getSUID()).isSet(IDAREProperties.IDARE_NETWORK_ID))
		{
			NetworkTable.getRow(network.getSUID()).set(IDAREProperties.IDARE_NETWORK_ID,IDAREIdmgr.getNextNetworkID());
		}
		List<CyRow> NodeRows = NodeTable.getAllRows();
		
		for(CyRow row : NodeRows)
		{
			// if the column is either not set, or at the default value, initialize it.
			//furthermore we can initialize the IDARE_TARGET_ID property for save/restore functionality
			if(!row.isSet(IDAREProperties.IDARE_NODE_UID))
			{
				//row.set(IDAREProperties.IDARE_NODE_TYPE, row.get(NodeTypeCol,String.class));
				Long id = IDAREIdmgr.getNextNodeID();
				row.set(IDAREProperties.IDARE_NODE_UID, id);

			}
			if(!row.isSet(IDAREProperties.IDARE_NODE_TYPE))
			{				
				
				row.set(IDAREProperties.IDARE_NODE_TYPE,IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
			}
			else
			{
				//if the value is not null but still the default, than also set it to the type value.
				if(row.get(IDAREProperties.IDARE_NODE_TYPE,String.class) != null && 
						row.get(IDAREProperties.IDARE_NODE_TYPE,String.class).equals(NodeTable.getColumn(IDAREProperties.IDARE_NODE_TYPE).getDefaultValue()))
				{
					if(!row.get(IDAREProperties.IDARE_NODE_TYPE,String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
					{
						//never change a linker node type.
						row.set(IDAREProperties.IDARE_NODE_TYPE,IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
					}
				}
			}
						
			if(NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME).getDefaultValue() == null)
			{
				System.out.println("Default Value Wrong!!!");
			}
			if(!row.isSet(IDAREProperties.IDARE_NODE_NAME) && NodeTable.getColumn(IDCol) != null)
			{
				if(!row.isSet(IDAREProperties.IDARE_NODE_TYPE) || !row.get(IDAREProperties.IDARE_NODE_TYPE,String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
				{
					row.set(IDAREProperties.IDARE_NODE_NAME, row.get(IDCol,String.class));
				}
			}
			else if (NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME) != null && (NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME).getDefaultValue() == null || NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME).getDefaultValue().equals(row.get(IDAREProperties.IDARE_NODE_NAME, String.class))))
			{
				row.set(IDAREProperties.IDARE_NODE_NAME, row.get(IDCol,String.class));
			}
		}
		List<CyRow> EdgeRows = EdgeTable.getAllRows();
		for(CyRow row: EdgeRows)
		{
			if(!row.isSet(IDAREProperties.IDARE_EDGE_PROPERTY) || row.get(IDAREProperties.IDARE_EDGE_PROPERTY, String.class).equals(EdgeTable.getColumn(IDAREProperties.IDARE_EDGE_PROPERTY).getDefaultValue()))
			{
				if(row.isSet(IDAREProperties.SBML_EDGE_TYPE))
				{
					row.set(IDAREProperties.IDARE_EDGE_PROPERTY, row.get(IDAREProperties.SBML_EDGE_TYPE, String.class));
				}
			}
		}

	}
	
	/**
	 * Set up the provided network using the Provided IDCol for the IDARE Names and the NodeTypesCol for the IDARE Node Types
	 * @param network - the network to set up
	 * @param IDAREIdmgr - A IDARE ID Manager to set up the IDs of all  
	 * @param NodeTypeCol
	 * @param IDCol
	 */
	public static void SetNetworkData(CyNetwork network, IDARESettingsManager IDAREIdmgr, String NodeTypeCol, String IDCol, boolean overwrite , NodeManager nm)
	{
		if(!isSetupNetwork(network))
		{
			SetupNetwork(network, IDAREIdmgr, NodeTypeCol, IDCol);
		}
		CyTable NodeTable = network.getDefaultNodeTable();
		CyTable NetworkTable = network.getDefaultNetworkTable();				
		if(!NetworkTable.getRow(network.getSUID()).isSet(IDAREProperties.IDARE_NETWORK_ID))
		{
			NetworkTable.getRow(network.getSUID()).set(IDAREProperties.IDARE_NETWORK_ID,IDAREIdmgr.getNextNetworkID());
		}
		List<CyRow> NodeRows = NodeTable.getAllRows();
		Set<String> IDAREIDs = new HashSet<String>();
		for(CyRow row : NodeRows)
		{
			//if the column is either not set, or at the default value, initialize it.
			//furthermore we can initialize the IDARE_TARGET_ID property for save/restore functionality
			if(!row.isSet(IDAREProperties.IDARE_NODE_UID))
			{
				//row.set(IDAREProperties.IDARE_NODE_TYPE, row.get(NodeTypeCol,String.class));
				Long id = IDAREIdmgr.getNextNodeID();
				row.set(IDAREProperties.IDARE_NODE_UID, id);

			}			
			//if the row is not set, or is equal to null or is at the default, set it to an updated value.
			if(!row.isSet(IDAREProperties.IDARE_NODE_TYPE))
			{
				row.set(IDAREProperties.IDARE_NODE_TYPE,IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
			}
			if(overwrite)
			{
				PrintFDebugger.Debugging(IDAREIdmgr, "Asking Property manager to obtain value for entry " + row.get(NodeTypeCol, String.class) + " it returned " + IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
				if(!row.get(IDAREProperties.IDARE_NODE_TYPE,String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
				{
					row.set(IDAREProperties.IDARE_NODE_TYPE,IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
				}
			}
			if(!row.isSet(IDAREProperties.IDARE_NODE_TYPE) || !row.get(IDAREProperties.IDARE_NODE_TYPE,String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
			{
				//DO NOT UPDATE LINKER NODE NAMES!
				row.set(IDAREProperties.IDARE_NODE_NAME, row.get(IDCol,String.class));
			}
			IDAREIDs.add(row.get(IDCol, String.class));
		}
		nm.updateNetworkNodes();
	}
	/**
	 * Test whether a Network has the table Columns required for usage in IDARE
	 * @param network
	 * @return whether the network is set up to be used with IDARE.
	 */
	public static boolean isSetupNetwork(CyNetwork network)
	{
		CyTable NodeTable = network.getDefaultNodeTable();
		CyTable EdgeTable = network.getDefaultEdgeTable();
		if(EdgeTable.getColumn(IDAREProperties.IDARE_EDGE_PROPERTY) == null || NodeTable.getColumn(IDAREProperties.IDARE_NODE_TYPE) == null 
				|| 	NodeTable.getColumn(IDAREProperties.IDARE_NODE_NAME) == null ||	NodeTable.getColumn(IDAREProperties.LINK_TARGET) == null
				|| NodeTable.getColumn(IDAREProperties.IDARE_NODE_UID) == null || NodeTable.getColumn(IDAREProperties.LINK_TARGET_SUBSYSTEM) == null
				|| NodeTable.getColumn(IDAREProperties.IDARE_SUBNETWORK_TYPE) == null)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Set the matching IDARENames to the values set in the Selected Column(except that Strings will be used in the IDARE_NAME column
	 * @param network - the network for which to assign the names
	 * @param IDs - The IDs which are transferred
	 * @param ColForNames - The Column to get the IDs from.
	 */
	public static void setIDARENames(CyNetwork network, Collection<String> IDs, String ColForNames)
	{
		CyTable nodeTable = network.getDefaultNodeTable();
		Set<CyRow> matchingrows = new HashSet<CyRow>();
		for(String id : IDs)
		{
			matchingrows.addAll(nodeTable.getMatchingRows(ColForNames, id));
		}
		for(CyRow row : matchingrows)
		{
			row.set(IDAREProperties.IDARE_NODE_NAME, row.get(ColForNames,String.class));
		}
	}
	
	/**
	 * Set up the provided network using the Provided IDCol for the IDARE Names and the NodeTypesCol for the IDARE Node Types
	 * @param network - the network to set up
	 */
	public static void initNetwork(CyNetwork network)
	{
		CyTable NodeTable = network.getDefaultNodeTable();
		CyTable EdgeTable = network.getDefaultEdgeTable();
		CyTable NetworkTable = network.getDefaultNetworkTable();
		try{
			EdgeTable.createColumn(IDAREProperties.IDARE_EDGE_PROPERTY, String.class, false,"");
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}

		try{
			NodeTable.createColumn(IDAREProperties.IDARE_NODE_TYPE, String.class, false,"");
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}

		try{
			NodeTable.createColumn(IDAREProperties.IDARE_NODE_NAME, String.class, false,"");
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}


		try{
			NodeTable.createColumn(IDAREProperties.LINK_TARGET, Long.class, false);
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}

		try{
			NodeTable.createColumn(IDAREProperties.IDARE_NODE_UID, Long.class, false, null);			
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}

		try{
			NodeTable.createColumn(IDAREProperties.LINK_TARGET_SUBSYSTEM, Long.class, false);
		}

		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}
		try{
			NodeTable.createColumn(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class, false);
		}

		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}
		
		try{
			NetworkTable.createColumn(IDAREProperties.IDARE_NETWORK_ID, Long.class, false, null);
		}

		catch(IllegalArgumentException e)
		{
			e.printStackTrace(System.out);
		}
	}
	

	/**
	 * Get the set of nodeIDs present in the provided network
	 * @param network - the {@link CyNetwork} to retrieve the nodeids from
	 * @param IDCol - The column in the networks node {@link CyTable} containing the IDs.
	 * @return - a {@link Set} of IDs present in the provided network
	 */
	public static Set<String> getNetworkIDAREIDs(CyNetwork network, String IDCol)
	{
		Set<String> ids = new HashSet<String>();
		if(isSetupNetwork(network))
		{
			List<CyRow> NodeRows = network.getDefaultNodeTable().getAllRows();
			for(CyRow row : NodeRows)
			{
				ids.add(row.get(IDCol,String.class));			
				
			}

		}
		return ids;
	}
	
	
}
