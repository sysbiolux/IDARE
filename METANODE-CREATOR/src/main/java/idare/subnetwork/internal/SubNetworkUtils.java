package idare.subnetwork.internal;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;

import java.util.List;
import java.util.Vector;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class SubNetworkUtils {

	
	/**
	 *  Get all Columns present in the node Table of the provided network
	 */
	public static Vector<String> getNodeTableColumnNames(CyNetwork network)
	{
		Vector<String> columnNames = new Vector<String>();
		for(CyColumn col : network.getDefaultNodeTable().getColumns())
		{
			columnNames.add(col.getName());
		}
		return columnNames;
	}
	
	/**
	 * Set up the Network for SubNetwork Generation, i.e. set the subnetwork Type column to the values translated from the specified NodeTypeColumn. 
	 * @param network - the network to set up
	 * @param Idmgr - the ID Manager
	 * @param NodeTypeCol - The NodeType Column name
	 */
	public static void setupNetworkForSubNetworkCreation(CyNetwork network, IDARESettingsManager Idmgr, String NodeTypeCol)
	{
	CyTable NodeTable = network.getDefaultNodeTable();
	CyTable EdgeTable = network.getDefaultEdgeTable();
	if(!IDARESettingsManager.isSetupNetwork(network))
	{
		IDARESettingsManager.initNetwork(network);
	}
	List<CyRow> NodeRows = NodeTable.getAllRows();
	for(CyRow row : NodeRows)
	{
		//if the column is either not set, or at the default value, initialize it.
		//furthermore we can initialize the IDARE_TARGET_ID property for save/restore functionality
		if(!row.isSet(IDAREProperties.IDARE_NODE_UID))
		{
			Long id = Idmgr.getNextNodeID();
			row.set(IDAREProperties.IDARE_NODE_UID, id);
		}			
		//Always update to the current value!
		
		row.set(IDAREProperties.IDARE_SUBNETWORK_TYPE,Idmgr.getSubNetworkType(row.get(NodeTypeCol, String.class)));
		
		//row.set(IDAREProperties.IDARE_NODE_NAME, row.get(IDCol,String.class));			
		//IDAREIDs.add(row.get(IDCol, String.class));
	}		
}

}
