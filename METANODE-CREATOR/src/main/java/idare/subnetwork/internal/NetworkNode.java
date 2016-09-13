package idare.subnetwork.internal;

import idare.Properties.IDAREProperties;
import idare.Properties.IDARESettingsManager;
import idare.imagenode.internal.Debug.PrintFDebugger;
import idare.subnetwork.internal.Tasks.SubsystemGeneration.SubnetworkCreationTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

public class NetworkNode implements Serializable{
	public NetworkNode parent;
	private CyNetwork networkreference;
	public String colName;	
	public String networkID;
	public Long networkIDAREID;
	private Vector<NetworkNode> children = new Vector<NetworkNode>();	
	public NetworkNode(NetworkNode parent, CyNetwork reference, String ColName, String networkID, IDARESettingsManager ism) {		
		this.parent = parent;		
		networkreference = reference;
		networkIDAREID = reference.getDefaultNetworkTable().getRow(reference.getSUID()).get(IDAREProperties.IDARE_NETWORK_ID, Long.class);
		if(networkIDAREID == null)
		{
			networkIDAREID = ism.getNextNetworkID();
			reference.getDefaultNetworkTable().getRow(reference.getSUID()).set(IDAREProperties.IDARE_NETWORK_ID, networkIDAREID);
		}
		this.colName = ColName;		
		this.networkID = networkID;
		PrintFDebugger.Debugging(this, "Created a Node with colName " + colName + "; networkIDAREID "+ networkIDAREID + "; networkID " + networkID + " and " + children.size() + " children"); 
	}
	
	public NetworkNode(CyNetwork reference, IDARESettingsManager ism) {
		networkreference = reference;
		
		networkIDAREID = reference.getDefaultNetworkTable().getRow(reference.getSUID()).get(IDAREProperties.IDARE_NETWORK_ID, Long.class);
		if(networkIDAREID == null)
		{
			networkIDAREID = ism.getNextNetworkID();
			reference.getDefaultNetworkTable().getRow(reference.getSUID()).set(IDAREProperties.IDARE_NETWORK_ID, networkIDAREID);
		}
		
	}
	
	public Collection<NetworkNode> getChildren() {
		return children; 
	}
	
	
	public String getNetworkID()
	{
		String ID = "";
		if(networkID != null)
		{
			ID = networkID;
		}
		
		if(parent != null)
		{			
			ID = parent.getNetworkID().equals("") ?  ID : parent.getNetworkID() + SubnetworkCreationTask.subnetworkNameSeparator + ID;
		}
		return ID;
	}
	
	public Collection<CyNetwork> getChildNetworks() {
		Vector<CyNetwork> childnetworks = new Vector<CyNetwork>();
		for(NetworkNode node: children)
		{
			childnetworks.add(node.networkreference);
		}
		return childnetworks; 
	}
	
	public void addChild(NetworkNode child) {
		children.add(child);
	}
	
	
	public void removeChild(NetworkNode child) {
		if(child.parent == this)
		{
			child.parent = null;		
			children.remove(child.networkreference);
		}
	}

	public void setNetwork(CyNetwork network)
	{
		networkreference = network;
		networkIDAREID = network == null ? null : network.getDefaultNetworkTable().getRow(network.getSUID()).get(IDAREProperties.IDARE_NETWORK_ID, Long.class);	
	}
	
	public CyNetwork getNetwork()
	{
		return networkreference;
	}
	
	public void removeChild(CyNetwork child) {
		for(NetworkNode node: children)
		{
			if(node.networkreference == child)
			{		
			node.networkreference = null;					
			}
		}
		
	}
	
	public void setupNetworkReferences(CyNetworkManager mgr)
	{
		PrintFDebugger.Debugging(this, "Trying to restore network reference with IDAREID " + networkIDAREID);
		for(CyNetwork network : mgr.getNetworkSet())
		{
			Long NetworkIDAREID = network.getDefaultNetworkTable().getRow(network.getSUID()).get(IDAREProperties.IDARE_NETWORK_ID, Long.class);
			if(NetworkIDAREID!= null  && NetworkIDAREID.equals(networkIDAREID)){
				networkreference = network;
				PrintFDebugger.Debugging(this, "Restoring Network Node" + print());
				break;
			}
		}		
		for(NetworkNode node : children)
		{
			node.setupNetworkReferences(mgr);
		}
	}
	
	
	private void writeObject(ObjectOutputStream out)
	{
		try{
			PrintFDebugger.Debugging(this, "Writing a Node with colName " + colName + "; networkIDAREID "+ networkIDAREID + "; networkID " + networkID + " and " + children.size() + " children"); 
			out.writeObject(this.colName);
			out.writeObject(this.networkIDAREID);
			out.writeObject(this.networkID);
			out.writeObject(children);
		}
		catch(IOException e)
		{
			
		}
	}
	
	private void readObject(ObjectInputStream in)
	{
		try{
			colName = (String)(in.readObject());
			networkIDAREID = (Long)(in.readObject());
			networkID = (String)(in.readObject());
			children = (Vector<NetworkNode>)(in.readObject());
			PrintFDebugger.Debugging(this, "Successfully read a NetworkNode with colName " + colName + "; networkIDAREID "+ networkIDAREID + "; networkID " + networkID + " and " + children.size() + " children"); 
		}
		catch(IOException | ClassNotFoundException e)
		{
			
		}
	}
	
	public String printWithChildren(String indent)
	{
		String Result = indent + "NetworkRef: " + networkreference + "  networkID: " + networkID  + " IDAREID: " + networkIDAREID + " colName " + colName;
		if(parent != null)
		{
			Result += "; has Parent";
		}
		Result += "\n";
		for(NetworkNode node : children)
		{
			String newindent = indent + "\t";
			Result += node.printWithChildren(newindent);
		}
		return Result;
	}
	
	public String print()
	{
		String Result = "NetworkRef: " + networkreference + "  networkID: " + networkID  + " IDAREID: " + networkIDAREID + " colName " + colName;
		if(parent != null)
		{
			Result += "; has Parent";
		}
		return Result;
	}
}
