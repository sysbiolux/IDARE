package idare.imagenode.internal.ImageManagement;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualStyleSetEvent;
import org.cytoscape.view.vizmap.events.VisualStyleSetListener;

import idare.Properties.IDAREProperties;
import idare.imagenode.internal.DataManagement.NodeManager;
import idare.imagenode.internal.Debug.PrintFDebugger;

public class ActiveNodeManager implements VisualStyleSetListener{

	private HashMap<String,Integer> VisualCounts = new HashMap<String, Integer>();
	private VisualMappingManager vmmServiceRef;
	private Set<String> stylesWithNodes = new HashSet<String>();
	private HashMap<CyNetworkView,VisualStyle> currentStyles = new HashMap<>();
	private CyNetworkViewManager cyNetViewMgr;	
	private NodeManager nm;
	
	public ActiveNodeManager(CyNetworkViewManager cyNetViewMgr, VisualMappingManager vmmServiceRef)
	{
		this.cyNetViewMgr = cyNetViewMgr;
		this.vmmServiceRef = vmmServiceRef;
		updateVisualCounts();
	}
	
	/**
	 * This function will update ALL visual counts, completely dropping the current visualCounts.
	 */
	public void updateVisualCounts() 
	{
		VisualCounts = new HashMap<>();
		for(CyNetworkView networkView : cyNetViewMgr.getNetworkViewSet())
		{
			VisualStyle cvs = vmmServiceRef.getVisualStyle(networkView);	
			if(stylesWithNodes.contains(cvs.getTitle()))
			{
				Set<String> nodeIDs = getNetworkViewIDs(networkView);
				addIDs(nodeIDs);
			}
		}
	}
	
	/**
	 * 
	 */
	public Collection<String> getActiveNodeIDs()
	{
		return VisualCounts.keySet();
	}
	@Override
	public void handleEvent(VisualStyleSetEvent arg0) {
		// TODO Auto-generated method stub
		CyNetworkView changedView = arg0.getNetworkView();
		VisualStyle newStyle = arg0.getVisualStyle();
		PrintFDebugger.Debugging(this, "Got a New Visual Style set event.");
		if(!currentStyles.containsKey(changedView))
		{
			if(stylesWithNodes.contains(newStyle.getTitle()))
			{
				//Yes, we need to add the nodes of this network.
				Set<String> nodeIDs = getNetworkViewIDs(changedView);
				addIDs(nodeIDs);
				if(nm != null)
				{
					nm.updateActiveNodes(nodeIDs);
				}
			}
			currentStyles.put(changedView, newStyle);			
		}
		else
		{
			VisualStyle oldStyle = currentStyles.get(changedView);
			if(stylesWithNodes.contains(oldStyle.getTitle()))
			{
				if(!stylesWithNodes.contains(newStyle.getTitle()))
				{
					Set<String> nodeIDs = getNetworkViewIDs(changedView);
					removeIDs(nodeIDs);
				}
			}
			else
			{
				if(stylesWithNodes.contains(newStyle.getTitle()))
				{
					Set<String> nodeIDs = getNetworkViewIDs(changedView);
					addIDs(nodeIDs);
					if(nm != null)
					{
						nm.updateActiveNodes(nodeIDs);
					}
				}
			}
			
		}
	}
	
	private void addIDs(Set<String> nodeIDs)
	{
		for(String id : nodeIDs)
		{
			if(!VisualCounts.containsKey(id))
			{
				VisualCounts.put(id, 1);
			}
			else
			{
				VisualCounts.put(id, VisualCounts.get(id) + 1);
			}
		}
	}
	
	private void removeIDs(Set<String> nodeIDs)
	{
		for(String id : nodeIDs)
		{
			if(VisualCounts.containsKey(id))
			{
				Integer newCount = VisualCounts.get(id) -1;
				if(newCount <= 0)
				{
					VisualCounts.remove(id);
				}
				else
				{
					VisualCounts.put(id, newCount);
				}
			}
		}
	}
	
	public void setNodeManager(NodeManager nm)
	{
		this.nm = nm;
	}
	
	public void addStyleUsingNodes(String Style)
	{				
		if(!stylesWithNodes.contains(Style))
		{
			PrintFDebugger.Debugging(this, "Style " + Style + " was added to the Styles showing Images.");
			stylesWithNodes.add(Style);
			HashMap<String,Integer> newMap = getStyleNodes(Style);
			for(String id : newMap.keySet())
			{
				if(!VisualCounts.containsKey(id))
				{
					VisualCounts.put(id, 0);
				}
				VisualCounts.put(id, VisualCounts.get(id) + newMap.get(id) );
			}
			PrintFDebugger.Debugging(this, "There are now " + VisualCounts.size() + " Images being watched");
			if(nm != null)
			{
				nm.updateActiveNodes(newMap.keySet());
			}
		}		
		
	}
	
	public void removeStyleUsingNodes(String Style)
	{
		if(stylesWithNodes.contains(Style))
		{
			stylesWithNodes.remove(Style);
			HashMap<String,Integer> newMap = getStyleNodes(Style);
			for(String id : newMap.keySet())
			{
				if(VisualCounts.containsKey(id))
				{
					Integer newValue =  VisualCounts.get(id) - newMap.get(id);
					if(newValue <= 0)
					{
						VisualCounts.remove(id);
					}
					else
					{
						VisualCounts.put(id, newValue);
					}					
				}
				
			}
		}
	}
	
	private Set<String> getNetworkViewIDs(CyNetworkView view)
	{
		Set<String> nodeIDs = new HashSet<>(); 
		CyNetwork network = view.getModel();
		CyTable nodeTable = network.getDefaultNodeTable();
		if(network.getDefaultNodeTable().getColumn(IDAREProperties.IDARE_NODE_NAME) != null) //if this is an idare node
		{
			for(CyRow row : nodeTable.getAllRows())
			{
				String id = row.get(IDAREProperties.IDARE_NODE_NAME, String.class);
				if(id == null || id.isEmpty())
				{
					continue;
				}
				else
				{
					nodeIDs.add(id);
				}
			}
		}
		return nodeIDs;
	}
		
	private HashMap<String,Integer> getStyleNodes(String Style)
	{
		HashMap<String,Integer> resultMap = new HashMap<>();
		//Generate all Node Images, if they don't exist				
		for(CyNetworkView networkView : cyNetViewMgr.getNetworkViewSet())
		{			
			VisualStyle cvs = vmmServiceRef.getVisualStyle(networkView);			
			if(cvs.getTitle().equals(Style))
			{
				CyNetwork network = networkView.getModel();
				CyTable nodeTable = network.getDefaultNodeTable();
				if(network.getDefaultNodeTable().getColumn(IDAREProperties.IDARE_NODE_NAME) != null) //if this is an idare node
				{
					for(CyRow row : nodeTable.getAllRows())
					{
						String id = row.get(IDAREProperties.IDARE_NODE_NAME, String.class);
						if(id == null || id.isEmpty())
						{
							continue;
						}
						if(!resultMap.containsKey(id))
						{
							resultMap.put(id, new Integer(0));
						}
						resultMap.put(id, resultMap.get(id) + 1);	
					}
				}
			}
		}
		return resultMap;
	}
	
}
