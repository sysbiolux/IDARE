package idare.subnetwork.internal;

import idare.Properties.IDAREProperties;
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
	public NetworkNode(NetworkNode parent, CyNetwork reference, String ColName, String networkID) {		
		this.parent = parent;		
		networkreference = reference;
		networkIDAREID = reference.getDefaultNetworkTable().getRow(reference.getSUID()).get(IDAREProperties.IDARE_NETWORK_ID, Long.class);
		this.colName = ColName;		
		this.networkID = networkID;
	}
	
	public NetworkNode(CyNetwork reference) {
		networkreference = reference;
		networkIDAREID = reference.getSUID();
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
		PrintFDebugger.Debugging(this, "The manager is " + mgr);
		PrintFDebugger.Debugging(this, "The networkSUID is " + networkIDAREID);
		for(CyNetwork network : mgr.getNetworkSet())
		{
			if(network.getDefaultNetworkTable().getRow(network.getSUID()).get(IDAREProperties.IDARE_NETWORK_ID, Long.class).equals(networkIDAREID)){
				networkreference = network;
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
		}
		catch(IOException | ClassNotFoundException e)
		{
			
		}
	}
}
