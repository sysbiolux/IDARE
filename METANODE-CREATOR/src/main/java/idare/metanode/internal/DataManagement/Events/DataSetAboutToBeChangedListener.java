package idare.metanode.internal.DataManagement.Events;

import idare.metanode.Interfaces.DataSets.DataSet;

/**
 * A Listener of this type will be informed, if DataSets are about to change.
 * This indicates, that the set has not yet changed, but allows preparatory functions.
 * @author Thomas Pfau
 *
 */
public interface DataSetAboutToBeChangedListener {
	/**
	 * Inform that there will be a change in the {@link DataSet}s 
	 * @param e
	 */
	public void datasetChanged(DataSetChangedEvent e);

	/**
	 * Inform that a Collection of changes coming up in the {@link DataSet}s.
	 * @param e
	 */
	public void datasetsChanged(DataSetsChangedEvent e);
	
}
