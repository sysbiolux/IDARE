package idare.imagenode.internal.DataManagement.Events;

import java.util.Collection;
import java.util.Vector;
/**
 * An Event to indicate that specific nodes were updated.
 * @author Thomas Pfau
 *
 */
public class NodeUpdateEvent {

	private Collection<String> nodeIDs;
	/**
	 * Default constructor with a provided set of IDs.
	 * @param updatedIDs
	 */
	public NodeUpdateEvent(Collection<String> updatedIDs) {
		// TODO Auto-generated constructor stub
		nodeIDs = new Vector<String>();
		nodeIDs.addAll(updatedIDs);
	}
	/**
	 * Get all IDs of updated nodes 
	 * @return the {@link Collection} of Strings representing the updated IDs
	 */
	public Collection<String> getupdatedIDs()
	{
		return nodeIDs;
	}
}
