package idare.Properties;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.session.events.SessionLoadedEvent;

/**
 * Class to keep track of IDARE IDs and complain (throw exceptions) if duplicate IDs are found. hopefully there wont be any instances with > max(long) nodes in one network...
 * 
 * @author thomas
 *
 */
public class IDARESettingsManager {
	
	private HashSet<Long> IDs;
	private Long MaxID = new Long(0); 
	private IDAREProperties properties;
	private HashMap<String,String> SubNetworkTypes;	
	/**
	 * Initialize the IDare Manager
	 */
	public IDARESettingsManager()
	{
		properties = new IDAREProperties();
		IDs = new HashSet<Long>();
		MaxID = new Long(0);
		SubNetworkTypes = new HashMap<String, String>();
	}
	/**
	 * Get the next ID which is the largest current ID + 1
	 * This Id should be used otherwise it will be blocked for this manager.
	 * @return a valid IDARE identifier
	 */
	
	public long getNextID()
	{
		MaxID = MaxID+1;
		IDs.add(MaxID);
		return MaxID;
	}
	/**
	 * Reset this Manager. This invalidates all IDs formerly provided by this manager.
	 * All IDARE objects/nodes should therefore recieve a new ID once this function is called.
	 */
	public void reset()
	{
		IDs = new HashSet<Long>();
		MaxID = new Long(0);
	}
	
	/**
	 * Add an ID to this manager. The manager checks whether this ID is already used.
	 * @param id - The id that needs to be checked
	 * @throws IllegalArgumentException - If the provided ID is already present in this Manager this exception is thrown but the Manager is still valid 
	 */
	public void addID(Long id) throws IllegalArgumentException
	{
		if(IDs.contains(id)) {
			throw new IllegalArgumentException("ID already used");
		}
		else
		{
			if(id.compareTo(MaxID) > 0)
			{
				MaxID = id;
			}
			IDs.add(id);
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
			if(IDs.contains(id))
			{
				throw new IllegalArgumentException("ID already used");
			}
		}

		for(Long id : ids)
		{
			if(id > MaxID)
			{
				MaxID = id;
			}
			IDs.add(id);
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
	
	
	public void handleSessionLoadedEvent(SessionLoadedEvent e)
	{
		//Reset the maxID
		MaxID = 0L;
		//And Obtain it back from the network. 
		for(CyNetwork Network: e.getLoadedSession().getNetworks())
		{
			CyTable NodeTable = Network.getDefaultNodeTable();
			if(NodeTable.getColumn(IDAREProperties.IDARE_NODE_UID) != null)
			{
				for(CyRow row : NodeTable.getAllRows())
				{
					if(row.isSet(IDAREProperties.IDARE_NODE_UID))
					{
						Long val = row.get(IDAREProperties.IDARE_NODE_UID, Long.class);
						MaxID = Math.max(val, MaxID);
					}
				}
					
			}
		}
			
	}
}
