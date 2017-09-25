package idare.imagenode.internal.DataManagement.Events;


/**
 * A Listener that listens to the change in Nodes.
 * @author Thomas Pfau
  */
public interface NodeChangedListener {
	/**
	 * Handle a {@link NodeUpdateEvent}, that informs that certain nodes were updated.
	 * @param e The {@link NodeUpdateEvent} to handle.
	 */
	public void handleNodeUpdate(NodeUpdateEvent e);
}
