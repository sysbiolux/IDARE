package idare.imagenode.internal.DataManagement.Events;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.internal.DataManagement.DataSetManager;
/**
 * An Event Indicating that Datasets are changing.
 * @author Thomas Pfau
 *
 */
public class DataSetChangedEvent {
	
	private DataSetManager source;
	private DataSet setChanged;
	private boolean setAdded;
	private boolean setRemoved;
	private boolean setWasChanged;
	/**
	 * Create a New Event with the changing set, the {@link DataSetManager} this event originates from and an indicator whether the set is added or removed. 
	 * @param source - The {@link DataSetManager} this event originates from
	 * @param setchanged - The Set that is changing
	 * @param added - whether the set is added or removed.
	 */
	public DataSetChangedEvent(DataSetManager source, DataSet setchanged, boolean added, boolean removed, boolean changed)
	{
	this.source = source;
	setChanged = setchanged;
	setAdded = added;
	setWasChanged = changed;
	setRemoved = removed;
	}
	/**
	 * Get the {@link DataSetManager} this event originates from.
	 * @return The {@link DataSetManager} that created this Event
	 */
	public DataSetManager getSource()
	{
		return source;
	}
	/**
	 * Get the {@link DataSet} that is changing
	 * @return Get the Set associated with this event.
	 */
	public DataSet getSet()
	{
		return setChanged;
	}
	/**
	 * Obtain information whether the set is being added.
	 * @return true, if the provided set was added altered, false otherwise
	 */
	public boolean wasAdded()
	{
		return setAdded;
	}
	/**
	 * Obtain information whether the provided set was removed
	 * @return true, if the set was removed, false otherwise
	 */
	public boolean wasRemoved()
	{
		return setRemoved;
	}
	/**
	 * Obtain information whether the set was changed
	 * @return true, if the set was internally altered, false otherwise
	 */
	public boolean wasChanged()
	{
		return setWasChanged;
	}
	
}
