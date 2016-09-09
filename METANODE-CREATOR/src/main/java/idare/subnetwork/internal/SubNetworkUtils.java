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

	/**
	 * Get a Vector of SubSystem Identifiers from the given CyTable and the given Column name (i.e. all different entries in that column)
	 * @param table - The table to look up
	 * @param ColName - The String identifying the Column to look up the different subsystem identifiers 
	 * @return a {@link Vector} of column Identifiers
	 */
	public static Vector<Object> getDifferentSubSystems(CyTable table, String ColName)
	{
		Vector<Object> SubsystemTypes = new Vector<Object>();		
		List<CyRow> rows = table.getAllRows();
		//we do not need to make any difference between the objects since they are simply comparable by equals();
		//lets get the Column first.
		CyColumn col = table.getColumn(ColName);
		if(col == null)
		{
			return SubsystemTypes;
		}
		if( col.getListElementType() == null)
		{
			//this is a "normal" column, so we can just go on. 
			for(CyRow row : rows)
			{				
				if(row.isSet(ColName))
				{
					if(row.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) == null || !row.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION))
					{
						//skip anything that is not a species for subnetwork selection, as anything beside species will not be allowed.
						continue;
					}
					if(SubNetworkUtils.isempty(row.get(ColName, table.getColumn(ColName).getType())))
					{
						continue;
					}
					if(!SubsystemTypes.contains(row.get(ColName, table.getColumn(ColName).getType())))
					{
						SubsystemTypes.add(row.get(ColName, table.getColumn(ColName).getType()));
					}

				}			
			}	
		}
		else
		{			
			//this is a list column, so we need to get all List elements and add them to the SubSystem Types 

			for(CyRow row : rows)
			{
				if(row.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class) == null || !row.get(IDAREProperties.IDARE_SUBNETWORK_TYPE, String.class).equals(IDAREProperties.NodeType.IDARE_REACTION))
				{
					//skip anything that is not a species for subnetwork selection, as anything beside species will not be allowed.
					continue;
				}

				if(row.isSet(ColName))
				{
					for(Object item : row.getList(ColName, table.getColumn(ColName).getListElementType()))
					{
						if(SubNetworkUtils.isempty(item))
						{
							continue;
						}
						if(!SubsystemTypes.contains(item))
						{
							SubsystemTypes.add(item);
						}
					}					
				}			
			}	
		}
		return SubsystemTypes;
	}



	/**
	 * Check whether the entry of Column ColName in CyRow row is empty. I.e. it is either null or its string representation is an empty string.
	 * @param row - The row in question
	 * @param ColName -  the Column to check.
	 * @return Whether the column is empty in the given row
	 */
	public static boolean isempty(CyRow row, String ColName)
	{
		//we will assume, that a null value means it is empty. If its not null, we will check 
		if(row.getRaw(ColName) != null)
		{
			//Anything that is not "" or null will be considered as not empty. 
			if(row.getRaw(ColName).toString() != "")
			{
				return false;
			}			
		}
		return true;
	}

	/**
	 * Check whether a given Object is either empty or an empty String
	 * @return Whether the entry is empty or represented by an empty string
	 */
	public static boolean isempty(Object Entry)
	{
		//we will assume, that a null value means it is empty. If its not null, we will check 
		if(Entry != null)
		{
			//Anything that is not "" or null will be considered as not empty. 
			if(!Entry.toString().equals(""))
			{
				return false;
			}			
		}
		return true;
	}
}
