package idare.Properties;

import idare.Properties.IDAREProperties.ColumnHeaders;
import idare.imagenode.Interfaces.DataSetReaders.WorkBook.IDARERow;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
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
	private HashSet<Long> proteinIDs;
	private Long maxProteinID = new Long(0);
	private HashSet<Long> geneIDs;
	private Long maxGeneID = new Long(0);
	private HashSet<Long> proteinComplexIDs;
	private Long maxProteinComplexID = new Long(0);
	private Pattern genePattern = Pattern.compile("^" + IDAREProperties.NodeType.IDARE_GENE + "([0-9]+)$");
	private Pattern proteinPattern = Pattern.compile("^" + IDAREProperties.NodeType.IDARE_PROTEIN + "([0-9]+)$");
	private Pattern proteinComplexPattern = Pattern.compile("^" + IDAREProperties.NodeType.IDARE_PROTEINCOMPLEX + "([0-9]+)$");
	private IDAREProperties properties;
	private HashMap<String,String> SubNetworkTypes;
	
	private CyTableManager cyTableMgr;
	private CyNetworkTableManager cyNetTableMgr;
	
	
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
		proteinIDs = new HashSet<Long>();
		maxProteinID = new Long(0);
		geneIDs = new HashSet<Long>();
		maxGeneID = new Long(0);
		maxProteinComplexID = new Long(0);
		proteinComplexIDs = new HashSet<Long>();		
		SubNetworkTypes = new HashMap<String, String>();
	}
	
	/**
	 * Get the next protein ID which is the largest current ID + 1
	 * This Id should be used otherwise it will be blocked for this manager.
	 * @return a valid IDARE identifier
	 */
	
	public long getNextProteinID()
	{
		maxProteinID = maxProteinID+1;
		proteinIDs.add(maxProteinID);
		return maxProteinID;
	}
	
	
	/**
	 * Get the next gene ID which is the largest current ID + 1
	 * This Id should be used otherwise it will be blocked for this manager.
	 * @return a valid IDARE identifier
	 */
	
	public long getNextGeneID()
	{
		maxGeneID = maxGeneID+1;
		geneIDs.add(maxGeneID);
		return maxGeneID;
	}
	
	
	/**
	 * Get the nextprotein Complex  ID which is the largest current ID + 1
	 * This Id should be used otherwise it will be blocked for this manager.
	 * @return a valid IDARE identifier
	 */
	
	public long getNextProteinComplexID()
	{
		maxProteinComplexID = maxProteinComplexID+1;
		proteinComplexIDs.add(maxProteinComplexID);
		return maxProteinComplexID;
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
		proteinIDs = new HashSet<Long>();
		maxProteinID = new Long(0);
		maxProteinComplexID = new Long(0);
		proteinComplexIDs = new HashSet<Long>();
		geneIDs = new HashSet<Long>();
		maxGeneID = new Long(0);
	}
	
	/**
	 * Add an ID to this manager. The manager checks whether this ID is already used.
	 * @param id - The id that needs to be checked
	 * @throws IllegalArgumentException - If the provided ID is already present in this Manager this exception is thrown but the Manager is still valid 
	 */
	private synchronized void addID(Long id) throws IllegalArgumentException
	{
		if(id == null)
		{
			return;
		}
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
	 * Add a protein ID to this manager. The manager checks whether this ID is already used.
	 * @param id - The id that needs to be checked
	 * @throws IllegalArgumentException - If the provided ID is already present in this Manager this exception is thrown but the Manager is still valid 
	 */
	private synchronized void addProteinID(Long id) throws IllegalArgumentException
	{
		if(id == null)
		{
			return;
		}
		if(proteinIDs.contains(id)) {
			throw new IllegalArgumentException("ID already used");
		}
		else
		{
			if(id.compareTo(maxProteinID) > 0)
			{
				maxProteinID = id;
			}
			proteinIDs.add(id);
		}
	}
	

	/**
	 * Add an gene ID to this manager. The manager checks whether this ID is already used.
	 * @param id - The id that needs to be checked
	 * @throws IllegalArgumentException - If the provided ID is already present in this Manager this exception is thrown but the Manager is still valid 
	 */
	private synchronized void addGeneID(Long id) throws IllegalArgumentException
	{
		if(id == null)
		{
			return;
		}
		if(geneIDs.contains(id)) {
			throw new IllegalArgumentException("ID already used");
		}
		else
		{
			if(id.compareTo(maxGeneID) > 0)
			{
				maxGeneID = id;
			}
			geneIDs.add(id);
		}
	}
	
	/**
	 * Add an ID to this manager. The manager checks whether this ID is already used.
	 * @param id - The id that needs to be checked
	 * @throws IllegalArgumentException - If the provided ID is already present in this Manager this exception is thrown but the Manager is still valid 
	 */
	private synchronized void addProteinComplexID(Long id) throws IllegalArgumentException
	{
		if(id == null)
		{
			return;
		}
		if(proteinComplexIDs.contains(id)) {
			throw new IllegalArgumentException("ID already used");
		}
		else
		{
			if(id.compareTo(maxProteinComplexID) > 0)
			{
				maxProteinComplexID = id;
			}
			proteinComplexIDs.add(id);
		}
	}
	
	/**
	 * Add a network ID to this manager. The manager checks whether this ID is already used.
	 * @param id - The id that needs to be checked
	 * @throws IllegalArgumentException - If the provided ID is already present in this Manager this exception is thrown but the Manager is still valid 
	 */
	private synchronized void addNetworkID(Long id) throws IllegalArgumentException
	{
		if(id == null)
		{
			return;
		}
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
		HashSet<Long> checkedNodes = new HashSet<>();
		for(CyNetwork network : networks)
		{					
			if(isSetupNetwork(network))
			{
				CyTable NodeTable = cyNetTableMgr.getTable(network, CyNode.class, IDAREProperties.IDARE_NAMESPACE);
				CyTable NetworkTable = cyNetTableMgr.getTable(network, CyNetwork.class, IDAREProperties.IDARE_NAMESPACE);				
				for(CyRow row : NodeTable.getAllRows())
				{
					if(!checkedNodes.contains(row.get(CyNode.SUID, Long.class)))
					{
						checkedNodes.add(row.get(CyNode.SUID, Long.class));
					}
					else
					{
						//this node was already checked.
						continue;
					}
					Long IDAREID = row.get(IDAREProperties.ColumnHeaders.IDARE_NODE_UID.toString(), Long.class);
					Long IDAREDUPLICATEID = row.get(IDAREProperties.ColumnHeaders.IDARE_ORIGINAL_NODE.toString(), Long.class);
					CyNode current = network.getNode(row.get(CyNode.SUID,Long.class));
					if(current != null && IDAREDUPLICATEID != null)
					{						
						try
						{
							addID(IDAREDUPLICATEID);
						}
						catch(IllegalArgumentException e)
						{
							if(nodematch.containsKey(IDAREDUPLICATEID))
							{
								//Ok, we got a problem. we have a node with the ID while it is also duplicated. This should not happen!
								throw(e);
							}
							//Otherwise this is ok. 
						}

					}						
					//this happens, if the node is not in this network. As we get the default NOde Table it contains ALL Nodes...) 
					if(current == null || IDAREID == null)
					{
						continue;
					}					
					if(!nodematch.containsKey(IDAREID))
					{
						try{
							addID(IDAREID);
							nodematch.put(IDAREID, current);
						}
						catch(IllegalArgumentException e)
						{
							//this can happen, if we have a duplicated ID added and then encounter a "normal" id.
							resetNetworks(networks,cyNetTableMgr,cyTableMgr);
							reset();
							return;
						}
					}
					else
					{
						//if this is not pointing to the same node, we have to reset the whole IDARE Setup, i.e. we will clear the whole network from Cytoscape Columns.
						//PrintFDebugger.Debugging(this, "Determining whether " + nodematch.get(IDAREID) + " has the same SUID as " + current);
						if(!nodematch.get(IDAREID).getSUID().equals(current.getSUID()))
						{
//							PrintFDebugger.Debugging(this, "Found a duplicate node id: " + IDAREID + " ... removing all IDARE Columns");
							resetNetworks(networks,cyNetTableMgr,cyTableMgr);
							reset();
							return;
						}
						else
						{
							// we can skip the remaining checks as they were already done once.
							continue;
						}
					}
					
					if(NodeTable.getColumn(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString()) != null)
					{
						String nodename = row.get(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), String.class);						
						if(nodename != null)
						{
							Matcher protMatch = proteinPattern.matcher(nodename);
							if(protMatch.matches())
							{								
								addProteinID(Long.parseLong(protMatch.group(0)));
							}
							Matcher protComplexMatch = proteinComplexPattern.matcher(nodename);
							if(protComplexMatch.matches())
							{
								addProteinComplexID(Long.parseLong(protComplexMatch.group(0)));
							}
							Matcher geneMatch = genePattern.matcher(nodename);
							if(geneMatch.matches())
							{
								addGeneID(Long.parseLong(geneMatch.group(0)));
							}
						}
						
						
					}
				}
				Long NetworkID = NetworkTable.getRow(network.getSUID()).get(IDAREProperties.ColumnHeaders.IDARE_NETWORK_ID.toString(), Long.class);
				if(NetworkID == null)
				{
					continue;
				}
				if(!networkmatch.containsKey(NetworkID))
				{
					networkmatch.put(NetworkID, network);
					addNetworkID(NetworkID);
				}
				else
				{
					if(!networkmatch.get(NetworkID).getSUID().equals(network.getSUID()))
					{
//						PrintFDebugger.Debugging(this, "Found a duplicate network id: " + NetworkID + " ... removing all IDARE Columns");
						resetNetworks(networks,cyNetTableMgr,cyTableMgr);
						reset();						
						return;
					}
				}
			}
		}
	}
	
	public static void resetNetworks(Collection<CyNetwork> networks, CyNetworkTableManager cyNetTableMgr, CyTableManager cyTabMgr)
	{
		for(CyNetwork network : networks)
		{
			
			if(isSetupNetwork(network))
			{
				CyTable NodeTable = cyNetTableMgr.getTable(network, CyNode.class, IDAREProperties.IDARE_NAMESPACE);
				CyTable NetworkTable = cyNetTableMgr.getTable(network, CyNetwork.class, IDAREProperties.IDARE_NAMESPACE);				
				CyTable EdgeTable = cyNetTableMgr.getTable(network, CyEdge.class, IDAREProperties.IDARE_NAMESPACE);
				for(IDAREProperties.ColumnHeaders header : IDAREProperties.NodeHeaders )
				{
					if(NodeTable.getColumn(header.toString()) != null)
					{
						NodeTable.deleteColumn(header.toString());
					}
				}
				for(IDAREProperties.ColumnHeaders header : IDAREProperties.EdgeHeaders )
				{
					if(EdgeTable.getColumn(header.toString()) != null)
					{
						EdgeTable.deleteColumn(header.toString());
					}
				}
				
				for(IDAREProperties.ColumnHeaders header : IDAREProperties.NetworkHeaders )
				{
					if(NetworkTable.getColumn(header.toString()) != null)
					{
						NetworkTable.deleteColumn(header.toString());
					}
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
	public static void SetupNetwork(CyNetwork network, IDARESettingsManager IDAREIdmgr, String NodeTypeCol, String IDCol, CyServiceRegistrar reg)
		{
		CyTable NodeTable = network.getDefaultNodeTable();
		CyTable EdgeTable = network.getDefaultEdgeTable();
		CyTable NetworkTable = network.getDefaultNetworkTable();
		
		CyTable IDARENodeTable = getIDARETable(network, reg, CyNode.class);
		CyTable IDAREEdgeTable = getIDARETable(network, reg, CyEdge.class);
		CyTable IDARENetworkTable = getIDARETable(network, reg, CyNetwork.class);
		
		initNetwork(network,reg);
		if(!NetworkTable.getRow(network.getSUID()).isSet(IDAREProperties.ColumnHeaders.IDARE_NETWORK_ID.toString()))
		{
			NetworkTable.getRow(network.getSUID()).set(IDAREProperties.ColumnHeaders.IDARE_NETWORK_ID.toString(),IDAREIdmgr.getNextNetworkID());
		}
		List<CyRow> NodeRows = NodeTable.getAllRows();
		
		for(CyRow row : NodeRows)
		{
			// if the column is either not set, or at the default value, initialize it.
			//furthermore we can initialize the IDARE_TARGET_ID property for save/restore functionality
			//Only assign IDs to non duplicated nodes.
			CyRow IDARERow = IDARENodeTable.getRow(row.get(CyNode.SUID,Long.class));
			if(!IDARERow.isSet(IDAREProperties.ColumnHeaders.IDARE_NODE_UID.toString()) & !IDARERow.get(IDAREProperties.ColumnHeaders.IDARE_DUPLICATED_NODE.toString(), Boolean.class))
			{
				//row.set(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(), row.get(NodeTypeCol,String.class));				
				Long id = IDAREIdmgr.getNextNodeID();
				IDARERow.set(IDAREProperties.ColumnHeaders.IDARE_NODE_UID.toString(), id);

			}
			if(!IDARERow.isSet(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString()))
			{				
				
				IDARERow.set(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(),IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
			}
			else
			{
				//if the value is not null but still the default, than also set it to the type value.
				if(IDARERow.get(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(),String.class) != null && 
						IDARERow.get(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(),String.class).equals(IDARENodeTable.getColumn(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString()).getDefaultValue()))
				{
					if(!IDARERow.get(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(),String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
					{
						//never change a linker node type.
						IDARERow.set(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(),IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
					}
				}
			}
						
			if(!IDARERow.isSet(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString()) && NodeTable.getColumn(IDCol) != null)
			{
				if(!IDARERow.isSet(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString()) || !IDARERow.get(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(),String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
				{
					IDARERow.set(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), row.get(IDCol,String.class));
				}
			}
			else if (IDARENodeTable.getColumn(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString()) != null &&
					(IDARENodeTable.getColumn(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString()).getDefaultValue() == null ||
					IDARENodeTable.getColumn(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString()).getDefaultValue().equals(IDARERow.get(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), String.class))))
			{
				IDARERow.set(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), row.get(IDCol,String.class));
			}
		}
		List<CyRow> EdgeRows = EdgeTable.getAllRows();
		for(CyRow row: EdgeRows)
		{
			CyRow IDARErow = IDAREEdgeTable.getRow(row.get(CyEdge.SUID, Long.class));
			if(!IDARErow.isSet(IDAREProperties.ColumnHeaders.IDARE_EDGE_PROPERTY.toString()) ||
					IDARErow.get(IDAREProperties.ColumnHeaders.IDARE_EDGE_PROPERTY.toString(), String.class).equals(IDAREEdgeTable.getColumn(IDAREProperties.ColumnHeaders.IDARE_EDGE_PROPERTY.toString()).getDefaultValue()))
			{
				if(row.isSet(IDAREProperties.SBML_EDGE_TYPE))
				{
					IDARErow.set(IDAREProperties.ColumnHeaders.IDARE_EDGE_PROPERTY.toString(), row.get(IDAREProperties.SBML_EDGE_TYPE, String.class));
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
	public static void SetNetworkData(CyNetwork network, IDARESettingsManager IDAREIdmgr, String NodeTypeCol, String IDCol, boolean overwrite , NodeManager nm, CyServiceRegistrar reg)
	{
		if(!isSetupNetwork(network))
		{
			SetupNetwork(network, IDAREIdmgr, NodeTypeCol, IDCol, reg);
		}
		CyTable NodeTable = network.getDefaultNodeTable();
		CyTable NetworkTable = network.getDefaultNetworkTable();				
		if(!NetworkTable.getRow(network.getSUID()).isSet(IDAREProperties.ColumnHeaders.IDARE_NETWORK_ID.toString()))
		{
			NetworkTable.getRow(network.getSUID()).set(IDAREProperties.ColumnHeaders.IDARE_NETWORK_ID.toString(),IDAREIdmgr.getNextNetworkID());
		}
		List<CyRow> NodeRows = NodeTable.getAllRows();
		Set<String> IDAREIDs = new HashSet<String>();
		for(CyRow row : NodeRows)
		{
			//if the column is either not set, or at the default value, initialize it.
			//furthermore we can initialize the IDARE_TARGET_ID property for save/restore functionality
			if(!row.isSet(IDAREProperties.ColumnHeaders.IDARE_NODE_UID.toString()))
			{
				//row.set(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(), row.get(NodeTypeCol,String.class));
				Long id = IDAREIdmgr.getNextNodeID();
				row.set(IDAREProperties.ColumnHeaders.IDARE_NODE_UID.toString(), id);

			}			
			//if the row is not set, or is equal to null or is at the default, set it to an updated value.
			if(!row.isSet(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString()))
			{
				row.set(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(),IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
			}
			if(overwrite)
			{
				//PrintFDebugger.Debugging(IDAREIdmgr, "Asking Property manager to obtain value for entry " + row.get(NodeTypeCol, String.class) + " it returned " + IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
				if(!row.get(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(),String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
				{
					row.set(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(),IDAREIdmgr.getType(row.get(NodeTypeCol, String.class)));
				}
			}
			if(!row.isSet(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString()) || !row.get(IDAREProperties.ColumnHeaders.IDARE_NODE_TYPE.toString(),String.class).equals(IDAREProperties.NodeType.IDARE_LINK))
			{
				//DO NOT UPDATE LINKER NODE NAMES!
				row.set(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), row.get(IDCol,String.class));
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
		
		CyTable NodeTable = network.getTable(CyNode.class, IDAREProperties.IDARE_NAMESPACE);
		CyTable EdgeTable = network.getTable(CyEdge.class, IDAREProperties.IDARE_NAMESPACE);
		CyTable NetworkTable = network.getTable(CyNetwork.class, IDAREProperties.IDARE_NAMESPACE);

		//if any of the tables does not exist, the network is not set up.
		if(NodeTable == null || EdgeTable == null || NetworkTable == null)
		{
			return false;
		}
		//And if a column does not exist its not set up neither.
		for(ColumnHeaders header : IDAREProperties.NodeHeaders)
		{
			if(NodeTable.getColumn(header.toString()) == null)
			{
				return false;
			}
		}
		for(ColumnHeaders header : IDAREProperties.EdgeHeaders)
		{
			if(EdgeTable.getColumn(header.toString()) == null)
			{
				return false;
			}
		}
		for(ColumnHeaders header : IDAREProperties.NetworkHeaders)
		{
			if(NetworkTable.getColumn(header.toString()) == null)
			{
				return false;
			}
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
		CyTable IDAREnodeTable = network.getTable(CyNode.class, IDAREProperties.IDARE_NAMESPACE);
		Set<CyRow> matchingrows = new HashSet<CyRow>();
		for(String id : IDs)
		{
			matchingrows.addAll(nodeTable.getMatchingRows(ColForNames, id));
		}
		for(CyRow row : matchingrows)
		{			
			IDAREnodeTable.getRow(row.get(CyNode.SUID,Long.class)).set(IDAREProperties.ColumnHeaders.IDARE_NODE_NAME.toString(), row.get(ColForNames,String.class));		
		}
	}
	
	/**
	 * Set up the provided network using the Provided IDCol for the IDARE Names and the NodeTypesCol for the IDARE Node Types
	 * @param network - the network to set up
	 */
	public static void initNetwork(CyNetwork network, CyServiceRegistrar reg)
	{
		
		CyTable IDARENodeTable = getIDARETable(network, reg, CyNode.class);
		CyTable IDAREEdgeTable = getIDARETable(network, reg, CyEdge.class);
		CyTable IDARENetworkTable = getIDARETable(network, reg, CyNetwork.class);
		
		for(ColumnHeaders header : IDAREProperties.NodeHeaders )
		{
			try{
				IDARENodeTable.createColumn(header.toString(), header.getType(),false, header.getdefaultValue());
			}
			catch(IllegalArgumentException e)
			{
				e.printStackTrace(System.out);
			}
		}

		for(ColumnHeaders header : IDAREProperties.EdgeHeaders )
		{
			try{
				IDAREEdgeTable.createColumn(header.toString(), header.getType(),false, header.getdefaultValue());
			}
			catch(IllegalArgumentException e)
			{
				e.printStackTrace(System.out);
			}
		}
		for(ColumnHeaders header : IDAREProperties.NetworkHeaders )
		{
			try{
				IDARENetworkTable.createColumn(header.toString(), header.getType(),false, header.getdefaultValue());
			}
			catch(IllegalArgumentException e)
			{
				e.printStackTrace(System.out);
			}
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
	
	/**
	 * 
	 */
	public static CyTable getIDARETable(CyNetwork network, CyServiceRegistrar reg, Class<? extends CyIdentifiable> type)
	{
		CyTable IDARETable = network.getTable(type, IDAREProperties.IDARE_NAMESPACE);
		if(IDARETable == null)
		{
			IDARETable = reg.getService(CyTableFactory.class).createTable("IDARE_TABLE", "SUID", type, false, true);			
			reg.getService(CyTableManager.class).addTable(IDARETable);
			//Associate the table with the root network (Maybe we have to add it to all individual subnetworks, but for now, try it like this)
			reg.getService(CyNetworkTableManager.class).setTable(reg.getService(CyRootNetworkManager.class).getRootNetwork(network), type, IDAREProperties.IDARE_NAMESPACE, IDARETable);
		}
		return IDARETable;
	}
}
