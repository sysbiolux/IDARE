package idare.metanode.internal.GUI.Legend;

import idare.Properties.IDAREProperties;
import idare.metanode.internal.DataManagement.NodeManager;
import idare.metanode.internal.VisualStyle.IDAREVisualStyle;
import idare.metanode.internal.VisualStyle.StyleManager;

import java.util.Collection;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 * A Class to keep the legend up to date with the current Row Selection. 
 * @author Thomas Pfau
 *
 */
public class LegendUpdater implements RowsSetListener{

	private boolean active = false;
	private IDARELegend legend;
	private NodeManager nm;
	private  final CyApplicationManager appmgr;
	private VisualMappingManager vmmServiceRef;
	private StyleManager styleMgr;
	/**
	 * Default contstructor using the target {@link IDARELegend}, the NodeManager to check whether there are layouts for the node, 
	 * The Style Manager to determine, whether a Style is using ImageNodes and Cytoscape managers to deterine the properties of visual styles and nodes. 
	 * @param target - The {@link IDARELegend} which this updated updates.
	 * @param nm - the nodemanager where the updater checks for layouts.
	 * @param appmgr - the Application MAnager to determine the current view 
	 * @param vmm - the VisualStyle Manager to determine the Style of the current view
	 * @param mgr - the StyleManager that handles the image node to Style settings.
	 */
	public LegendUpdater(IDARELegend target, NodeManager nm, CyApplicationManager appmgr, VisualMappingManager vmm, StyleManager mgr) {
		// TODO Auto-generated constructor stub
		this.nm = nm;
		this.legend = target;
		this.appmgr = appmgr;
		vmmServiceRef = vmm;
		this.styleMgr = mgr;
	}
	/**
	 * Activate the legend updating method
	 */
	public void activate()
	{
		active = true;
	}
	/**
	 * Inactivate the updating method
	 */
	public void disable()
	{
		active = false;
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {		
		//Lets just test whether we have exactly one Node Row that is SELECTED now.
		//This function updates the Legend Panel to show the Legend for the current node.
		if(active)
		{
			//PrintFDebugger.Debugging(this,"Got a new RowsSet event");
			if(e.containsColumn(CyNetwork.SELECTED))
			{
				//PrintFDebugger.Debugging(this,"Some Rows were selected");
				Collection<CyRow> selrows = e.getSource().getMatchingRows(CyNetwork.SELECTED, true);
				Vector<String> SelectedNodeIDs = new Vector<String>();
				for(CyRow row : selrows)
				{
					String IDAREType = row.get(IDAREProperties.IDARE_NODE_TYPE,String.class);
					//The only nodes we want to exclude are nodes which do not have an IDARE Type or which are Links.
					//Anything else is fine and can be matched.
					if(IDAREType != null && !(IDAREType.equals(IDAREProperties.NodeType.IDARE_LINK)))
					{
						String IDAREID = row.get(IDAREProperties.IDARE_NODE_NAME, String.class);
						if(nm.isNodeLayouted(IDAREID))
						{
							SelectedNodeIDs.add(IDAREID);
						}
					}
				}
				CyNetworkView currentView = appmgr.getCurrentNetworkView();
				VisualStyle currentVs = vmmServiceRef.getVisualStyle(currentView);
				//PrintFDebugger.Debugging(this,"There are " + SelectedNodeIDs.size() + " selected nodes");
				//check whether the current view has an IDARE Style or part of the styles with nodes
				if(!SelectedNodeIDs.isEmpty() && (styleMgr.styleUsed(currentVs.getTitle()) | IDAREVisualStyle.IDARE_STYLE_TITLE.equalsIgnoreCase(currentVs.getTitle())))					
				{
					updateLegend(SelectedNodeIDs.firstElement());
				}	
				else
				{
					legend.reset();
				}
			}
		}
	}
	
	public void updateLegend(String NodeID)
	{		
		legend.setLegendNode(NodeID);
	}
	
	
}
