package idare.imagenode.internal.DataManagement.Events;

import idare.imagenode.Interfaces.DataSets.DataSet;
import idare.imagenode.internal.DataManagement.DataSetManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * An event indicating the change of the state of multiple Datasets. (Either their addition or removal)
 * @author Thomas Pfau
 *
 */
public class DataSetsChangedEvent {
	private DataSetManager source;
	private Collection<DataSet> setsChanged;
	private boolean setAdded;
	private boolean setRemoved;
	private boolean setChanged;
	/**
	 * Create a New Event with the changing sets, the {@link DataSetManager} this event originates from and an indicator whether the sets are added or removed. 
	 * @param source The {@link DataSetManager} this event originates from
	 * @param setschanged The Sets that are changing
	 * @param added whether the sets are added or removed.
	 */
	public DataSetsChangedEvent(DataSetManager source, Collection<DataSet> setschanged, boolean added, boolean removed, boolean changed)
	{
	this.source = source;
	setsChanged = setschanged;
	setAdded = added;
	setRemoved = removed;
	setChanged = changed;
	}
	/**
	 * Get the {@link DataSetManager} this event originates from.
	 * @return the source of this event
	 */
	public DataSetManager getSource()
	{
		return source;
	}
	/**
	 * Get the Collection of datasets that are changing.
	 * @return a {@link Collection} of {@link DataSet}s that are changing.
	 */
	public Collection<DataSet> getSet()
	{
		Set<DataSet> changedSets = new HashSet<DataSet>();
		changedSets.addAll(setsChanged);
		return changedSets;
	}
	/**
	 * Obtain information whether the set is being added.
	 * @return boolean Indicator whether the set was added 
	 */

	public boolean wasAdded()
	{
		return setAdded;
	}
	/**
	 * Obtain information whether the set is being removed.
	 * @return boolean Indicator whether the set was removed 
	 */

	public boolean wasRemoved()
	{
		return setRemoved;
	}
	/**
	 * Obtain information whether the set is being changed.
	 * @return boolean Indicator whether the set was changed 
	 */

	public boolean wasChanged()
	{
		return setChanged;
	}
	
}
