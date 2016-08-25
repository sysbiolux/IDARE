package idare.imagenode.internal.GUI.NetworkSetup.Tasks;

import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.DataManagement.NodeManager;

import org.cytoscape.model.CyNetwork;

public class NetworkSetupProperties {

	/**
	 * IDs for the individual types
	 */
	public String ProteinID;
	public String CompoundID;
	public String InteractionID;
	public String GeneID;
	
	/**
	 * IDs of the columns to use for names and types
	 */
	public String TypeColID;
	public String IDColID;
	
	/**
	 * Whether to overwrite data
	 */
	public boolean overwrite;	
	
	/**
	 * The Target network to set up
	 */
	public CyNetwork network;
	
	/**
	 * The managers necessary to keep track/update IDs etc pp
	 */
	public IDARESettingsManager mgr;
	public NodeManager nm;
	
	public String toString()
	{
		String res = "";
		res += "ProteinID: " + ProteinID;
		res += "\nCompoundID: " + CompoundID;
		res += "\nInteractionID: " + InteractionID;
		res += "\nGeneID: " + GeneID;
		res += "\nTypeColID: " + TypeColID;
		res += "\nIDColID: " + IDColID;
		res += "\noverwrite: " + overwrite;
		res += "\nnetwork: " + network;
		res += "\nmgr: " + mgr;
		res += "\nnm: " + nm;
		return res;
	}
}
